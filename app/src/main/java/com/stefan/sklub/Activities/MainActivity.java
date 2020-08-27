package com.stefan.sklub.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.auth.FirebaseAuth;
import com.stefan.sklub.Database.FirestoreDB;
import com.stefan.sklub.Interfaces.OnGetItems;
import com.stefan.sklub.Model.Event;
import com.stefan.sklub.Model.User;
import com.stefan.sklub.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class MainActivity extends BaseActivity {

    final String TAG = "MainActivity ispis";
    private FirebaseAuth mAuth;
    private FirestoreDB db;
    private Menu mMenu;
    private User me;
    private Fragment splashScreenFragment;
    private EventsFragment eventsFragment;
    public List<Event> events = new ArrayList<Event>();
    private MenuItem settingsItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        db = FirestoreDB.getInstance();

        if (getSupportActionBar() != null)
            getSupportActionBar().hide();

        if (mAuth.getCurrentUser() == null)
            startActivity(new Intent(this, LoginActivity.class));
        else {
            splashScreenFragment = new SplashScreenFragment(isOnline());
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.frameLayout, splashScreenFragment);
            fragmentTransaction.commit();

            startApp();
        }
    }

    private void startApp() {
        if (isOnline()) {
            db.getUserByUserUid(mAuth.getCurrentUser().getUid(), user -> {
                me = user;
                loadRecycleView();
            });
        } else {
            MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(this);
            materialAlertDialogBuilder
                    .setTitle("You're offline")
                    .setMessage("You wan't to see available events? Unobtainable. You wan't to edit your profile? Ridiculous. " +
                            "You wan't to see who else is attending an event? Beyond the bounds of possibility. ")
                    .setPositiveButton("Try again", (dialogInterface, i) -> {
                        startApp();
                    })
                    .setNegativeButton("Close app", (dialogInterface, i) -> {
                        finishAffinity();
                    });
            materialAlertDialogBuilder.show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        this.mMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_action_my_profile:
                Intent userProfileIntent = new Intent(this, UserProfileActivity.class);
                userProfileIntent.putExtra("me", me);
                if (me == null) {
                    Log.e(TAG, "Me is NULL");
                }
                startActivityForResult(userProfileIntent, 1);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void loadRecycleView() {
        db.getEvents(new OnGetItems<Event>() {
            @Override
            public void onGetItem(Event item) {
                Log.d(TAG, "Firestore.DB: getEvents(): event fetched");
                events.add(item);
            }

            @Override
            public void onFinishedGettingItems() {
                Log.d(TAG, "Firestore.DB: getEvents(): all events fetched");
                Collections.sort(events);

                eventsFragment = new EventsFragment(events, getApplicationContext(), me);

                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
                fragmentTransaction.replace(R.id.frameLayout, eventsFragment);
                fragmentTransaction.commit();

                getSupportActionBar().show();
            }

        });
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        Log.d(TAG, "onActivityResult(): requestCode: " + requestCode + ", resultCode: " + resultCode);
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode == 3) {
//            if (requestCode == 3) {
                if (intent.hasExtra("event")) {
                    Event event = intent.getParcelableExtra("event");
                    Log.i(TAG, "onActivityResult(): Event data received successfully");
                    eventsFragment.addEvent(event);
                } else {
                    Log.e(TAG, "onActivityResult(): Event data not received");
                }
//            }
        } else if (resultCode == 1) {
            if (intent.hasExtra("me")) {
                me = intent.getParcelableExtra("me");
                Log.i(TAG, "onActivityResult(): User data received successfully");
                RequestOptions options = new RequestOptions()
                        .centerCrop()
                        .placeholder(R.drawable.ic_person_white_18dp)
                        .error(R.drawable.ic_person_white_18dp)
                        .circleCrop();
                Glide.with(this).asBitmap().load(me.getImgUrl()).apply(options).into(new SimpleTarget<Bitmap>(100,100) {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        settingsItem.setIcon(new BitmapDrawable(getResources(), resource));
                    }
                });
            } else {
                Log.e(TAG, "onActivityResult(): User data not received");
            }
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        settingsItem = menu.findItem(R.id.menu_action_my_profile);
        db.getUserByUserUid(mAuth.getUid(), user -> {
            this.me = user;

            RequestOptions options = new RequestOptions()
                    .centerCrop()
                    .placeholder(R.drawable.ic_person_white_18dp)
                    .error(R.drawable.ic_person_white_18dp)
                    .circleCrop();

            if (me.getImgUrl().equals("default")) {
                Glide.with(this).asBitmap().load(R.drawable.ic_person_white_18dp).apply(options).into(new SimpleTarget<Bitmap>(100,100) {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        settingsItem.setIcon(new BitmapDrawable(getResources(), resource));
                    }
                });
            } else {
                Glide.with(this).asBitmap().load(user.getImgUrl()).apply(options).into(new SimpleTarget<Bitmap>(100, 100) {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        settingsItem.setIcon(new BitmapDrawable(getResources(), resource));
                    }
                });
            }
        });
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        System.gc();
        this.finishAffinity();
    }
}
