package com.mmq.chatlite;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

public class MessageNotify extends Service {

    private ChildEventListener childEventListener;
    protected static boolean isRunning = true;

    public MessageNotify() {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        isRunning = true;
        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                if (isRunning) {
                    // Get chat room data
                    Rooms room = dataSnapshot.getValue(Rooms.class);

                    // If not read
                    if (room.isRead == false)
                        showNotification(dataSnapshot.getKey(), room.getName() + ": " + room.getLastMessage());
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        API.firebaseRef.child("USERS")
                .child(API.currentUID)
                .child("chatRooms").removeEventListener(childEventListener);
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        API.firebaseRef.child("USERS")
                .child(API.currentUID)
                .child("chatRooms").addChildEventListener(childEventListener);
        return START_NOT_STICKY;
    }

    private void showNotification(String room, String messsage) {
        Intent resultIntent = new Intent(getBaseContext(), ChatActivity.class);
        // Create a new Bundle to store {senderUid, receiverUid}
        Bundle extras = new Bundle();

        // Put extras
        extras.putString("SENDER_UID", API.currentUID);
        extras.putString("RECEIVER_UID", room);
        resultIntent.putExtras(extras);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(getBaseContext());
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,PendingIntent.FLAG_ONE_SHOT );

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(getBaseContext())
                        .setContentTitle("Chat Lite")
                        .setContentText(messsage)
                        .setSmallIcon(R.drawable.ic_chat_black_24dp)
                        .setAutoCancel(true)
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                        .setContentIntent(resultPendingIntent);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);


        mNotificationManager.notify(001, mBuilder.build());
    }
}
