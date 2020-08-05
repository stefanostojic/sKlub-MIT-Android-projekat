package com.stefan.sklub.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.stefan.sklub.R;

public class AddEventActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private GoogleMap map;
    private EditText etLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);
        Log.i("AddEventActivityTag", "mapFragment napokon vise nije null :)");

        etLocation = (EditText) findViewById(R.id.etLocation);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        this.map = map;
        map.setOnMapLongClickListener(this);
    }

    @Override
    public void onMapLongClick(LatLng point) {
        etLocation.setText(point.toString());
        map.addMarker(new MarkerOptions().position(point).alpha(0.8f).title("Selected location")).showInfoWindow();
    }
}