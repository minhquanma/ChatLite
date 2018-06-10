package com.mmq.chatlite;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ProfileActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ImageView imgViewDlgProfile;
    private TextView txtViewProfile;
    private TextView txtViewEmail;
    private TextView txtViewGender;
    private TextView txtViewBirthday;
    private ProgressBar progressBar;

    private String userId;
    private Users user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Mapping
        toolbar = findViewById(R.id.toolBar);
        imgViewDlgProfile = findViewById(R.id.imgViewDlgProfile);
        txtViewProfile = findViewById(R.id.txtViewProfile);
        txtViewEmail = findViewById(R.id.txtViewEmail);
        txtViewGender = findViewById(R.id.txtViewGender);
        txtViewBirthday = findViewById(R.id.txtViewBirthday);
        progressBar = findViewById(R.id.progressBar);

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
        userId = intent.getStringExtra("PROFILE");

        // Load userId profile
        API.firebaseRef.child("USERS").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(Users.class);
                loadProfileData(user);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void loadProfileData(Users user) {
        Picasso.with(ProfileActivity.this)
                .load(user.getAvatar())
                .into(imgViewDlgProfile);
        txtViewProfile.setText(user.getDisplayName());
        txtViewEmail.setText(user.getAccount());
        txtViewGender.setText(user.isGender() ? "Male" : "Female");
        txtViewBirthday.setText(user.getBirthDay());
    }
}
