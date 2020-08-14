package com.stefan.sklub.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.features.ReturnMode;
import com.esafirm.imagepicker.model.Image;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.stefan.sklub.Database.FirestoreDB;
import com.stefan.sklub.Model.Place;
import com.stefan.sklub.R;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class AddPlaceActivity extends BaseActivity {
    private final String TAG = "AddPlaceActivity ispis";

    private GoogleMap map;
    private TextInputLayout tyl_name;
    private FirestoreDB firestoreDB;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int ACCESS_FINE_LOCATION_REQUEST_CODE = 101;
    private static final int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 7502;

    private ImageView iv_img;
    private LatLng selectedLocation;
    private boolean isImgSet;
    private Bitmap imgBitmap;
    private ArrayList<Image> images = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_place);

        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle("Add new place");
        tyl_name = findViewById(R.id.tf_place_name);
        iv_img = (ImageView) findViewById(R.id.iv_img);

        firestoreDB = FirestoreDB.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment_add_place);

        mapFragment.getMapAsync(googleMap -> {
            this.map = googleMap;
            map.setOnMapLongClickListener(latLngPoint -> {
                putLocationMarker(latLngPoint);
            });
            moveMapToCurrentLocation();
        });
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
                addPlace();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void addImg(View view) {
        ImagePicker imagePicker = ImagePicker.create(this)
                .folderMode(true) // set folder mode (false by default)
                .toolbarArrowColor(Color.WHITE) // set toolbar arrow up color
                .toolbarFolderTitle("Folder") // folder selection title
                .toolbarImageTitle("Tap to select") // image selection title
                .toolbarDoneButtonText("Done"); // done button text
        imagePicker.single();
        imagePicker.start();
    }

    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (ImagePicker.shouldHandle(requestCode, resultCode, data)) {
            images = (ArrayList<Image>) ImagePicker.getImages(data);
//            printImages(images);
            RequestOptions options = new RequestOptions()
                    .centerCrop()
                    .placeholder(R.drawable.ic_person_black_18dp)
                    .error(R.drawable.ic_person_black_18dp);

            Bitmap bmp1 = BitmapFactory.decodeFile(images.get(0).getPath());

            int x = 0;
            int height = bmp1.getHeight();
            int width = bmp1.getWidth();
            int startX = 0;
            int startY = 0;
            if ((double)width / height > 1.5) {
                // landscape img
                x = height / 2;
                width = 3 * x;
                startX = (bmp1.getWidth() - width) / 2;
            } else {
                // portrait img
                x = width / 3;
                height = x * 2;
                startY = (bmp1.getHeight() - height) / 2;
            }

            Bitmap bitmap2 = Bitmap.createBitmap(bmp1, startX, startY, width, height);
            Bitmap bitmap3 = Bitmap.createScaledBitmap(bitmap2, 600, 400, false);
            imgBitmap = bitmap3;
            iv_img.setPadding(0, 0, 0, 0);
            Glide.with(this)
                    .load(bitmap3)
                    .apply(options)
                    .into(iv_img);
        }
    }

    private void squareCropBitmap(Bitmap bmp1) {
        int sideLength = 0;
        int height = bmp1.getHeight();
        int width = bmp1.getWidth();
        int startX = 0;
        int startY = 0;
        if ((double)width / height > 1.5) {
            // landscape img
            sideLength = height;
            startX = (width - sideLength) / 2;
        } else {
            // portrait img
            sideLength = width;
            startY = (height - sideLength) / 2;
        }
    }

    private void addPlace() {
        if (tyl_name.getEditText().getText().toString().isEmpty()) {
            Toast.makeText(this, "Name field can't be empty", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedLocation == null) {
            Toast.makeText(this, "Location not set", Toast.LENGTH_SHORT).show();
            return;
        }
        if (isImgSet) {
            Toast.makeText(this, "Image not set", Toast.LENGTH_SHORT).show();
            return;
        }

        Place newPlace = new Place();
        newPlace.setName(tyl_name.getEditText().getText().toString());
        newPlace.setLocation(selectedLocation);

//        iv_img.setDrawingCacheEnabled(true);
//        iv_img.buildDrawingCache();
//        Bitmap bitmap = ((BitmapDrawable) iv_img.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        imgBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

        firestoreDB.addPlace(newPlace, baos.toByteArray(), () -> {
            Toast.makeText(this, "Place successfully added", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void moveMapToCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(AddPlaceActivity.this, new String[]{ Manifest.permission.ACCESS_FINE_LOCATION }, ACCESS_FINE_LOCATION_REQUEST_CODE);
        } else {
            map.setMyLocationEnabled(true);
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {

                                LatLng cur_Latlng = new LatLng(location.getLatitude(), location.getLongitude());
                                map.moveCamera(CameraUpdateFactory.newLatLng(cur_Latlng));
                                map.animateCamera(CameraUpdateFactory.zoomTo(10));
                                Log.d(TAG, "Map moved to location! :D");

                            } else {
                                Log.d(TAG, "location is null :(");
                                Toast.makeText(AddPlaceActivity.this, "Location is null?! :((((", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private void putLocationMarker(LatLng latLngPoint) {
        selectedLocation = latLngPoint;
        map.clear();
        map.addMarker(new MarkerOptions().position(latLngPoint).title("Selected location")).showInfoWindow();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == ACCESS_FINE_LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "ACCESS_FINE_LOCATION: Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "ACCESS_FINE_LOCATION: Denied", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == WRITE_EXTERNAL_STORAGE_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "WRITE_EXTERNAL_STORAGE: Granted", Toast.LENGTH_SHORT).show();
//                easyImage.openGallery(AddPlaceActivity.this);
            } else {
                Toast.makeText(this, "WRITE_EXTERNAL_STORAGE: Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }


}