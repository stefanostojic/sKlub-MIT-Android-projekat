package com.stefan.sklub.activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.stefan.sklub.Event;
import com.stefan.sklub.adapters.EventAdapter;
import com.stefan.sklub.Place;
import com.stefan.sklub.R;
import com.stefan.sklub.User;

import java.io.File;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.annotation.Nonnull;

public class MainActivity extends AppCompatActivity implements EventAdapter.EventAdapterOnClickHandler {

    private RecyclerView mRecyclerView;
    private EventAdapter mEventAdapter;
    final List<Event> onlineEventList = new ArrayList<Event>();

    final String TAG = "ispis";
    private StorageReference mStorageRef;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        db = FirebaseFirestore.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();

//        getSupportActionBar().hide();
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "raspored.jpg");
        Log.i(TAG, file.exists() + "");
        requestPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, 3);

        mStorageRef = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        loadRecycleView();
    }

    private final int[] totalOfObjectsToLoad = new int[1];
    private final int[] counterOfPreparedObjectsToLoad = new int[1];

    private void loadRecycleView() {
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_event);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);
        mEventAdapter = new EventAdapter(this);
        mRecyclerView.setAdapter(mEventAdapter);
//        mRecyclerView.setNestedScrollingEnabled(false);

        db.collection("events").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> fetchEventTask) {
                        if (fetchEventTask.isSuccessful()) {
                            for (final QueryDocumentSnapshot eventDocument : fetchEventTask.getResult()) {
                                totalOfObjectsToLoad[0] = fetchEventTask.getResult().size();
                                Event event = new Event();
                                event.setName((String) eventDocument.get("name"));
                                event.setEventId(eventDocument.getId());
                                event.setDate((Timestamp) eventDocument.get("date"));
                                event.setOrganiser(new User());
                                ((DocumentReference) eventDocument.get("organiser")).get()
                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> fetchUserTask) {
                                                if (fetchUserTask.isSuccessful()) {
                                                    DocumentSnapshot userDocumentSnapshot = fetchUserTask.getResult();
                                                    if (userDocumentSnapshot.exists()) {
                                                        event.getOrganiser().setFirstname((String) userDocumentSnapshot.get("firstname"));
                                                        event.getOrganiser().setLastname((String) userDocumentSnapshot.get("lastname"));
                                                        event.getOrganiser().setBirthday((Timestamp) userDocumentSnapshot.get("birthday"));
                                                        event.getOrganiser().setLocation((GeoPoint) userDocumentSnapshot.get("location"));
                                                        mStorageRef.child((String) userDocumentSnapshot.get("img")).getDownloadUrl()
                                                                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                                    @Override
                                                                    public void onSuccess(Uri uri) {
                                                                        event.getOrganiser().setImgUri(uri.toString());
                                                                        ((DocumentReference) eventDocument.get("place")).get()
                                                                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<DocumentSnapshot> fetchPlaceTask) {
                                                                                        if (fetchPlaceTask.isSuccessful()) {
                                                                                            DocumentSnapshot eventDocumentSnapshot = fetchPlaceTask.getResult();
                                                                                            if (eventDocumentSnapshot.exists()) {
                                                                                                event.getPlace().setName((String) eventDocumentSnapshot.get("name"));
                                                                                                event.getPlace().setLocation((GeoPoint) eventDocumentSnapshot.get("location"));
                                                                                                mStorageRef.child((String) eventDocumentSnapshot.get("img")).getDownloadUrl()
                                                                                                        .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                                                                            @Override
                                                                                                            public void onSuccess(Uri uri) {
                                                                                                                event.getPlace().setImgUri(uri.toString());
                                                                                                                onlineEventList.add(event);

                                                                                                                counterOfPreparedObjectsToLoad[0]++;
                                                                                                                if (counterOfPreparedObjectsToLoad[0] == totalOfObjectsToLoad[0]) {
                                                                                                                    Event[] eventsArray = new Event[onlineEventList.size()];
                                                                                                                    eventsArray = onlineEventList.toArray(eventsArray);
                                                                                                                    mEventAdapter.setContext(getApplicationContext());
                                                                                                                    mEventAdapter.setEventsData(eventsArray);
                                                                                                                }
                                                                                                            }
                                                                                                        }).addOnFailureListener(new OnFailureListener() {
                                                                                                    @Override
                                                                                                    public void onFailure(@NonNull Exception exception) {
                                                                                                        Log.d(TAG, "event.place.img.getDownloadUrl() error");
                                                                                                    }
                                                                                                });
                                                                                            } else {
                                                                                                Log.d(TAG, "Place not found");
                                                                                            }
                                                                                        } else {
                                                                                            Log.d(TAG, "get failed with ", fetchPlaceTask.getException());
                                                                                        }
                                                                                    }
                                                                                });
                                                                    }
                                                                }).addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception exception) {
                                                                Log.d(TAG, "event.organiser.img.getDownloadUrl() error");
                                                            }
                                                        });

                                                    } else {
                                                        Log.d(TAG, "User not found");
                                                    }
                                                } else {
                                                    Log.d(TAG, "get failed with ", fetchUserTask.getException());
                                                }
                                            }
                                        });
                            }
                        } else {
                            Log.d(TAG, "Error getting event documents: ", fetchEventTask.getException());
                        }
                    }
                });
    }

//    private void loadRecycleViewV2() {
//        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_event);
//        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
//        mRecyclerView.setLayoutManager(layoutManager);
//        mRecyclerView.setHasFixedSize(true);
//        mEventAdapter = new EventAdapter(this);
//        mRecyclerView.setAdapter(mEventAdapter);
//
//
//
//        // refactoring the crap out of it
//        db.collection("events").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                @Override
//                public void onComplete(@NonNull Task<QuerySnapshot> fetchEventTask) {
//                    if (fetchEventTask.isSuccessful()) {
//                        for (final QueryDocumentSnapshot eventDocument : fetchEventTask.getResult()) {
//                            totalOfObjectsToLoad[0] = fetchEventTask.getResult().size();
//                            Event[] eventArrayForRecycleView = new Event[fetchEventTask.getResult().size()];
//                            int positionInEventArrayForRecycleView = 0;
//                            final String eventId = eventDocument.getId();
//                            final String eventName = eventDocument.get("name").toString();
//                            final Date eventDate = ((Timestamp) eventDocument.get("date")).toDate();
//                            eventArrayForRecycleView[positionInEventArrayForRecycleView] = new Event(eventId, eventName, new User(), eventDate, new Place());
//
//                            if (mAuth.getCurrentUser() != null)
//                                Log.d(TAG, "mAuth.getCurrentUser().getEmail(): " + mAuth.getCurrentUser().getEmail());
//
//                            fetchUserData(eventDocument, eventArrayForRecycleView, positionInEventArrayForRecycleView);
//                        }
//                    } else {
//                        Log.d(TAG, "Error getting event documents: ", fetchEventTask.getException());
//                    }
//                }
//        });
//    }

//    private void fetchUserData(final DocumentSnapshot eventDocument, final Event[] eventArrayForRecycleView, final int positionInEventArrayForRecycleView) {
//        DocumentReference userDocRef = (DocumentReference) eventDocument.get("organiser");
//        userDocRef.get()
//                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<DocumentSnapshot> fetchUserTask) {
//                        if (fetchUserTask.isSuccessful()) {
//                            DocumentSnapshot userDocumentSnapshot = fetchUserTask.getResult();
//                            if (userDocumentSnapshot.exists()) {
//                                eventArrayForRecycleView[positionInEventArrayForRecycleView].organiser.firstname = (String) userDocumentSnapshot.get("firstname");
//        //                                        organiser.lastname = (String) userDocumentSnapshot.get("lastname");
//                                mStorageRef.child((String) userDocumentSnapshot.get("img")).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//                                    @Override
//                                    public void onSuccess(Uri uri) {
//                                        eventArrayForRecycleView[positionInEventArrayForRecycleView].organiser.imgUri = uri.toString();
//
//                                        fetchPlaceData(eventDocument, eventArrayForRecycleView, positionInEventArrayForRecycleView);
//                                    }
//                                }).addOnFailureListener(new OnFailureListener() {
//                                    @Override
//                                    public void onFailure(@NonNull Exception exception) {
//                                        Log.d(TAG, "getDownloadUrl() error");
//                                    }
//                                });
//
//                            } else {
//                                Log.d(TAG, "User not found");
//                            }
//                        } else {
//                            Log.d(TAG, "get failed with ", fetchUserTask.getException());
//                        }
//                    }
//                });
//    }
//
//    private void fetchPlaceData(DocumentSnapshot eventDocument, final Event[] eventArrayForRecycleView, final int positionInEventArrayForRecycleView) {
//        DocumentReference placeDocRef = (DocumentReference) eventDocument.get("place");
//        placeDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                @Override
//                public void onComplete(@NonNull Task<DocumentSnapshot> fetchPlaceTask) {
//                    if (fetchPlaceTask.isSuccessful()) {
//                        DocumentSnapshot eventDocumentSnapshot = fetchPlaceTask.getResult();
//                        if (eventDocumentSnapshot.exists()) {
//                            eventArrayForRecycleView[positionInEventArrayForRecycleView].place.name = (String) eventDocumentSnapshot.get("name");
//                            GeoPoint geoPoint = (GeoPoint) eventDocumentSnapshot.get("location");
//                            eventArrayForRecycleView[positionInEventArrayForRecycleView].place.location = new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude());
//
//                            mStorageRef.child((String) eventDocumentSnapshot.get("img")).getDownloadUrl()
//                                    .addOnSuccessListener(new OnSuccessListener<Uri>() {
//                                        @Override
//                                        public void onSuccess(Uri uri) {
//                                            eventArrayForRecycleView[positionInEventArrayForRecycleView].place.imgUri = uri.toString();
//                                            maybeLoadIntoEventAdapter();
//                                        }
//                                    }).addOnFailureListener(new OnFailureListener() {
//                                    @Override
//                                    public void onFailure(@NonNull Exception exception) {
//                                        Log.d(TAG, "event.place.img.getDownloadUrl(): error");
//                                    }
//                            });
//                        } else {
//                            Log.d(TAG, "Place not found");
//                        }
//                    } else {
//                        Log.d(TAG, "get failed with ", fetchPlaceTask.getException());
//                    }
//                }
//        });
//    }
//
//    private void maybeLoadIntoEventAdapter() {
//        counterOfPreparedObjectsToLoad[0]++;
//        if (counterOfPreparedObjectsToLoad[0] == totalOfObjectsToLoad[0]) {
//            Event[] eventsArray = new Event[onlineEventList.size()];
//            eventsArray = onlineEventList.toArray(eventsArray);
//            mEventAdapter.setContext(getApplicationContext());
//            mEventAdapter.setEventsData(eventsArray);
//        }
//    }

    private void signOut() {
        Log.i(TAG, "signOut() klik!");
        mAuth.signOut();
        this.onDestroy();
    }

    @Override
    public void onDestroy() {
        mAuth.signOut();

        super.onDestroy();
    }

    public void onBtnSignOutClick(View view) {
        mAuth.signOut();
        Intent loginIntent = new Intent(this, LoginActivity.class);
        startActivity(loginIntent);
        finish();
    }

    public void onBtnUpload(View view) {
//        Uri file = Uri.fromFile(new File("path/to/images/rivers.jpg"));
        Uri file = Uri.fromFile(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "raspored.jpg"));
        Log.i(TAG, file.getPath());
        StorageReference riversRef = mStorageRef.child("images/raspored.jpg");

        riversRef.putFile(file)
        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.i(TAG, "Upload slike uspesan :)");
                // Get a URL to the uploaded content
//                Uri downloadUrl = taskSnapshot.getMetadata().getPath();
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.i(TAG, "Upload slike prop'o");
                Log.i(TAG, exception.getCause().toString());
                Log.i(TAG, exception.getMessage());
                Log.i(TAG, exception.getStackTrace().toString());
                // Handle unsuccessful uploads
                // ...
            }
        });
    }

    public void onBtnAddEvent(View view) {
        Intent addEventIntent = new Intent(MainActivity.this, AddEventActivity.class);
        startActivity(addEventIntent);
    }

    @Override
    public void onClick(String eventName) {

    }

}
