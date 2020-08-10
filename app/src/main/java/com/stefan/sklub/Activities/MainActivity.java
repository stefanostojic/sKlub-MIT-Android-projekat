package com.stefan.sklub.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.stefan.sklub.Database.FirestoreDB;
import com.stefan.sklub.Adapters.EventAdapter;
import com.stefan.sklub.R;

public class MainActivity extends BaseActivity {

    final String TAG = "MainActiviy ispis";
    private FirebaseAuth mAuth;
    private FirestoreDB firestoreDB;
    private RecyclerView mRecyclerView;
    private FloatingActionButton fab;
    private EventAdapter mEventAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_event);
        fab = (FloatingActionButton) findViewById(R.id.floating_action_button);

        fab.setOnClickListener(view -> {
            startActivity(new Intent(MainActivity.this, AddEventActivity.class));
        });

        mAuth = FirebaseAuth.getInstance();
        firestoreDB = FirestoreDB.getInstance();

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
        firestoreDB.getEvents(event -> {
            mEventAdapter.addEvent(event);
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        System.gc();
        this.finishAffinity();
    }

}
