package com.mmq.chatlite;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.UploadTask;
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.EmojiPopup;
import com.vanniktech.emoji.listeners.OnEmojiPopupDismissListener;
import com.vanniktech.emoji.listeners.OnEmojiPopupShownListener;
import com.vanniktech.emoji.twitter.TwitterEmojiProvider;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;


public class ChatActivity extends AppCompatActivity {

    // Widgets
    private View chatView;
    private Button btnSend;
    private ProgressDialog progressDialog;

    private EmojiEditText editTextMsg;
    private RecyclerView recyclerView;
    private ImageView imageViewEmoticon;
    private Button btnImage;
    private ProgressBar progressBarMsg;
    private Toolbar toolbar;

    private EmojiPopup emojiPopup;
    private Uri selectedImage;
    private final int REQUEST_CODE = 1;

    private String currentRoomID;

    ArrayList<Messages> messageList;

    @Override
    protected void onStart() {
        MessageNotify.isRunning = false;
        super.onStart();
    }

    @Override
    protected void onPostResume() {
        MessageNotify.isRunning = false;
        super.onPostResume();
    }

    @Override
    protected void onResume() {
        MessageNotify.isRunning = false;
        super.onResume();
    }

    @Override
    protected void onPause() {
        MessageNotify.isRunning = true;
        super.onPause();

    }

    @Override
    protected void onStop() {
        MessageNotify.isRunning = true;
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        MessageNotify.isRunning = true;
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Disable notifier
        MessageNotify.isRunning = false;

        EmojiManager.install(new TwitterEmojiProvider());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        // Mapping
        chatView = findViewById(R.id.chatView);
        toolbar = findViewById(R.id.toolBar);
        btnSend = findViewById(R.id.btnSend);
        editTextMsg = findViewById(R.id.editTextMsg);
        recyclerView = findViewById(R.id.chatRoom);
        imageViewEmoticon = findViewById(R.id.imageViewEmoticon);
        btnImage = findViewById(R.id.btnImage);
        progressBarMsg = findViewById(R.id.progressBarMsg);

        // Emoji inits
        EmojiPopup.Builder builder = EmojiPopup.Builder.fromRootView(chatView);
        builder.setOnEmojiPopupDismissListener(new OnEmojiPopupDismissListener() {
            @Override
            public void onEmojiPopupDismiss() {
                imageViewEmoticon.setBackgroundResource(R.drawable.ic_insert_emoticon_black_24dp);
            }
        }).setOnEmojiPopupShownListener(new OnEmojiPopupShownListener() {
            @Override
            public void onEmojiPopupShown() {
                imageViewEmoticon.setBackgroundResource(R.drawable.ic_keyboard_black_24dp);
            }
        });
        emojiPopup = builder.build(editTextMsg);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // BACK Navigation button onclick
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // Get extras
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        // Config data
        Conversation.senderUid = extras.getString("SENDER_UID");
        Conversation.receiverUid = extras.getString("RECEIVER_UID");
        Conversation.senderRoomId = String.format("USERS/%s/chatRooms/%s", Conversation.senderUid, Conversation.receiverUid);
        Conversation.receiverRoomId = String.format("USERS/%s/chatRooms/%s", Conversation.receiverUid, Conversation.senderUid);

        currentRoomID = Conversation.senderRoomId + "/MESSAGES";

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Intent login = new Intent(ChatActivity.this, LoginActivity.class);
            startActivity(login);
        }

        // Subscribe the UserInfoEventListener.
        API.firebaseRef.child("USERS").child(API.currentUID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    // Assign the current User object to global var
                    API.currentUser = dataSnapshot.getValue(Users.class);

                    // Load all messages
                    loadMessagesFromFirebase(currentRoomID);

                    // Remove the progressbar
                    progressBarMsg.setVisibility(View.GONE);

                } catch (java.lang.NullPointerException ex) {
                    Toast.makeText(ChatActivity.this, ex.getMessage().toString(), Toast.LENGTH_SHORT).show();
                    progressBarMsg.setVisibility(View.GONE);
                    API.firebaseAuth.signOut();
                    Intent intent = new Intent(ChatActivity.this, LoginActivity.class);
                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // Get receiver info
        API.firebaseRef.child("USERS").child(Conversation.receiverUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Set Activity title
                setTitle(dataSnapshot.getValue(Users.class).getDisplayName());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        editTextMsg.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                MessageNotify.isRunning = false;
                return false;
            }
        });

        // Send message button
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = editTextMsg.getText().toString();
                if (message.isEmpty()) {
                } else {
                    API.addPrivateMessage(false, API.currentUID, Conversation.receiverUid, message, Conversation.senderRoomId, Conversation.receiverRoomId);
                    // Clear the EditText after sending
                    editTextMsg.setText("");
                }
            }
        });

        imageViewEmoticon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (emojiPopup.isShowing()) {
                    emojiPopup.dismiss();
                }
                else {
                    emojiPopup.toggle();
                }
            }
        });

        btnImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select image"), REQUEST_CODE);
            }
        });
    }

    public void loadMessagesFromFirebase(String roomId) {
        API.firebaseRef.child(roomId)
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                messageList = new ArrayList<>();
                ChatAdapter chatAdapter = new ChatAdapter(messageList);
                LinearLayoutManager chatLayoutManager = new LinearLayoutManager(ChatActivity.this);

                // Set orientation
                chatLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                recyclerView.setLayoutManager(chatLayoutManager);
                recyclerView.setAdapter(chatAdapter);

                for (DataSnapshot db : dataSnapshot.getChildren()) {
                        Messages msg = db.getValue(Messages.class);

                        if (msg.getUid().toLowerCase().contains(API.currentUser.getUid().toLowerCase())) {
                            msg.setMe(true);
                        } else {
                            msg.setMe(false);
                        }

                    messageList.add(msg);
                }
                // Move to the last item of layout
                chatLayoutManager.scrollToPosition(messageList.size() - 1);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // Set room isRead to true.
        API.firebaseRef.child("USERS")
                .child(API.currentUID)
                .child("chatRooms")
                .child(Conversation.receiverUid)
                .child("isRead").setValue(true);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.context_menu, menu);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE
                && resultCode == RESULT_OK
                && data != null
                && data.getData() != null) {


            selectedImage = data.getData();

            // Upload image file
            if (selectedImage != null) {
                progressDialog = ProgressDialog.show(ChatActivity.this, null, "Uploading image...", true);
                API.firebaseStorage.child("CHAT_IMAGES/" + System.currentTimeMillis() + ".image")
                        .putFile(selectedImage)
                        .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                // Get the uploaded image URL
                                String imageURL = task.getResult().getDownloadUrl().toString();

                                // Send message
                                API.addPrivateMessage(true, API.currentUID, Conversation.receiverUid, imageURL, Conversation.senderRoomId, Conversation.receiverRoomId);

                                progressDialog.dismiss();
                            }
                        });
            }

            try {
                Bitmap bitMap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.chat_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.m_profile:
                Intent profile = new Intent(ChatActivity.this, ProfileActivity.class);
                profile.putExtra("PROFILE", Conversation.receiverUid);
                startActivityForResult(profile, 0);
                break;

            case R.id.m_search:
                searchForMessage();
                break;

            case R.id.m_sign_out:
                API.firebaseAuth.signOut();
                Intent intent = new Intent(ChatActivity.this, LoginActivity.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void searchForMessage() {
        final EditText searchInput = new EditText(ChatActivity.this);

        AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
        builder.setTitle("Search for text message");
        builder.setView(searchInput);
        builder.setPositiveButton("Search", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                final String input = searchInput.getText().toString();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Do nothing here
            }
        });
        builder.show();
    }

    public String getImageExtension(Uri uri) {
        ContentResolver contentResolver = this.getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }
}
