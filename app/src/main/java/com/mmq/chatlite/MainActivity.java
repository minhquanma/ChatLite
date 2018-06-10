package com.mmq.chatlite;



import android.content.Intent;
import android.os.*;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.twitter.TwitterEmojiProvider;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private LinearLayout progressLayout;
    private int[] TABS_ICON = {
            R.drawable.ic_chat_black_24dp,
            R.drawable.ic_contacts_black_24dp,
            R.drawable.ic_person_black_24dp
    };
    private TabLayout tabLayout;
    private ViewPager viewPager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EmojiManager.install(new TwitterEmojiProvider());

        // TabLayout mapping
        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);
        progressLayout = findViewById(R.id.progressLayout);

        // Get current user UID
        API.currentUID = API.firebaseAuth.getCurrentUser().getUid();

        // "Get current user profile" event subscription
        API.firebaseRef.child("USERS").child(API.currentUID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Assigning USER data
                API.currentUser = dataSnapshot.getValue(Users.class);

                startService(new Intent(getBaseContext(), MessageNotify.class));

                // After that... => Setting up ViewPager
                setupViewPager(viewPager);
                tabLayout.setupWithViewPager(viewPager);

                // Setup TabIcons
                setupTabIcons();

                progressLayout.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            // After that... => Setting up ViewPager
                setupViewPager(viewPager);
                tabLayout.setupWithViewPager(viewPager);

                // Setup TabIcons
                setupTabIcons();

                progressLayout.setVisibility(View.GONE);
                startService(new Intent(getBaseContext(), MessageNotify.class));
            }
        });
    }

    private void setupTabIcons() {
        for (int i = 0; i < TABS_ICON.length; i++) {
            tabLayout.getTabAt(i).setIcon(TABS_ICON[i]);
        }
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new RoomFragment(), "");
        adapter.addFragment(new ContactFragment(), "");
        adapter.addFragment(new ProfileFragment(), "");
        viewPager.setAdapter(adapter);
    }

    private class ViewPagerAdapter extends FragmentPagerAdapter {

        List<Fragment> fragmentList = new ArrayList<>();
        List<String> fragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            fragmentList.add(fragment);
            fragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return fragmentTitleList.get(position);
        }
    }
}
