package com.stefan.sklub.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.stefan.sklub.Database.FirestoreDB;
import com.stefan.sklub.Adapters.EventAdapter;
import com.stefan.sklub.Interfaces.OnGetItem;
import com.stefan.sklub.Interfaces.OnGetItems;
import com.stefan.sklub.Model.Event;
import com.stefan.sklub.R;

public class MainActivity extends BaseActivity {

    final String TAG = "MainActivity ispis";
    private FirebaseAuth mAuth;
    private FirestoreDB firestoreDB;
    private RecyclerView mRecyclerView;
    private FloatingActionButton fab;
    private EventAdapter mEventAdapter;

//    private SplashScreenFragment splashScreenFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "onCreate()");

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_event);
        fab = (FloatingActionButton) findViewById(R.id.floating_action_button);

//        splashScreenFragment = new SplashScreenFragment();

//        FragmentManager fragmentManager = getSupportFragmentManager();
//        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//        fragmentTransaction.replace(R.id.main_activity_fragment_1, splashScreenFragment);
//        fragmentTransaction.commit();

        fab.setOnClickListener(view -> {
            startActivity(new Intent(MainActivity.this, AddEventActivity.class));
        });

        mAuth = FirebaseAuth.getInstance();
        firestoreDB = FirestoreDB.getInstance();

//        final Handler handler = new Handler();
//        handler.postDelayed(() -> {
//            FragmentManager fragmentManager1 = getSupportFragmentManager();
//            FragmentTransaction fragmentTransaction1 = fragmentManager1.beginTransaction();
//            fragmentTransaction1.remove(splashScreenFragment);
//            fragmentTransaction1.commitAllowingStateLoss();
//        }, 1500);

//        firestoreDB.fixGeoFirestoreDocumentsPlaces();
//        firestoreDB.fixGeoFirestoreDocumentsUsers();
        loadRecycleView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    private void loadRecycleView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);
        mEventAdapter = new EventAdapter();
//        firestoreDB.getEvents(event -> {
//            mEventAdapter.addEvent(event);
//        });
        Log.d(TAG, "Firestore.DB: getEvents(): starts fetching events");
        firestoreDB.getEvents(new OnGetItems<Event>() {
            @Override
            public void onGetItem(Event item) {
                Log.d(TAG, "Firestore.DB: getEvents(): event fetched");
                mEventAdapter.addEvent(item);
            }

            @Override
            public void onFinishedGettingItems() {
                Log.d(TAG, "Firestore.DB: getEvents(): all events fetched");
            }

        });
        mEventAdapter.setOnClickHandler(event -> {
            Intent eventDetailsIntent = new Intent(this, EventDetailsActivity.class);
            eventDetailsIntent.putExtra("event", event);
            this.startActivity(eventDetailsIntent);
        });
        mEventAdapter.setContext(this);
        mRecyclerView.setAdapter(mEventAdapter);

//        checkPermission(Manifest.permission.CAMERA, CAMERA_REQUEST_CODE);
//        ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.ACCESS_NETWORK_STATE }, 0);
//        ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.READ_EXTERNAL_STORAGE }, 0);
//        ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE }, 0);
//        ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.INTERNET }, 0);
//        ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, 0);
//        ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.CAMERA }, 0);
    }

//    @Override
//    protected void onStart() {
//        super.onStart();
//        Log.d(TAG, "onStart()");
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        Log.d(TAG, "onResume()");
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        Log.d(TAG, "onPause()");
//        if(isFinishing()){
//            Log.d(TAG, "onPause() is finishing");
//        }
//    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//        Log.d(TAG, "onStop()");
//    }
//
//    @Override
//    protected void onRestart() {
//        super.onRestart();
//        Log.d(TAG, "onRestart()");
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        Log.d(TAG, "onDestroy()");
//    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        System.gc();
        this.finishAffinity();
    }

}
