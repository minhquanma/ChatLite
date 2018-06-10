package com.mmq.chatlite;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by mmq on 09/08/2017.
 */

public class API {

    // Firebase
    public static FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    public static DatabaseReference firebaseRef = firebaseDatabase.getReference();
    public static FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    public static StorageReference firebaseStorage = FirebaseStorage.getInstance().getReference();
    public static String chatRoomID;

    private static Users user = new Users();
    private static ProgressDialog progressDialog;

    // Users
    public static Users currentUser;
    public static String currentUID;

    public API() {

    }

    // Ham tao tin nhan moi tren firebase
    /*
    public static void addMessage(Users currentUser, String message, String roomID) {
        // Tao key moi tren firebase
        String newKey = firebaseRef.child(roomID)
                .push().getKey();


        firebaseRef.child(roomID).child(newKey)
                .setValue(new Messages(currentUser.getUid(), newKey, currentUser.getDisplayName(),
                        message,
                        currentUser.avatar));
    }*/


    public static void addPrivateMessage(final boolean isImage, String senderUid, String receiverUid,
                                         final String message, final String senderRoomId, String receiverRoomId) {
        // Create message on the Sender
        String key = firebaseRef.child(senderRoomId + "/MESSAGES/")
                .push().getKey();

        // Set message value
        firebaseRef.child(senderRoomId).child("MESSAGES").child(key)
                .setValue(new Messages(senderUid, key, currentUser.getDisplayName(),
                        message,
                        currentUser.getAvatar(),
                        ServerValue.TIMESTAMP,
                        isImage));


        firebaseRef.child("USERS").child(receiverUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Users receiver = dataSnapshot.getValue(Users.class);

                Map<String, Object> map = new HashMap<String, Object>();
                map.put("name", receiver.getDisplayName());
                map.put("avatar", receiver.getAvatar());
                map.put("lastMessage", "You: " + (isImage ? "Image" : message));
                map.put("isRead", true);
                map.put("timestamp", ServerValue.TIMESTAMP);

                // Update sender chat room information
                firebaseRef.child(senderRoomId).updateChildren(map);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // Create message on the Receiver
        firebaseRef.child(receiverRoomId).child("MESSAGES").child(key)
                .setValue(new Messages(senderUid, key, currentUser.getDisplayName(),
                        message,
                        currentUser.getAvatar(),
                        ServerValue.TIMESTAMP,
                        isImage));

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("name", currentUser.getDisplayName());
        map.put("avatar", currentUser.getAvatar());
        map.put("lastMessage", (isImage ? "Image" : message));
        map.put("isRead", false);
        map.put("timestamp", ServerValue.TIMESTAMP);

        // Update receiver chat room metadata
        firebaseRef.child(receiverRoomId).updateChildren(map);
    }

    public static void removePrivateMessage(String key) {
        firebaseRef.child(Conversation.senderRoomId)
                .child(key)
                .removeValue();

        firebaseRef.child(Conversation.receiverRoomId)
                .child(key)
                .removeValue();
    }

    public static void removeMessage(String key) {
        // Xoa key
        firebaseRef.child(chatRoomID)
                .child(key)
                .removeValue();
    }

    public static void openChatActivityById(String roomId, View view) {
        Intent chatIntent = new Intent(view.getContext(), ChatActivity.class);
        chatIntent.putExtra("ROOMID", roomId);
        view.getContext().startActivity(chatIntent);
    }

    public static void openPrivateChatActivity(String senderUid, String receiverUid, View view) {
        Intent chatIntent = new Intent(view.getContext(), ChatActivity.class);

        // Create a new Bundle to store {senderUid, receiverUid}
        Bundle extras = new Bundle();

        // Put extras
        extras.putString("SENDER_UID", senderUid);
        extras.putString("RECEIVER_UID", receiverUid);
        chatIntent.putExtras(extras);

        // Start activity
        view.getContext().startActivity(chatIntent);
    }

    public static void addContact(String contactID) {
        firebaseRef.child("USERS").child(currentUID).child("contacts").child(contactID).setValue(true);
    }

    public static void removeContact(String contactID) {
        firebaseRef.child("USERS").child(currentUID).child("contacts").child(contactID).removeValue();
    }

    // Xem thong tin user
    public static void showProfileDialog(Context context, Users user) {
        AlertDialog.Builder aBuilder = new AlertDialog.Builder(context);
        final View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_profile, null);

        ImageView imgViewDlgProfile = dialogView.findViewById(R.id.imgViewDlgProfile);
        TextView txtViewDlgEmail = dialogView.findViewById(R.id.txtViewProfile);
        TextView txtViewDlgGender = dialogView.findViewById(R.id.txtViewDlgGender);

        // Load thong tin
        if (URLUtil.isValidUrl(user.getAvatar())) {
            Picasso.with(context)
                    .load(user.getAvatar())
                    .into(imgViewDlgProfile);
        }
        txtViewDlgEmail.setText(user.getAccount());
        txtViewDlgGender.setText("Gender: " + user.stringGender());

        aBuilder.setView(dialogView);
        aBuilder.setTitle(user.getDisplayName());
        aBuilder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Do nothing.
            }
        });
        final AlertDialog dialog = aBuilder.create();
        dialog.show();
    }

    public static void showProfileDialog(final Context context, String uid) {
        progressDialog = ProgressDialog.show(context, "", "Please wait...");

        firebaseRef.child("USERS").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(Users.class);

                showProfileDialog(context, user);

                // Tat progress
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
