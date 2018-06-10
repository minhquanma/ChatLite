package com.mmq.chatlite;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TableLayout;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class RoomFragment extends Fragment {

    ArrayList<Rooms> chatRoomList;
    RoomAdapter roomAdapter;

    // Mapping
    private ListView roomListView;
    private TabHost tableLayout;

    public RoomFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Mapping
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        roomListView = view.findViewById(R.id.roomListView);

        chatRoomList = new ArrayList<>();

        // Get all chat rooms
        API.firebaseRef.child("USERS")
                .child(API.currentUID)
                .child("chatRooms")
                .orderByChild("timestamp").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (getActivity() != null) {
                    chatRoomList = new ArrayList<>();
                    roomAdapter = new RoomAdapter(getContext(), R.layout.room_item, chatRoomList);
                    roomListView.setAdapter(roomAdapter);
                }

                for (DataSnapshot db : dataSnapshot.getChildren()) {
                    // Get each chat room
                    Rooms room = db.getValue(Rooms.class);

                    if (room.getTimestamp() != null) {
                        room.setId(db.getKey());
                        chatRoomList.add(room);
                    }
                }

                // Reverse messages list (lasted
                Collections.reverse(chatRoomList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        roomListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // Get the selected room
                Rooms selectedRoom = ((Rooms) adapterView.getItemAtPosition(i));

                // Open chat room
                API.openPrivateChatActivity(API.currentUID, selectedRoom.getId(), getView());
            }
        });

        // Inflate the layout for this fragment
        return view;
    }

}


