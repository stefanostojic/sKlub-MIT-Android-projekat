package com.stefan.sklub.Activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.stefan.sklub.Adapters.AttendeeAdapter;
import com.stefan.sklub.Adapters.EventAdapter;
import com.stefan.sklub.Adapters.UserAdapter;
import com.stefan.sklub.Database.FirestoreDB;
import com.stefan.sklub.Model.Attendee;
import com.stefan.sklub.Model.Event;
import com.stefan.sklub.Model.Sport;
import com.stefan.sklub.Model.User;
import com.stefan.sklub.R;

import java.time.format.DateTimeFormatter;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EventDetailsActivity extends BaseActivity {

    @BindView(R.id.tv_event_name)
    TextView tv_event_name;
    @BindView(R.id.tv_event_organiser_firstname)
    TextView tv_event_organiser_firstname;
    @BindView(R.id.tv_event_organiser_lastname)
    TextView tv_event_organiser_lastname;
    @BindView(R.id.tv_event_sport_name)
    TextView tv_event_sport_name;
    @BindView(R.id.tv_event_description)
    TextView tv_event_description;
    @BindView(R.id.iv_event)
    ImageView iv_event;
    @BindView(R.id.iv_event_organiser)
    ImageView iv_event_organiser;
    @BindView(R.id.iv_event_sport)
    ImageView iv_event_sport;
    @BindView(R.id.tv_time2)
    TextView tv_time;
    @BindView(R.id.tv_date2)
    TextView tv_date;
    @BindView(R.id.tv_place2)
    TextView tv_place;
    @BindView(R.id.btn_attendance)
    Button btn_attendance;
    @BindView(R.id.btn_attendance2)
    Button btnOtherAttendees;

//    @BindView(R.id.recyclerview_attendees)
//    RecyclerView attendeesRecyclerView;

    private ShareActionProvider actionProvider;
    private FirestoreDB db;
    private AttendeeAdapter attendeesAdapter;

    private Event event;
    private User me;
    private boolean imAttending;
    private final String TAG = "EventDetailsActivity ispis";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        setContentView(R.layout.activity_event_details);
        ButterKnife.bind(this);
        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle("Event details");
        db = FirestoreDB.getInstance();

        if (getIntent().hasExtra("event")) {
            event = getIntent().getParcelableExtra("event");
            Log.d(TAG, "Received event: " + event);
        } else {
            Log.e(TAG, "No event data");
        }
        if (getIntent().hasExtra("me")) {
            me = getIntent().getParcelableExtra("me");
            Log.d(TAG, "Received User(Me) data: " + me);
        } else {
            Log.e(TAG, "No app user data");
        }

        if (event != null && me != null)
            fillEventDetailsData();

        if (event.getOrganiser().getUserDocId().equals(me.getUserDocId())) {
            btn_attendance.setOnClickListener(null);
            btn_attendance.setText("I'm\nattending!");
            btn_attendance.setBackgroundColor(getResources().getColor(R.color.blue_colorPrimary));
            imAttending = true;
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.details_menu, menu);
//        MenuItem item = menu.findItem(R.id.menu_action_share);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_action_comments:
                Intent commentsActivityIntent = new Intent(this, CommentsActivity.class);
                commentsActivityIntent.putExtra("event", event);
                commentsActivityIntent.putExtra("me", me);
                startActivityForResult(commentsActivityIntent, 3);
                return true;
            case R.id.menu_action_share:
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, "Hey! Check out this cool event: " + event.getName());
                startActivity(Intent.createChooser(shareIntent, "Share"));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void fillEventDetailsData() {
//        loadAttendees();

        tv_event_name.setText(event.getName());
        tv_event_organiser_firstname.setText(event.getOrganiser().getFirstName());
        tv_event_organiser_lastname.setText(event.getOrganiser().getLastName());
        if (event.getDescription() != null)
            tv_event_description.setText(event.getDescription());
        tv_event_sport_name.setText(event.getSport());
        tv_time.setText(event.getDate().format(DateTimeFormatter.ofPattern("HH:mm")));
        tv_date.setText(event.getDate().format(DateTimeFormatter.ofPattern("dd.MM.")));
        tv_place.setText(event.getPlace().getName());

        imAttending = false;
        btn_attendance.setBackgroundColor(Color.LTGRAY);
        for (Attendee attendee : event.getAttendees()) {
            if (attendee.getUserDocId().equals(me.getUserDocId())) {
                imAttending = true;
                btn_attendance.setBackgroundColor(getResources().getColor(R.color.blue_colorPrimary));
                btn_attendance.setText("I'm\nattending!");
            }
        }
        setBtnOtherAttendees();

        RequestOptions options = new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.ic_person_black_18dp)
                .error(R.drawable.ic_person_black_18dp)
                .circleCrop();

        Glide.with(this)
                .load(event.getPlace().getImgUrl())
                .into(iv_event);
        Glide.with(this)
                .load(event.getOrganiser().getImgUrl())
                .apply(options)
                .into(iv_event_organiser);
        Glide.with(this)
                .load(Sport.getImgUri(event.getSport()))
                .into(iv_event_sport);
    }

    private void putMeOnTop() {
        int index = 0;
        for (Attendee attendee : event.getAttendees()) {
            if (attendee.getUserDocId().equals(me.getUserDocId())) {
                break;
            } else {
                index++;
            }
        }
        Attendee attendeeMe = event.getAttendees().get(index);
        event.getAttendees().remove(index);
        event.getAttendees().add(0, attendeeMe);
    }

    public void onBtnAttendanceClick(View view) {
        if (imAttending) {
            btn_attendance.setText("I'm\nattending?");
            btn_attendance.setBackgroundColor(Color.LTGRAY);
            imAttending = false;
            db.removeEventAttendence(event, me);
//            attendeesAdapter.removeMe(me);
            int index = 0;
            for (Attendee attendee : event.getAttendees()) {
                if (attendee.getUserDocId().equals(me.getUserDocId())) {
                    break;
                } else {
                    index++;
                }
            }
            event.getAttendees().remove(index);
        } else {
            btn_attendance.setText("I'm\nattending!");
            btn_attendance.setBackgroundColor(getResources().getColor(R.color.blue_colorPrimary));
            imAttending = true;
            db.addEventAttendence(event, me);
//            attendeesAdapter.addMe(me);
            event.getAttendees().add(convertUserToAttendee(me));
        }
        setBtnOtherAttendees();
    }

    private void setBtnOtherAttendees() {
        if (event.getAttendees().size() == 0) {
            btnOtherAttendees.setVisibility(View.INVISIBLE);
        } else if (event.getAttendees().size() == 1 && imAttending) {
            btnOtherAttendees.setVisibility(View.INVISIBLE);
        } else if (event.getAttendees().size() == 1 && !imAttending) {
            btnOtherAttendees.setVisibility(View.VISIBLE);
            String text = "See others (" + event.getAttendees().size() + ")";
            btnOtherAttendees.setText(text);
        } else if (event.getAttendees().size() > 1 && imAttending) {
            btnOtherAttendees.setVisibility(View.VISIBLE);
            String text = "See others (" + (event.getAttendees().size() - 1) + ")";
            btnOtherAttendees.setText(text);
        } else if (event.getAttendees().size() > 1 && !imAttending) {
            btnOtherAttendees.setVisibility(View.VISIBLE);
            String text = "See others (" + event.getAttendees().size() + ")";
            btnOtherAttendees.setText(text);
        }
    }

    public void onOrganiserClick(View view) {
        Intent userDetailsIntent = new Intent(this, UserProfileActivity.class);
        userDetailsIntent.putExtra("userDocId", event.getOrganiser().getUserDocId());
        startActivity(userDetailsIntent);
    }

    public void seeOtherAttendees(View view) {
        Intent eventAttendeesIntent = new Intent(this, EventAttendeesActivity.class);
        eventAttendeesIntent.putExtra("event", event);
        startActivity(eventAttendeesIntent);
    }

    private Attendee convertUserToAttendee(User user) {
        Attendee attendee = new Attendee();
        attendee.setUserDocId(user.getUserDocId());
        attendee.setFirstName(user.getFirstName());
        attendee.setLastName(user.getLastName());
        attendee.setImgUrl(user.getImgUrl());
        return  attendee;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        Log.d(TAG, "onActivityResult()");
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode == 3) {
//            if (requestCode == 3) {
                if (intent.hasExtra("me")) {
                    me = intent.getParcelableExtra("me");
                    Log.i(TAG, "onActivityResult(): User data received successfully");
                } else {
                    Log.e(TAG, "onActivityResult(): User data not received");
                }
                if (intent.hasExtra("event")) {
                    event = intent.getParcelableExtra("event");
                    Log.i(TAG, "onActivityResult(): Event data received successfully");
                    fillEventDetailsData();
                } else {
                    Log.e(TAG, "onActivityResult(): Event data not received");
                }
//            }
        }
    }
}