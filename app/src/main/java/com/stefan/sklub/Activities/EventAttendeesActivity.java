package com.stefan.sklub.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.stefan.sklub.Adapters.AttendeeAdapter;
import com.stefan.sklub.Adapters.EventAdapter;
import com.stefan.sklub.Model.Event;
import com.stefan.sklub.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EventAttendeesActivity extends BaseActivity {
    String TAG = "EventAttendeesActivity ispis";
    AttendeeAdapter attendeeAdapter;
    Event event;

    @BindView(R.id.recyclerview_attendees)
    RecyclerView recyclerviewAttendees;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_attendees);
        ButterKnife.bind(this);

        if (getIntent().hasExtra("event")) {
            event = getIntent().getParcelableExtra("event");
            viewAttendees();
        } else {
            Log.e(TAG, "No Event data");
        }
    }

    private void viewAttendees() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerviewAttendees.setLayoutManager(layoutManager);
        recyclerviewAttendees.setHasFixedSize(true);
        attendeeAdapter = new AttendeeAdapter();
        attendeeAdapter.setAttendeesData(event.getAttendees());
        attendeeAdapter.setOnClickHandler(attendee -> {
            Intent userProfileIntent = new Intent(this, UserProfileActivity.class);
            userProfileIntent.putExtra("userDocId", attendee.getUserDocId());
            startActivity(userProfileIntent);
        });
        attendeeAdapter.setContext(this);
        recyclerviewAttendees.setAdapter(attendeeAdapter);
    }
}
