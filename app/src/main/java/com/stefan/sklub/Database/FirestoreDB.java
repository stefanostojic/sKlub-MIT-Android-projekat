package com.stefan.sklub.Database;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
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
import com.stefan.sklub.Interfaces.OnAddItem;
import com.stefan.sklub.Interfaces.OnComplete;
import com.stefan.sklub.Interfaces.OnGetItem;
import com.stefan.sklub.Interfaces.OnGetItems;
import com.stefan.sklub.Interfaces.OnUpdateItem;
import com.stefan.sklub.Model.Attendee;
import com.stefan.sklub.Model.Comment;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class FirestoreDB {
    private static FirestoreDB instance;
    private static final String TAG = "FirestoreDB ispis";
    private StorageReference mStorageRef;
    private FirebaseFirestore firestore;
    private FirebaseAuth mAuth;
    private static User specialUser;
    GeoFire geoFirePlacesRef;
    GeoFire geoFireUsersRef;

    private FirestoreDB() {
        firestore = FirebaseFirestore.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        geoFirePlacesRef = new GeoFire(FirebaseFirestore.getInstance().collection("places"));
        geoFireUsersRef = new GeoFire(FirebaseFirestore.getInstance().collection("users"));
        geoFirePlacesRef = new GeoFire(FirebaseFirestore.getInstance().collection("places"));
    }

    public static FirestoreDB getInstance(){
        if (instance == null) {
            instance = new FirestoreDB();
        }
        return instance;
    }

    // CRUD: Event

    public void addEvent(Event event, OnAddItem callback) {

        Map<String, Object> newEventMap = new HashMap<String, Object>();
        newEventMap.put("name", event.getName());
        newEventMap.put("description", event.getDescription());
        newEventMap.put("organiser", firestore.document("users/" + event.getOrganiser().getUserDocId()));
        newEventMap.put("date", localDateTimeToTimestamp(event.getDate()));
        newEventMap.put("place", firestore.collection("places").document(event.getPlace().getPlaceDocId()));
        newEventMap.put("sport", event.getSport());

        firestore.collection("events")
                .add(newEventMap)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        callback.onAdd(documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error adding document", e);
                        callback.onError("An error occured. Sorry.");
                    }
                });
    }

    public void getEvents(OnGetItems<Event> onGetDataCallbacks) {
        Log.d(TAG, "Getting events...");
        firestore.collection("events").whereGreaterThan("date", localDateTimeToTimestamp(LocalDateTime.now().minusHours(2))).orderBy("date").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> fetchEventTask) {
                        if (fetchEventTask.isSuccessful() && fetchEventTask.getResult() != null) {
                            AtomicInteger numberOfEvents = new AtomicInteger(fetchEventTask.getResult().size());
                            for (final QueryDocumentSnapshot eventDocument : fetchEventTask.getResult()) {
                                Event event = new Event();
                                event.setEventDocId(eventDocument.getId());
                                event.setName((String) eventDocument.get("name"));
                                event.setDate(timestampToLocalDateTime((Timestamp) eventDocument.get("date")));
                                event.setSport((String) eventDocument.get("sport"));
                                event.setDescription((String) eventDocument.get("description"));

                                List<String> attendeesUserDocIdList = (List<String>) eventDocument.get("attendeesUserDocId");
                                List<String> attendeesFirstNameList = (List<String>) eventDocument.get("attendeesFirstName");
                                List<String> attendeesLastNameList = (List<String>) eventDocument.get("attendeesLastName");
                                List<String> attendeesImgUrlList = (List<String>) eventDocument.get("attendeesImgUrl");

                                if (attendeesUserDocIdList != null) {
                                    List<Attendee> attendeesList = new ArrayList<>();
                                    for (int i = 0; i < attendeesUserDocIdList.size(); i++) {
                                        Attendee attendee = new Attendee();
                                        attendee.setUserDocId(attendeesUserDocIdList.get(i));
                                        attendee.setFirstName(attendeesFirstNameList.get(i));
                                        attendee.setLastName(attendeesLastNameList.get(i));
                                        attendee.setImgUrl(attendeesImgUrlList.get(i));
                                        attendeesList.add(attendee);
                                    }
                                    event.setAttendees(attendeesList);
                                } else {
                                    Log.d(TAG, "getEvents(): No attendees for event: " + event.getName());
                                }

                                List<Comment> comments = new ArrayList<>();
                                List<String> csvComments = (List<String>) eventDocument.get("comments");
                                if (csvComments != null) {
                                    for (String csvComment : csvComments) {
                                        String[] csvCommentArray = csvComment.split(";");
                                        Comment comment = new Comment();
                                        comment.setUserDocId(csvCommentArray[0]);
                                        comment.setFirstName(csvCommentArray[1]);
                                        comment.setLastName(csvCommentArray[2]);
                                        comment.setDateTime(LocalDateTime.parse(csvCommentArray[3]));
                                        comment.setText(csvCommentArray[4]);
                                        comments.add(comment);
                                        Log.d(TAG, "Comment fetched from Firebase: " + comment);
                                    }
                                    event.setComments(comments);
                                } else {
                                    Log.d(TAG, "getEvents(): No comments for event: " + event.getName());
                                }

                                getUserByUserDocId(((DocumentReference) eventDocument.get("organiser")).getId(), (User user) -> {
                                    event.setOrganiser(user);

                                    getPlace((DocumentReference) eventDocument.get("place"), place -> {
                                        event.setPlace(place);

                                        onGetDataCallbacks.onGetItem(event);
                                        if (numberOfEvents.decrementAndGet() == 0)
                                            onGetDataCallbacks.onFinishedGettingItems();
                                    });
                                });
                            }
                        } else {
                            Log.d(TAG, "Error getting event documents: ", fetchEventTask.getException());
                        }
                    }
                });
    }

    public void getEventsByOrganiser(String userDocId, OnGetItems<Event> callback) {
        firestore.collection("events").whereEqualTo("organiser", firestore.collection("users").document(userDocId)).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> fetchEventTask) {
                        if (fetchEventTask.isSuccessful() && fetchEventTask.getResult() != null) {
                            int counter = fetchEventTask.getResult().size();
                            if (counter == 0)
                                callback.onFinishedGettingItems();
                            for (final QueryDocumentSnapshot eventDocument : fetchEventTask.getResult()) {
                                Event event = new Event();
                                event.setEventDocId(eventDocument.getId());
                                event.setName((String) eventDocument.get("name"));
                                event.setDate(timestampToLocalDateTime((Timestamp) eventDocument.get("date")));
                                event.setSport((String) eventDocument.get("sport"));
                                event.setDescription((String) eventDocument.get("description"));

                                List<String> attendeesUserDocIdList = (List<String>) eventDocument.get("attendeesUserDocId");
                                List<String> attendeesFirstNameList = (List<String>) eventDocument.get("attendeesFirstName");
                                List<String> attendeesLastNameList = (List<String>) eventDocument.get("attendeesLastName");
                                List<String> attendeesImgUrlList = (List<String>) eventDocument.get("attendeesImgUrl");

                                if (attendeesUserDocIdList != null) {
                                    List<Attendee> attendeesList = new ArrayList<>();
                                    for (int i = 0; i < attendeesUserDocIdList.size(); i++) {
                                        Attendee attendee = new Attendee();
                                        attendee.setUserDocId(attendeesUserDocIdList.get(i));
                                        attendee.setFirstName(attendeesFirstNameList.get(i));
                                        attendee.setLastName(attendeesLastNameList.get(i));
                                        attendee.setImgUrl(attendeesImgUrlList.get(i));
                                        attendeesList.add(attendee);
                                    }
                                    event.setAttendees(attendeesList);
                                } else {
                                    Log.d(TAG, "getEvents(): No attendees for event: " + event.getName());
                                }

                                List<Comment> comments = new ArrayList<>();
                                List<String> csvComments = (List<String>) eventDocument.get("comments");
                                if (csvComments != null) {
                                    for (String csvComment : csvComments) {
                                        String[] csvCommentArray = csvComment.split(";");
                                        Comment comment = new Comment();
                                        comment.setUserDocId(csvCommentArray[0]);
                                        comment.setFirstName(csvCommentArray[1]);
                                        comment.setLastName(csvCommentArray[2]);
                                        comment.setDateTime(LocalDateTime.parse(csvCommentArray[3]));
                                        comment.setText(csvCommentArray[4]);
                                        comments.add(comment);
                                        Log.d(TAG, "Comment fetched from Firebase: " + comment);
                                    }
                                    event.setComments(comments);
                                } else {
                                    Log.d(TAG, "getEvents(): No comments for event: " + event.getName());
                                }

                                getUserByUserDocId(((DocumentReference) eventDocument.get("organiser")).getId(), (User user) -> {
                                    event.setOrganiser(user);

                                    getPlace((DocumentReference) eventDocument.get("place"), place -> {
                                        event.setPlace(place);

                                        callback.onGetItem(event);
                                        if (counter == 0)
                                            Log.d(TAG, "finished getting events...");
                                            callback.onFinishedGettingItems();
                                    });
                                });
                            }
                        } else {
                            Log.d(TAG, "Error getting event documents: ", fetchEventTask.getException());
                        }
                    }
                });
    }

//    public void getEventsByAttendee(String userDocId, OnGetItems<Event> callback) {
//        firestore.collection("events").whereEqualTo("organiser", firestore.collection("users").document(userDocId)).get()
//                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<QuerySnapshot> fetchEventTask) {
//                        if (fetchEventTask.isSuccessful() && fetchEventTask.getResult() != null) {
//                            int counter = fetchEventTask.getResult().size();
//                            for (final QueryDocumentSnapshot eventDocument : fetchEventTask.getResult()) {
//                                Event event = new Event();
//                                event.setEventDocId(eventDocument.getId());
//                                event.setName((String) eventDocument.get("name"));
//                                event.setDate(timestampToLocalDateTime((Timestamp) eventDocument.get("date")));
//                                event.setSport((String) eventDocument.get("sport"));
//                                event.setDescription((String) eventDocument.get("description"));
//
//                                List<String> attendeesUserDocIdList = (List<String>) eventDocument.get("attendeesUserDocId");
//                                List<String> attendeesFirstNameList = (List<String>) eventDocument.get("attendeesFirstName");
//                                List<String> attendeesLastNameList = (List<String>) eventDocument.get("attendeesLastName");
//                                List<String> attendeesImgUrlList = (List<String>) eventDocument.get("attendeesImgUrl");
//
//                                if (attendeesUserDocIdList != null) {
//                                    List<Attendee> attendeesList = new ArrayList<>();
//                                    for (int i = 0; i < attendeesUserDocIdList.size(); i++) {
//                                        Attendee attendee = new Attendee();
//                                        attendee.setUserDocId(attendeesUserDocIdList.get(i));
//                                        attendee.setFirstName(attendeesFirstNameList.get(i));
//                                        attendee.setLastName(attendeesLastNameList.get(i));
//                                        attendee.setImgUrl(attendeesImgUrlList.get(i));
//                                        attendeesList.add(attendee);
//                                    }
//                                    event.setAttendees(attendeesList);
//                                } else {
//                                    Log.d(TAG, "getEvents(): No attendees for event: " + event.getName());
//                                }
//
//                                List<Comment> comments = new ArrayList<>();
//                                List<String> csvComments = (List<String>) eventDocument.get("comments");
//                                if (csvComments != null) {
//                                    for (String csvComment : csvComments) {
//                                        String[] csvCommentArray = csvComment.split(";");
//                                        Comment comment = new Comment();
//                                        comment.setUserDocId(csvCommentArray[0]);
//                                        comment.setFirstName(csvCommentArray[1]);
//                                        comment.setLastName(csvCommentArray[2]);
//                                        comment.setDateTime(LocalDateTime.parse(csvCommentArray[3]));
//                                        comment.setText(csvCommentArray[4]);
//                                        comments.add(comment);
//                                        Log.d(TAG, "Comment fetched from Firebase: " + comment);
//                                    }
//                                    event.setComments(comments);
//                                } else {
//                                    Log.d(TAG, "getEvents(): No comments for event: " + event.getName());
//                                }
//
//                                getUserByUserDocId(((DocumentReference) eventDocument.get("organiser")).getId(), (User user) -> {
//                                    event.setOrganiser(user);
//
//                                    getPlace((DocumentReference) eventDocument.get("place"), place -> {
//                                        event.setPlace(place);
//
//                                        callback.onGetItem(event);
//                                        if (counter == 0)
//                                            Log.d(TAG, "finished getting events...");
//                                            callback.onFinishedGettingItems();
//                                    });
//                                });
//                            }
//                        } else {
//                            Log.d(TAG, "Error getting event documents: ", fetchEventTask.getException());
//                        }
//                    }
//                });
//    }

    public void getEventsByAttendee(String userDocId, OnGetItems<Event> callback) {
        firestore.collection("events").whereArrayContains("attendeesUserDocId", userDocId).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> fetchEventTask) {
                        if (fetchEventTask.isSuccessful() && fetchEventTask.getResult() != null) {
                            int counter = fetchEventTask.getResult().size();
                            if (counter == 0)
                                callback.onFinishedGettingItems();
                            for (final QueryDocumentSnapshot eventDocument : fetchEventTask.getResult()) {
                                Event event = new Event();
                                event.setEventDocId(eventDocument.getId());
                                event.setName((String) eventDocument.get("name"));
                                event.setDate(timestampToLocalDateTime((Timestamp) eventDocument.get("date")));
                                event.setSport((String) eventDocument.get("sport"));
                                event.setDescription((String) eventDocument.get("description"));

                                List<String> attendeesUserDocIdList = (List<String>) eventDocument.get("attendeesUserDocId");
                                List<String> attendeesFirstNameList = (List<String>) eventDocument.get("attendeesFirstName");
                                List<String> attendeesLastNameList = (List<String>) eventDocument.get("attendeesLastName");
                                List<String> attendeesImgUrlList = (List<String>) eventDocument.get("attendeesImgUrl");

                                if (attendeesUserDocIdList != null) {
                                    List<Attendee> attendeesList = new ArrayList<>();
                                    for (int i = 0; i < attendeesUserDocIdList.size(); i++) {
                                        Attendee attendee = new Attendee();
                                        attendee.setUserDocId(attendeesUserDocIdList.get(i));
                                        attendee.setFirstName(attendeesFirstNameList.get(i));
                                        attendee.setLastName(attendeesLastNameList.get(i));
                                        attendee.setImgUrl(attendeesImgUrlList.get(i));
                                        attendeesList.add(attendee);
                                    }
                                    event.setAttendees(attendeesList);
                                } else {
                                    Log.d(TAG, "getEvents(): No attendees for event: " + event.getName());
                                }

                                List<Comment> comments = new ArrayList<>();
                                List<String> csvComments = (List<String>) eventDocument.get("comments");
                                if (csvComments != null) {
                                    for (String csvComment : csvComments) {
                                        String[] csvCommentArray = csvComment.split(";");
                                        Comment comment = new Comment();
                                        comment.setUserDocId(csvCommentArray[0]);
                                        comment.setFirstName(csvCommentArray[1]);
                                        comment.setLastName(csvCommentArray[2]);
                                        comment.setDateTime(LocalDateTime.parse(csvCommentArray[3]));
                                        comment.setText(csvCommentArray[4]);
                                        comments.add(comment);
                                        Log.d(TAG, "Comment fetched from Firebase: " + comment);
                                    }
                                    event.setComments(comments);
                                } else {
                                    Log.d(TAG, "getEvents(): No comments for event: " + event.getName());
                                }

                                getUserByUserDocId(((DocumentReference) eventDocument.get("organiser")).getId(), (User user) -> {
                                    event.setOrganiser(user);

                                    getPlace((DocumentReference) eventDocument.get("place"), place -> {
                                        event.setPlace(place);

                                        callback.onGetItem(event);
                                        if (counter == 0)
                                            Log.d(TAG, "finished getting events...");
                                        callback.onFinishedGettingItems();
                                    });
                                });
                            }
                        } else {
                            Log.d(TAG, "Error getting event documents: ", fetchEventTask.getException());
                        }
                    }
                });
    }

    public void addEventAttendence(Event event, User user) {
        firestore.collection("events").document(event.getEventDocId())
                .update("attendeesUserDocId", FieldValue.arrayUnion(user.getUserDocId()));
        firestore.collection("events").document(event.getEventDocId())
                .update("attendeesFirstName", FieldValue.arrayUnion(user.getFirstName()));
        firestore.collection("events").document(event.getEventDocId())
                .update("attendeesLastName", FieldValue.arrayUnion(user.getLastName()));
        firestore.collection("events").document(event.getEventDocId())
                .update("attendeesImgUrl", FieldValue.arrayUnion(user.getImgUrl()));
    }

    public void removeEventAttendence(Event event, User user) {
        firestore.collection("events").document(event.getEventDocId())
                .update("attendeesUserDocId", FieldValue.arrayRemove(user.getUserDocId()));
        firestore.collection("events").document(event.getEventDocId())
                .update("attendeesFirstName", FieldValue.arrayRemove(user.getFirstName()));
        firestore.collection("events").document(event.getEventDocId())
                .update("attendeesLastName", FieldValue.arrayRemove(user.getLastName()));
        firestore.collection("events").document(event.getEventDocId())
                .update("attendeesImgUrl", FieldValue.arrayRemove(user.getImgUrl()));
    }

    public void addComment(Event event, Comment comment) {
        String csvComment = comment.getUserDocId();
        csvComment += ";" + comment.getFirstName();
        csvComment += ";" + comment.getLastName();
        csvComment += ";" + comment.getDateTime().toString();
        csvComment += ";" + comment.getText();
        firestore.collection("events").document(event.getEventDocId())
                .update("comments", FieldValue.arrayUnion(csvComment));
    }

    // CRUD: User

    public void addUser(User user, OnAddItem callback) {
        Map<String, Object> newUserMap = new HashMap<String, Object>();
        newUserMap.put("userUid", user.getUserUid());
        newUserMap.put("firstName", user.getFirstName());
        newUserMap.put("lastName", user.getLastName());
        newUserMap.put("imgUrl", "default");
        newUserMap.put("birthday", localDateToTimestamp(user.getBirthday()));
        newUserMap.put("gender", user.getGender());

        firestore.collection("users")
                .add(newUserMap)
                .addOnSuccessListener(documentReference -> {
                    geoFireUsersRef.setLocation(documentReference.getId(), geoPointToGeoLocation(user.getLocationAsGeoPoint()), (key, exception) ->   {
                        if (exception == null) {
                            callback.onAdd(documentReference.getId());
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error adding User", e);
                });
    }

    public void getUserByUserDocId(String userDocId, OnGetItem<User> callback) {
        Log.d(TAG, "getUserByUserDocId(): getting user");

        firestore.collection("users").document(userDocId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().exists()) {
                            DocumentSnapshot userDocumentSnapshot = task.getResult();
                            User user = new User();
                            user.setUserDocId(userDocumentSnapshot.getId());
                            user.setUserUid((String) userDocumentSnapshot.get("userUid"));
                            user.setFirstName((String) userDocumentSnapshot.get("firstName"));
                            user.setLastName((String) userDocumentSnapshot.get("lastName"));
                            user.setBirthday(timestampToLocalDate((Timestamp) userDocumentSnapshot.get("birthday")));
                            user.setGender((String) userDocumentSnapshot.get("gender"));
                            user.setImgUrl((String) userDocumentSnapshot.get("imgUrl"));

                            getGeoFireLocation(geoFireUsersRef, userDocId, data -> {
                                user.setLocation(data);
                                callback.onGetItem(user);
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

        firestore.collection("users").whereEqualTo("userUid", userUid).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().size() == 0)
                            Log.e(TAG, "getUserByUserUid(): User not found");
                        for (QueryDocumentSnapshot userQueryDocumentSnapshot : task.getResult()) {
                            Log.d(TAG, "getUserByUserUid(): User found");

                            User user = new User();
                            user.setUserDocId(userQueryDocumentSnapshot.getId());
                            user.setUserUid((String) userQueryDocumentSnapshot.get("userUid"));
                            user.setFirstName((String) userQueryDocumentSnapshot.get("firstName"));
                            user.setLastName((String) userQueryDocumentSnapshot.get("lastName"));
                            user.setBirthday(timestampToLocalDate((Timestamp) userQueryDocumentSnapshot.get("birthday")));
                            user.setGender((String) userQueryDocumentSnapshot.get("gender"));
                            user.setImgUrl((String) userQueryDocumentSnapshot.get("imgUrl"));

                            getGeoFireLocation(geoFireUsersRef, userQueryDocumentSnapshot.getId(), locationData -> {
                                user.setLocation(locationData);
                                callback.onGetItem(user);
                            });
                        }
                    } else {
                        Log.e(TAG, "getUserByUserUid(): Error getting documents: ", task.getException());
                    }
                });
    }

    public void updateUserProfile(User newUserData, byte[] imgByteArray, boolean removeImg, String oldPassword, String newPassword, OnUpdateItem<User> onUpdateUser) {
        FirebaseUser user = mAuth.getCurrentUser();

        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), oldPassword);

        Log.d(TAG, "Reauthenticating");
        user.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            user.updatePassword(newPassword).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Log.d(TAG, "Password updated; Updating profile...");
                                        Map<String, Object> newUserMap = new HashMap<String, Object>();
                                        newUserMap.put("firstName", newUserData.getFirstName());
                                        newUserMap.put("lastName", newUserData.getLastName());
                                        newUserMap.put("birthday", localDateToTimestamp(newUserData.getBirthday()));
                                        newUserMap.put("gender", newUserData.getGender());
                                        newUserMap.put("userUid", newUserData.getUserUid());
                                        if (imgByteArray != null) {
                                            String imageName = newUserData.getFirstName() + "_" + newUserData.getLastName();
                                            addStorageImg("users", imageName.toLowerCase(), imgByteArray, imgUrl -> {
                                                newUserData.setImgUrl(imgUrl);
                                                newUserMap.put("imgUrl", imgUrl);
                                                firestore.collection("users").document(newUserData.getUserDocId()).update(newUserMap);
                                                setGeoFireLocation(geoFireUsersRef, newUserData.getUserDocId(), newUserData.getLocationAsGeoPoint(), data -> {
                                                    onUpdateUser.onSuccessfulUpdate(newUserData);
                                                });
                                            });
                                        } else {
                                            newUserMap.put("imgUrl", "default");
                                            firestore.collection("users").document(newUserData.getUserDocId()).update(newUserMap);
                                            setGeoFireLocation(geoFireUsersRef, newUserData.getUserDocId(), newUserData.getLocationAsGeoPoint(), data -> {
                                                onUpdateUser.onSuccessfulUpdate(newUserData);
                                            });
                                        }


                                    } else {
                                        Log.e(TAG, "Error password not updated");
                                        onUpdateUser.onError("Error authenticating user. Old password was not right.");
                                    }
                                }
                            });
                        } else {
                            Log.e(TAG, "Error auth failed");
                            onUpdateUser.onError("Error authenticating user. Old password was not right.");
                        }
                    }
                });
    }

    // CRUD: Places

    public void addPlace(Place place, byte[] imgByteArray, OnAddItem callback) {
        Map<String, Object> newPlaceMap = new HashMap<String, Object>();
        newPlaceMap.put("name", place.getName());

        addStorageImg("places", place.getName().toLowerCase().replace(" ", "_"), imgByteArray, imgUrl -> {
            newPlaceMap.put("imgUrl", imgUrl);
            firestore.collection("places").add(newPlaceMap)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            setGeoFireLocation(geoFirePlacesRef, documentReference.getId(), place.getLocation(), data1 -> {
                                callback.onAdd(documentReference.getId());
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error adding document", e);
                            callback.onError("Error adding place. Sorry.");
                        }
                    });
        });
    }

    public void getPlace(DocumentReference placeDocRef, OnGetItem<Place> callback) {
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
                                place.setImgUrl((String) document.get("imgUrl"));

                                getGeoFireLocation(geoFirePlacesRef, place.getPlaceDocId(), locationData -> {
                                    place.setLocation(locationData);
                                    callback.onGetItem(place);
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
        geoFireQueryByLocation(geoFirePlacesRef, mapLocationCenter, 10, placeDocId -> {
           getPlace(firestore.collection("places").document(placeDocId), result -> {
               callback.onComplete(result);
           });
        });
    }

    // Firebase Storage

    private void addStorageImg(String collectionName, String imageName, byte[] data, OnComplete<String> onImgUpload) {
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
//                onCompleteCallback.onComplete(newImageRef.getPath());
                getStorageDownloadUrl(newImageRef.getPath(), url -> {
                    onImgUpload.onComplete(url);
                });
            }
        });
    }

    private void getStorageDownloadUrl(String pathString, OnComplete<String> onGetStorageDownloadUrl) {
        Log.d(TAG, "getStorageDownloadUrl(): getting download URL");
        mStorageRef.child(pathString).getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Log.d(TAG, "getStorageDownloadUrl(): success");
                        onGetStorageDownloadUrl.onComplete(uri.toString());
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

    public static User getSpecialUser() {
        return specialUser;
    }

    public static void setSpecialUser(User specialUser) {
        FirestoreDB.specialUser = specialUser;
    }

    /**
     * Converts a {@link java.time.LocalDate LocalDate} date to {@link com.google.firebase.Timestamp Timestamp}
     * @param timestamp Date to convert
     * @return {@link java.time.LocalDate LocalDate}
     */
    private LocalDate timestampToLocalDate(Timestamp timestamp) {
        return timestamp.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private LocalDateTime timestampToLocalDateTime(Timestamp timestamp) {
        return timestamp.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
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
