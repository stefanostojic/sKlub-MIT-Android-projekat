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
import com.koalap.geofirestore.GeoFire;
import com.koalap.geofirestore.GeoLocation;
import com.koalap.geofirestore.GeoQuery;
import com.koalap.geofirestore.GeoQueryEventListener;
import com.koalap.geofirestore.LocationCallback;
import com.stefan.sklub.Interfaces.OnComplete;
import com.stefan.sklub.Interfaces.OnGetItem;
import com.stefan.sklub.Model.Event;
import com.stefan.sklub.Model.Place;
import com.stefan.sklub.Model.User;

//import org.imperiumlabs.geofirestore.GeoFirestore;
//import org.imperiumlabs.geofirestore.GeoQuery;
//import org.imperiumlabs.geofirestore.GeoQueryDataEventListener;
//import org.imperiumlabs.geofirestore.GeoQueryEventListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class FirestoreDB {
    private static FirestoreDB instance;
    private static final String TAG = "FirestoreDB ispis";
    private StorageReference mStorageRef;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    GeoFire geoFirePlacesRef;
    GeoFire geoFireUsersRef;

    public FirestoreDB() {
        db = FirebaseFirestore.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        geoFirePlacesRef = new GeoFire(FirebaseFirestore.getInstance().collection("places"));
        geoFireUsersRef = new GeoFire(FirebaseFirestore.getInstance().collection("users"));
    }

    public static FirestoreDB getInstance(){
        if (instance == null) {
            instance = new FirestoreDB();
        }
        return instance;
    }

//    @Override
//    public void addEvent(Event event) {
//
//    }
//
//    @Override
//    public void getEvents(OnComplete<Event> callback) {
//
//    }

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
        void onGetPlaces(Place place);
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

        Map<String, Object> newEventMap = new HashMap<String, Object>();
        newEventMap.put("date", localDateTimeToTimestamp(event.getDate()));
        newEventMap.put("name", event.getName());
        newEventMap.put("organiser", db.document("users/" + event.getPlace().getPlaceDocId()));
        newEventMap.put("place", db.document("places/" + event.getPlace().getPlaceDocId()));
        newEventMap.put("sport", event.getSport());

        db.collection("events")
                .add(newEventMap)
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

    public void getEvents(com.stefan.sklub.Interfaces.OnGetItems<Event> onGetDataCallbacks) {
        Log.d(TAG, "Getting events...");
        db.collection("events").orderBy("date").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> fetchEventTask) {
                        if (fetchEventTask.isSuccessful() && fetchEventTask.getResult() != null) {
                            for (final QueryDocumentSnapshot eventDocument : fetchEventTask.getResult()) {
                                Event event = new Event();
                                event.setEventDocId(eventDocument.getId());
                                event.setName((String) eventDocument.get("name"));
                                event.setDate((Timestamp) eventDocument.get("date"));
                                event.setSport((String) eventDocument.get("sport"));
                                event.setDescription((String) eventDocument.get("description"));

                                getUserByUserDocId(((DocumentReference) eventDocument.get("organiser")).getId(), (User user) -> {
                                    event.setOrganiser(user);

                                    getPlace((DocumentReference) eventDocument.get("place"), (OnGetListener<Place>) place -> {
                                        event.setPlace(place);

                                        onGetDataCallbacks.onGetItem(event);
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
                .addOnSuccessListener(documentReference -> {
                    geoFireUsersRef.setLocation(documentReference.getId(), geoPointToGeoLocation(user.getLocationAsGeoPoint()), (key, exception) ->   {
                        if (exception == null) {
                            onAddUserListener.onAddUser();
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error adding User", e);
                });
    }

    public void getUserByUserDocId(String userDocId, OnGetUserListener callback) {
        Log.d(TAG, "getUserByUserDocId(): getting user");

        db.collection("users").document(userDocId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().exists()) {
                            DocumentSnapshot userDocumentSnapshot = task.getResult();
                            User user = new User();
                            user.setUserDocId(userDocumentSnapshot.getId());
                            user.setUserUid((String) userDocumentSnapshot.get("userUid"));
                            user.setFirstname((String) userDocumentSnapshot.get("firstname"));
                            user.setLastname((String) userDocumentSnapshot.get("lastname"));
                            user.setBirthday((Timestamp) userDocumentSnapshot.get("birthday"));
                            user.setImgUri((String) userDocumentSnapshot.get("img"));

                            getGeoFireLocation(geoFireUsersRef, userDocId, data -> {
                                user.setLocation(data);
                                getStorageDownloadUrl((String) userDocumentSnapshot.get("img"), url -> {
                                    user.setImgUri(url);
                                    callback.onGetUser(user);
                                });
                            });
                        } else {
                            Log.e(TAG, "getUserProfile(): User not found");
                        }
                    } else {
                        Log.e(TAG, "getUserProfile(): Error getting documents: ", task.getException());
                    }
                });
    }

    public void getUserByUserUid(String userUid, OnGetItem<User> callback) {
        Log.d(TAG, "getUserByUserUid(): getting user");

        db.collection("users").whereEqualTo("userUid", userUid).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot userQueryDocumentSnapshot : task.getResult()) {
                            Log.d(TAG, "getUserByUserUid(): User found");

                            User user = new User();
                            user.setUserDocId(userQueryDocumentSnapshot.getId());
                            user.setUserUid((String) userQueryDocumentSnapshot.get("userUid"));
                            user.setFirstname((String) userQueryDocumentSnapshot.get("firstname"));
                            user.setLastname((String) userQueryDocumentSnapshot.get("lastname"));
                            user.setBirthday((Timestamp) userQueryDocumentSnapshot.get("birthday"));
                            user.setImgUri((String) userQueryDocumentSnapshot.get("img"));

                            getGeoFireLocation(geoFireUsersRef, userQueryDocumentSnapshot.getId(), data -> {
                                user.setLocation(data);
                                getStorageDownloadUrl((String) userQueryDocumentSnapshot.get("img"), url -> {
                                    user.setImgUri(url);
                                    callback.onGetItem(user);
                                });
                            });
                        }
                    } else {
                        Log.e(TAG, "getUserByUserUid(): Error getting documents: ", task.getException());
                    }
                });
    }

    // CRUD: Places

    public void addPlace(Place place, byte[] imgByteArray, OnAddPlaceListener onAddPlaceListener) {
        Map<String, Object> newPlaceMap = new HashMap<String, Object>();
        newPlaceMap.put("name", place.getName());
        newPlaceMap.put("location", place.getLocation());
        newPlaceMap.put("img", "places/" + place.getName() + ".jpg");

        addStorageImg("places", place.getName(), imgByteArray, data1 -> {
            db.collection("places").add(newPlaceMap)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            setGeoFireLocation(geoFirePlacesRef, documentReference.getId(), place.getLocation(), data1 -> {
                                onAddPlaceListener.onAddPlace();
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error adding document", e);
                        }
                    });
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

    public void getPlace(DocumentReference placeDocRef, OnGetListener<Place> listener) {
        placeDocRef.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Place place = new Place();
                                place.setPlaceDocId(document.getId());
                                place.setName((String) document.get("name"));
                                place.setLocation((GeoPoint) document.get("location"));

                                getStorageDownloadUrl((String) document.get("img"), url -> {
                                    place.setImgUri(url);

                                    listener.onGet(place);
                                });
                            } else {
                                Log.d(TAG, "getPlace(): No such document");
                            }
                        } else {
                            Log.d(TAG, "getPlace(): get failed with ", task.getException());
                        }
                    }
                });
    }

    public void getPlaces(GeoPoint mapLocationCenter, OnComplete<Place> callback) {
        geoFireQueryByLocation(geoFirePlacesRef, mapLocationCenter, 5.5, placeDocId -> {
           getPlace(db.collection("places").document(placeDocId), result -> {
               callback.onComplete(result);
           });
        });
    }

    // Firebase Storage

    private void addStorageImg(String collectionName, String imageName, byte[] data, OnComplete<String> onCompleteCallback) {
        StorageReference newImageRef = mStorageRef.child(collectionName + "/" + imageName + ".jpg");

        UploadTask uploadTask = newImageRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.d(TAG, "addStorageImg: error uploading image ", exception);
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.d(TAG, "addStorageImg: image successfully uploaded");
                onCompleteCallback.onComplete(newImageRef.getPath());
            }
        });
    }

    private void getStorageDownloadUrl(String pathString, OnGetStorageDownloadUrlListener callback) {
        Log.d(TAG, "getStorageDownloadUrl(): getting download URL");
        mStorageRef.child(pathString).getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Log.d(TAG, "getStorageDownloadUrl(): success");
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

    // GeoFire

    private void setGeoFireLocation(GeoFire geoFireCollectionRef, String docId, GeoPoint newLocation, OnComplete<GeoPoint> onCompleteCallback) {
        geoFireCollectionRef.setLocation(docId, new GeoLocation(newLocation.getLatitude(), newLocation.getLongitude()), (key, exception) -> {
            onCompleteCallback.onComplete(newLocation);
        });
    }

    private void getGeoFireLocation(GeoFire geoFireCollectionRef, String docId, OnComplete<GeoPoint> onCompleteCallback) {
        Log.d(TAG, "getGeoFireLocation(): getting location");

        geoFireCollectionRef.getLocation(docId, new LocationCallback() {
            @Override
            public void onLocationResult(String key, GeoLocation location) {
                Log.d(TAG, "getGeoFireLocation(): success");
                onCompleteCallback.onComplete(new GeoPoint(location.latitude, location.longitude));
            }

            @Override
            public void onCancelled(Exception exception) {
                Log.e(TAG, "getGeoFireLocation(): ERROR: ", exception);
            }
        });
    }

    private void geoFireQueryByLocation(GeoFire geoFireCollectionRef, GeoPoint location, double radius, OnComplete<String> onGetDocId) {
        GeoQuery geoQuery = geoFireCollectionRef.queryAtLocation(geoPointToGeoLocation(location), radius);

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
//                System.out.println(String.format("Key %s entered the search area at [%f,%f]", key, location.latitude, location.longitude));

                onGetDocId.onComplete(key);
            }

            @Override
            public void onKeyExited(String key) {
                System.out.println(String.format("Key %s is no longer in the search area", key));
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                System.out.println(String.format("Key %s moved within the search area to [%f,%f]", key, location.latitude, location.longitude));
            }

            @Override
            public void onGeoQueryReady() {
                System.out.println("All initial data has been loaded and events have been fired!");
            }

            @Override
            public void onGeoQueryError(Exception exception) {
                System.err.println("There was an error with this query: " + exception.toString());
            }
        });
    }

    // Utilities

    /**
     * Converts a {@link java.time.LocalDate LocalDate} date to {@link com.google.firebase.Timestamp Timestamp}
     * @param timestamp Date to convert
     * @return {@link java.time.LocalDate LocalDate}
     */
    private LocalDate timestampToLocalDate(Timestamp timestamp) {
        return timestamp.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private Timestamp localDateToTimestamp(LocalDate localDate) {
        return new Timestamp(Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
    }

    private Timestamp localDateTimeToTimestamp(LocalDateTime localDateTime) {
        return new Timestamp(Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant()));
    }

    private GeoLocation geoPointToGeoLocation(GeoPoint geoPoint) {
        return new GeoLocation(geoPoint.getLatitude(), geoPoint.getLongitude());
    }

    private GeoLocation geoLocationToGeoPoint(GeoLocation geoLocation) {
        return new GeoLocation(geoLocation.latitude, geoLocation.longitude);
    }

    // Fixes

    /*public void fixGeoFirestoreDocumentsUsers() {
        db.collection("users").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot userDocumentSnapshot : task.getResult().getDocuments()) {
                            setGeoFireLocation(geoFireUsersRef, userDocumentSnapshot.getId(), (GeoPoint) userDocumentSnapshot.get("location"), data -> {

                            });
                        }
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                });
    }

    public void fixGeoFirestoreDocumentsPlaces() {
        db.collection("places").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot userDocumentSnapshot : task.getResult().getDocuments()) {
                            setGeoFireLocation(geoFirePlacesRef, userDocumentSnapshot.getId(), (GeoPoint) userDocumentSnapshot.get("location"), data -> {

                            });
                        }
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                });
    }*/
}
