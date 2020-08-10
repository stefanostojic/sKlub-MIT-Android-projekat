package com.stefan.sklub.Database;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.stefan.sklub.Adapters.EventAdapter;
import com.stefan.sklub.Model.Event;
import com.stefan.sklub.Model.Place;
import com.stefan.sklub.Model.User;

import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirestoreDB {
    private static FirestoreDB instance;
    final String TAG = "FirestoreDB ispis";
    private StorageReference mStorageRef;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private EventAdapter mEventAdapter;

    public FirestoreDB() {
        db = FirebaseFirestore.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
    }

    public static FirestoreDB getInstance(){
        if (instance == null) {
            instance = new FirestoreDB();
        }
        return instance;
    }

    // Interfaces

    // Interface: A Special One

    public interface OnGetListener<T> {
        void onGet(T result);
    }

    // Interfaces: Event

    public interface OnAddEventListener {
        void onAddEvent();
    }

    public interface OnGetEventListener {
        void onGetEvent(Event event);
    }

    // Interfaces: Place

    public interface OnAddPlaceListener {
        void onAddPlace();
    }

    public interface OnGetPlaceListener {
        void onGetPlace(Place place);
    }

    public interface OnGetPlacesListener {
        void onGetPlaces(List<Place> places);
    }

    // Interfaces: User

    public interface OnAddUserListener {
        void onAddUser();
    }

    public interface OnGetUserListener {
        void onGetUser(User user);
    }

    // Interfaces: Firebase Storage

    public interface OnGetStorageDownloadUrlListener {
        void onGetStorageDownloadUrl(String url);
    }

    // CRUD methods for: Events, Users, Places

    // CRUD: Event

    public void addEvent(Event event, OnAddEventListener callback) {

        db.collection("events")
                .add(getEventMap(event))
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "DocumentSnapshot written with ID: " + documentReference.getId());
                        callback.onAddEvent();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });
    }

    public void getEvents(OnGetEventListener onGetEventListener) {
        Log.d(TAG, "Getting events...");
        db.collection("events").orderBy("date").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> fetchEventTask) {
                        if (fetchEventTask.isSuccessful() && fetchEventTask.getResult() != null) {
                            for (final QueryDocumentSnapshot eventDocument : fetchEventTask.getResult()) {
                                Event event = new Event();
                                event.setEventId(eventDocument.getId());
                                event.setName((String) eventDocument.get("name"));
                                event.setDate((Timestamp) eventDocument.get("date"));
                                event.setSport((String) eventDocument.get("sport"));

                                getUserProfile((DocumentReference) eventDocument.get("organiser"), user -> {
                                    event.setOrganiser(user);
                                    getPlace((DocumentReference) eventDocument.get("place"), (OnGetListener<Place>) place -> {
                                        event.setPlace(place);
                                        onGetEventListener.onGetEvent(event);
                                    });
                                });
                            }
                        } else {
                            Log.d(TAG, "Error getting event documents: ", fetchEventTask.getException());
                        }
                    }
                });
    }

    // CRUD: User

    public void addUser(User user, OnAddUserListener onAddUserListener) {
        Map<String, Object> newUserMap = new HashMap<String, Object>();
        newUserMap.put("userUid", user.getUserUid());
        newUserMap.put("firstname", user.getFirstname());
        newUserMap.put("lastname", user.getLastname());
        newUserMap.put("birthday", localDateToTimestamp(user.getBirthday()));
        newUserMap.put("gender", user.getGender());

        db.collection("users")
                .add(newUserMap)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "DocumentSnapshot written with ID: " + documentReference.getId());
                        onAddUserListener.onAddUser();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });
    }

    public void getUserProfile(String userUid, OnGetUserListener callback) {
        db.collection("users").whereEqualTo("userUid", userUid).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult() != null && task.getResult().size() > 0) {
                            DocumentSnapshot userDocumentSnapshot = task.getResult().getDocuments().get(0);
                            User user = new User();
                            user.setFirstname((String) userDocumentSnapshot.get("firstname"));
                            user.setLastname((String) userDocumentSnapshot.get("lastname"));
                            user.setBirthday((Timestamp) userDocumentSnapshot.get("birthday"));
                            user.setImgUri((String) userDocumentSnapshot.get("img"));

                            getStorageDownloadUrl((String) userDocumentSnapshot.get("img"), url -> {
                                user.setImgUri(url.toString());
                                callback.onGetUser(user);
                            });

                        }
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                });
    }

    public void getUserProfile(DocumentReference userDocRef, OnGetUserListener callback) {
        Log.d(TAG, "Getting user profile...");

        userDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());

                        User user = new User();
                        user.setFirstname((String) document.get("firstname"));
                        user.setLastname((String) document.get("lastname"));
                        user.setBirthday((Timestamp) document.get("birthday"));
                        user.setLocation((GeoPoint) document.get("location"));
                        user.setImgUri((String) document.get("img"));

                        getStorageDownloadUrl((String) document.get("img"), url -> {
                            user.setImgUri(url);
                            callback.onGetUser(user);
                        });

                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    // CRUD: Places

    public void addPlace(Place place, byte[] data, OnAddPlaceListener onAddPlaceListener) {
        Map<String, Object> newPlaceMap = new HashMap<String, Object>();
        newPlaceMap.put("name", place.getName());
        newPlaceMap.put("location", place.getLocation());
        newPlaceMap.put("imgUri", "places/" + place.getName() + ".jpg");

        StorageReference newImageRef = mStorageRef.child("places/" + place.getName() + ".jpg");

        UploadTask uploadTask = newImageRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.d(TAG, "Image upload failed");
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.d(TAG, "Image upload successfull");

                db.collection("places")
                        .add(newPlaceMap)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Log.d(TAG, "DocumentSnapshot written with ID: " + documentReference.getId());
                                onAddPlaceListener.onAddPlace();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Error adding document", e);
                            }
                        });
            }
        });
    }

    public void getPlace(String documentPath, OnGetListener listener) {
        db.collection("places").document(documentPath).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Log.d(TAG, "DocumentSnapshot data: " + document.getData());

                                listener.onGet(document);

                            } else {
                                Log.d(TAG, "No such document");
                            }
                        } else {
                            Log.d(TAG, "get failed with ", task.getException());
                        }
                    }
        });
    }

    public void getPlace(DocumentReference placeDocRef, OnGetListener listener) {
        placeDocRef.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Log.d(TAG, "DocumentSnapshot data: " + document.getData());

                                Place place = new Place();
                                place.setPlaceDocId(document.getId());
                                place.setName((String) document.get("name"));
                                place.setLocation((GeoPoint) document.get("location"));
                                getStorageDownloadUrl((String) document.get("img"), url -> {
                                    place.setImgUri(url);

                                    listener.onGet(place);
                                });
                            } else {
                                Log.d(TAG, "No such document");
                            }
                        } else {
                            Log.d(TAG, "get failed with ", task.getException());
                        }
                    }
                });
    }

    public void getPlaces(String queryParameter, OnGetPlacesListener callback) {
        List<Place> places = new ArrayList<>();
        db.collection("places").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult() != null && task.getResult().size() > 0) {
                                for (final QueryDocumentSnapshot placeDocument : task.getResult()) {
                                    Place place = new Place();
                                    place.setName((String) placeDocument.get("name"));
                                    place.setImgUri((String) placeDocument.get("img"));
                                    place.setLocation((GeoPoint) placeDocument.get("location"));
                                    places.add(place);
                                }
                                callback.onGetPlaces(places);
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    // Firebase Storage

    private void getStorageDownloadUrl(String pathString, OnGetStorageDownloadUrlListener callback) {
        mStorageRef.child(pathString).getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        callback.onGetStorageDownloadUrl(uri.toString());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Log.d(TAG, "getDownloadUrl(): error");
                    }
                });
    }

    // Utilities

    private LocalDate timestampToLocalDate(Timestamp timestamp) {
        return timestamp.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private Timestamp localDateToTimestamp(LocalDate localDate) {
        return new Timestamp(Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
    }

    private Timestamp localDateTimeToTimestamp(LocalDateTime localDateTime) {
        return new Timestamp(Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant()));
    }

    private Map<String, Object> getEventMap(Event event) {
        Map<String, Object> newEventMap = new HashMap<String, Object>();
        newEventMap.put("date", localDateTimeToTimestamp(event.getDate()));
        newEventMap.put("name", event.getName());
        newEventMap.put("organiser", db.document("users/" + event.getPlace().getPlaceDocId()));
        newEventMap.put("place", db.document("places/" + event.getPlace().getPlaceDocId()));
        newEventMap.put("sport", event.getSport());
        return newEventMap;
    }
}
