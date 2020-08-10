package com.stefan.sklub.Activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.GeoPoint;
import com.stefan.sklub.Database.FirestoreDB;
import com.stefan.sklub.Model.Event;
import com.stefan.sklub.Model.Place;
import com.stefan.sklub.Model.Sport;
import com.stefan.sklub.Model.User;
import com.stefan.sklub.R;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;

public class AddEventActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    private final String TAG = "AddEventActivity ispis";

    private GoogleMap map;
    private EditText et_location;
    private TextInputLayout til_name;
    private TextInputLayout til_sport;
    private TextInputLayout til_description;
    private TextInputLayout til_date_and_time;
    private FirestoreDB firestoreDB;
    private FirebaseAuth mAuth;
    private List<Place> places;
    private List<Marker> markers;
    private Dictionary<Marker, Place> markerPlaceDictionary;
    private LocalDateTime selectedDateAndTime;
    private Place selectedPlace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        getSupportActionBar().setTitle("Add new event");

        mAuth = FirebaseAuth.getInstance();
        firestoreDB = FirestoreDB.getInstance();
//        firestoreDB = new FirestoreDB();
        til_name = findViewById(R.id.til_event_name);
        til_sport = findViewById(R.id.til_sport_dropdown);
        til_description = findViewById(R.id.til_event_description);
        til_date_and_time = findViewById(R.id.til_event_date_and_time);
        til_date_and_time.setEndIconOnClickListener(view -> {
            onDateTimePick();
        });
        places = new ArrayList<>();
        markers = new ArrayList<>();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        String[] menuItems = Sport.getSportNames();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.list_item, menuItems);
        AutoCompleteTextView actw = (AutoCompleteTextView) til_sport.getEditText();
        actw.setAdapter(adapter);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.confirmation_menu, menu);
        getSupportActionBar().setTitle("Add new event");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_action_confirm:
                addEvent();
                return true;
            default:
                Toast.makeText(this, "Settings item clicked", Toast.LENGTH_SHORT).show();
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        this.map = map;
        map.setOnMarkerClickListener(this);

        firestoreDB.getPlaces(null, places -> {
            this.places = places;
            for (Place place : places) {
                Marker marker = map.addMarker(new MarkerOptions()
                        .position(new LatLng(place.getLocation().getLatitude(), place.getLocation().getLongitude()))
                        .title(place.getName())
                        .snippet("This is my spot!")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                markers.add(marker);
            }
        });
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Log.d(TAG, "places.size(): " + places.size());
        Log.d(TAG, "markers.size(): " + markers.size());
        if (markers.indexOf(marker) >= 0) {
            Log.d(TAG, "places matched");
            selectedPlace = places.get(markers.indexOf(marker));
        }

//        for (Place place : places) {
//            if (place.getLocationAsLatLng().equals(marker.getPosition())) {
//                Log.d(TAG, "places matched");
//                selectedPlace = place;
//            }
//        }

        if (selectedPlace == null) {
            Log.d(TAG, "Selected marker didn't match a place from the list");
        }
        return false;
    }


    public void onDateTimePick() {
        final View dialogView = View.inflate(this, R.layout.date_time_picker, null);
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();

        dialogView.findViewById(R.id.date_time_set).setOnClickListener((View.OnClickListener) view -> {

            DatePicker datePicker = (DatePicker) dialogView.findViewById(R.id.date_picker);
            TimePicker timePicker = (TimePicker) dialogView.findViewById(R.id.time_picker);

            selectedDateAndTime = LocalDateTime.of(datePicker.getYear(),
                    datePicker.getMonth(),
                    datePicker.getDayOfMonth(),
                    timePicker.getHour(),
                    timePicker.getMinute());

            alertDialog.dismiss();
            til_date_and_time.getEditText().setText(selectedDateAndTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm")));
        });
        alertDialog.setView(dialogView);
        alertDialog.show();
    }

    public void addEvent() {

        String event_name = til_name.getEditText().getText().toString();
        String sport_type = til_sport.getEditText().getText().toString();

        if (event_name.isEmpty()) {
            Toast.makeText(this, "Event name field can't be empty", Toast.LENGTH_SHORT).show();
            return;
        }
        if (sport_type.isEmpty()) {
            Toast.makeText(this, "Sport type not selected", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedDateAndTime == null) {
            Toast.makeText(this, "Date and time not selected", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedPlace == null) {
            Toast.makeText(this, "Place not selected", Toast.LENGTH_SHORT).show();
            return;
        }

        Event event = new Event();
        event.setName(event_name);
        event.setSport(sport_type);
        event.setDate(selectedDateAndTime);
        event.setOrganiser(new User(mAuth.getCurrentUser().getUid()));
        event.setPlace(selectedPlace);

        firestoreDB.addEvent(event, () -> {
            Toast.makeText(this, "Event successfully added", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(AddEventActivity.this, MainActivity.class);
            intent.putExtra("event added", true);
            startActivity(intent);
            finish();
        });
    }
}