package com.stefan.sklub.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.widget.ViewPager2;
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.tabs.TabLayout;
import com.stefan.sklub.Adapters.UserProfileEventsViewStateAdapter;
import com.stefan.sklub.Database.FirestoreDB;
import com.stefan.sklub.Interfaces.OnGetItems;
import com.stefan.sklub.Model.Event;
import com.stefan.sklub.Model.User;
import com.stefan.sklub.R;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class UserProfileActivity extends BaseActivity {

    private static final String TAG = "UserProfileActivity ispis";
    private static final int EDIT_PROFILE = 1;
    private static final int SUCCESS = 2;
    private FirestoreDB db;
    private User user;
    boolean isMyProfile;
    private LoadingFragment loadingFragment;
    private UserEventsFragment userOrganisedEventsFragment;
    private UserEventsFragment userAttendedEventsFragment;
    public List<Event> events;

    @BindView(R.id.iv_img)
    ImageView iv_img;
    @BindView(R.id.tv_firstname)
    TextView tv_firstname;
    @BindView(R.id.tv_lastname)
    TextView tv_lastname;
    @BindView(R.id.tv_gender_and_age)
    TextView tv_gender_and_age;
    @BindView(R.id.user_profile_tab_layout)
    TabLayout user_profile_tab_layout;

    private RequestOptions options = new RequestOptions()
            .centerCrop()
            .placeholder(R.drawable.ic_person_white_18dp)
            .error(R.drawable.ic_person_white_18dp)
            .circleCrop();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        ButterKnife.bind(this);
        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle("My profile");

        db = FirestoreDB.getInstance();

        if (getIntent().hasExtra("me")) {
            user = getIntent().getParcelableExtra("me");
            isMyProfile = true;
            fillUserData();
        } else if (getIntent().hasExtra("userDocId")) {
            db.getUserByUserDocId(getIntent().getStringExtra("userDocId"), user1 -> {
                this.user = user1;
                fillUserData();
                if (getSupportActionBar() != null)
                    getSupportActionBar().setTitle(user.getFirstName() + "'s profile");
            });
        } else {
            Log.e(TAG, "No user data passed");
            return;
        }
    }

    private void fillUserData() {
        Log.d(TAG, "fillUserData(): ");
        Log.d(TAG, user.toString());
        tv_firstname.setText(user.getFirstName());
        tv_lastname.setText(user.getLastName());
        String gender = user.getGender().equals("m") ?
                getResources().getString(R.string.male) : getResources().getString(R.string.female);
        Period age = Period.between(user.getBirthday(), LocalDate.now());
        tv_gender_and_age.setText(gender + ", " + age.getYears());
        if (user.getImgUrl().equals("default")) {
            Log.i(TAG, "No image data");
            Glide.with(this)
                    .load(R.drawable.ic_person_white_18dp)
                    .apply(options)
                    .into(iv_img);
        } else {
            Glide.with(this)
                    .load(user.getImgUrl())
                    .apply(options)
                    .into(iv_img);
        }

        loadingFragment = new LoadingFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
        fragmentTransaction.add(R.id.frameLayoutForUserProfile, loadingFragment);
        fragmentTransaction.commit();

        loadOrganisedEvents();

        user_profile_tab_layout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Log.d(TAG, "Tab changed to: " + tab.getText().toString());
                loadingFragment = new LoadingFragment();
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
                fragmentTransaction.replace(R.id.frameLayoutForUserProfile, loadingFragment);
                fragmentTransaction.commit();
                if (tab.getText().toString().equals("My events")) {
                    loadOrganisedEvents();
                } else {
                    loadAttendedEvents();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                Log.d(TAG, "Tab unselected: " + tab.getText().toString());

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                Log.d(TAG, "Tab reselected to: " + tab.getText().toString());

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (isMyProfile) {
            getMenuInflater().inflate(R.menu.edit_menu, menu);
        } else {
            getMenuInflater().inflate(R.menu.report_menu, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_action_edit:
                Intent editProfileIntent = new Intent(this, EditProfileActivity.class);
                editProfileIntent.putExtra("me", user);
                startActivityForResult(editProfileIntent, EDIT_PROFILE);
                return true;
            case R.id.home:
                Intent returnIntent = new Intent();
                returnIntent.putExtra("me", user);
                setResult(1, returnIntent);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: requestCode: " + requestCode + ", resultCode: " + resultCode);
        if (requestCode == EDIT_PROFILE)
        {
            if (resultCode == SUCCESS) {
                user = data.getParcelableExtra("updatedUser");
                Log.d(TAG, "Refilling with updated User data...");
                fillUserData();
            }
        }
    }

    private void loadOrganisedEvents() {
        events = new ArrayList<>();
        db.getEventsByOrganiser(user.getUserDocId(), new OnGetItems<Event>() {
            @Override
            public void onGetItem(Event event) {
                Log.d(TAG, "Organised event loaded: " + event);
                events.add(event);
            }

            @Override
            public void onFinishedGettingItems() {
                Log.d(TAG, "Finished getting organised events");
                if (events.size() == 0) {
                    LoadingFragment loadingFragment = new LoadingFragment();
                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
                    fragmentTransaction.replace(R.id.frameLayoutForUserProfile, loadingFragment);
                    fragmentTransaction.commit();
                }
                userOrganisedEventsFragment = new UserEventsFragment();
                userOrganisedEventsFragment.setEventsData(events);
                userOrganisedEventsFragment.setContext(UserProfileActivity.this);
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
                fragmentTransaction.replace(R.id.frameLayoutForUserProfile, userOrganisedEventsFragment);
                fragmentTransaction.commit();
            }
        });
    }

    private void loadAttendedEvents() {
        events = new ArrayList<>();
        db.getEventsByAttendee(user.getUserDocId(), new OnGetItems<Event>() {
            @Override
            public void onGetItem(Event event) {
                Log.d(TAG, "Attended event loaded: " + event);
                events.add(event);
            }

            @Override
            public void onFinishedGettingItems() {
                Log.d(TAG, "Finished getting attended events");
                if (events.size() == 0) {
                    LoadingFragment loadingFragment = new LoadingFragment();
                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
                    fragmentTransaction.replace(R.id.frameLayoutForUserProfile, loadingFragment);
                    fragmentTransaction.commit();
                }
                userOrganisedEventsFragment = new UserEventsFragment();
                userOrganisedEventsFragment.setEventsData(events);
                userOrganisedEventsFragment.setContext(UserProfileActivity.this);
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
                fragmentTransaction.replace(R.id.frameLayoutForUserProfile, userOrganisedEventsFragment);
                fragmentTransaction.commit();
            }
        });
    }
}