package com.stefan.sklub.Activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.GeoPoint;
import com.stefan.sklub.Database.FirestoreDB;
import com.stefan.sklub.Interfaces.OnAddItem;
import com.stefan.sklub.Model.Event;
import com.stefan.sklub.Model.Place;
import com.stefan.sklub.Model.Sport;
import com.stefan.sklub.Model.User;
import com.stefan.sklub.R;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;

public class AddEventActivity extends BaseActivity {
    private final String TAG = "AddEventActivity ispis";

    private GoogleMap map;
    private User me;
    private EditText et_location;
    private TextInputLayout til_name;
    private TextInputLayout til_sport;
    private TextInputLayout til_description;
    private TextInputLayout til_date_and_time;
    private FirestoreDB firestoreDB;
    private FirebaseAuth mAuth;
    private List<Place> places;
    private List<Marker> markers;
    private LocalDateTime selectedDateAndTime;
    private LocalDate selectedDate;
    private LocalTime selectedTime;
    private Place selectedPlace;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int ACCESS_FINE_LOCATION_REQUEST_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle(R.string.action_bar_add_new_event);

        mAuth = FirebaseAuth.getInstance();
        firestoreDB = FirestoreDB.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (getIntent().hasExtra("me")) {
            me = getIntent().getParcelableExtra("me");
        } else {
            Log.e(TAG, "No app user data");
        }

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

        mapFragment.getMapAsync(googleMap -> {
            this.map = googleMap;
            map.setOnMarkerClickListener(marker -> {
                onPlaceMarkerClick(marker);
                return false;
            });

            moveMapToCurrentLocation();
        });

        String[] menuItems = Sport.getSportNames();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.dropdown_menu_item, menuItems);
        AutoCompleteTextView actw = (AutoCompleteTextView) til_sport.getEditText();
        actw.setAdapter(adapter);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.confirmation_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_action_confirm:
                addEvent();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void moveMapToCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(AddEventActivity.this, new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, ACCESS_FINE_LOCATION_REQUEST_CODE);
        } else {
            map.setMyLocationEnabled(true);
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {

                                LatLng curLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                                map.moveCamera(CameraUpdateFactory.newLatLng(curLatLng));
                                map.animateCamera(CameraUpdateFactory.zoomTo(10));
                                Log.d(TAG, "Map moved to location! :D");

                                firestoreDB.getPlaces(new GeoPoint(curLatLng.latitude, curLatLng.longitude), place -> {
                                    places.add(place);
                                    Marker marker = map.addMarker(new MarkerOptions()
                                            .position(new LatLng(place.getLocation().getLatitude(), place.getLocation().getLongitude()))
                                            .title(place.getName())
                                            .snippet("This is my spot!")
                                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                                    markers.add(marker);

                                });

                            } else {
                                Log.d(TAG, "location is null :(");
                                Toast.makeText(AddEventActivity.this, "Location is null?! :((((", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private void onPlaceMarkerClick(Marker marker) {
        Log.d(TAG, "places.size(): " + places.size());
        Log.d(TAG, "markers.size(): " + markers.size());
        if (markers.indexOf(marker) >= 0) {
            Log.d(TAG, "places matched");
            selectedPlace = places.get(markers.indexOf(marker));
        }

        if (selectedPlace == null) {
            Log.d(TAG, "Selected marker didn't match a place from the list");
        }
    }

    public void onDateTimePick() {
        pickDate();
    }

    private void pickDate() {
        final View dialogView = View.inflate(this, R.layout.dialog_date_picker, null);
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();

        dialogView.findViewById(R.id.date_set).setOnClickListener(view -> {

            DatePicker datePicker = (DatePicker) dialogView.findViewById(R.id.date_picker);

            selectedDate = LocalDate.of(datePicker.getYear(),
                    datePicker.getMonth() + 1,
                    datePicker.getDayOfMonth());

            if (selectedDate.isBefore(LocalDate.now())) {
                Toast.makeText(this, "The date can't be before today.", Toast.LENGTH_SHORT).show();
                selectedDate = null;
            } else {
                alertDialog.dismiss();
                pickTime();
            }
        });
        alertDialog.setView(dialogView);
        alertDialog.show();
    }

    private void pickTime() {
        final View dialogView = View.inflate(this, R.layout.dialog_time_picker, null);
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();

        dialogView.findViewById(R.id.time_set).setOnClickListener(view -> {

            TimePicker timePicker = (TimePicker) dialogView.findViewById(R.id.time_picker);

            selectedTime = LocalTime.of(timePicker.getHour(), timePicker.getMinute());
            selectedDateAndTime = LocalDateTime.of(selectedDate, selectedTime);

            if (selectedDateAndTime.isAfter(LocalDateTime.now())) {
                alertDialog.dismiss();
                til_date_and_time.getEditText().setText(selectedDateAndTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm")));
            } else {
                Toast.makeText(this, "The date and time can't be in the past.", Toast.LENGTH_SHORT).show();
                selectedTime = null;
                selectedDateAndTime = null;
            }
        });
        alertDialog.setView(dialogView);
        alertDialog.show();
    }

    public void addEvent() {

        String event_name = til_name.getEditText().getText().toString();
        String sport_type = til_sport.getEditText().getText().toString();
        String description = til_description.getEditText().getText().toString();

        if (event_name.isEmpty()) {
            Toast.makeText(this, "Event name field can't be empty", Toast.LENGTH_SHORT).show();
            return;
        }
        if (sport_type.isEmpty()) {
            Toast.makeText(this, "Sport type not selected", Toast.LENGTH_SHORT).show();
            return;
        }
        if (description.isEmpty()) {
            Toast.makeText(this, "Event description field can't be empty", Toast.LENGTH_SHORT).show();
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
        event.setDescription(description);
        event.setSport(sport_type);
        event.setDate(selectedDateAndTime);
        event.setOrganiser(me);
        event.setPlace(selectedPlace);

        firestoreDB.addEvent(event, new OnAddItem() {
            @Override
            public void onAdd(String docId) {
                event.setEventDocId(docId);
                Toast.makeText(AddEventActivity.this, "Event successfully added", Toast.LENGTH_SHORT).show();
//                Intent intent = new Intent(AddEventActivity.this, MainActivity.class);
//                intent.putExtra("event added", true);
//                startActivity(intent);
                Intent returnIntent = new Intent();
                returnIntent.putExtra("event", event);
                setResult(3, returnIntent);
                finish();
                // TODO: return to main activity with result (the newly added event)
            }

            @Override
            public void onError(String error) {
                Toast.makeText(AddEventActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}