package com.mmq.chatlite;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class ContactFragment extends Fragment {

    ArrayList<Users> contactList;
    ContactAdapter contactAdapter;
    FloatingActionButton floatingButton;
    ListView listViewContact;

    public ContactFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        // Mapping things
        View view = inflater.inflate(R.layout.fragment_contact, container, false);
        listViewContact = view.findViewById(R.id.listViewContact);
        floatingButton = view.findViewById(R.id.floatButtonContact);

        // Get contact list on Firebase database
        API.firebaseRef.child("USERS")
                .child(API.currentUID)
                .child("contacts").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                contactList = new ArrayList<>();
                contactAdapter = new ContactAdapter(getContext(), R.layout.contact_item, contactList);
                listViewContact.setAdapter(contactAdapter);

                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    // Get each contact UID
                    final String contactUid = data.getKey();

                    // Get user
                    API.firebaseRef.child("USERS").child(contactUid).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            // Get and add User to the list
                            Users user = dataSnapshot.getValue(Users.class);
                            contactList.add(user);

                            listViewContact.setAdapter(contactAdapter);
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        listViewContact.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // Get the selected contact
                Users selectUser = (Users) listViewContact.getItemAtPosition(i);

                API.openPrivateChatActivity(API.currentUID, selectUser.getUid(), getView());
            }
        });

        // FloatingAction Button On Click
        floatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddContactDialog();
            }
        });

        return view;
    }

    public void showAddContactDialog() {
        final EditText contactInput = new EditText(getContext());

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Add new contact");
        builder.setView(contactInput);
        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                final String inputEmail = contactInput.getText().toString();

                API.firebaseRef.child("USERS")
                        .orderByChild("account")
                        .equalTo(inputEmail)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()) {
                            for (DataSnapshot childSnapshot: dataSnapshot.getChildren()) {
                                // Add new contact
                                API.addContact(childSnapshot.getKey());

                                // Show mesage
                                Snackbar snackbar = Snackbar
                                        .make(getActivity().findViewById(R.id.activity_main), "Contact added successful!", Snackbar.LENGTH_LONG);

                                snackbar.show();
                            }
                        }
                        else {
                            Snackbar snackbar = Snackbar
                                    .make(getActivity().findViewById(R.id.activity_main), "Can't find the user with email: " + inputEmail, Snackbar.LENGTH_LONG);

                            snackbar.show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
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
}
