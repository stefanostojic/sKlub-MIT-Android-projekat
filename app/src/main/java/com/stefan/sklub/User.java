package com.stefan.sklub;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

public class User implements Parcelable {
    private String authId;
    private String userDocId;
    private String firstname;
    private String lastname;
    private LocalDate birthday;
    private String gender;
    private LatLng location;
    private String imgUri;

    public User() {

    }

    public User(String userUid) {
        this.authId = userUid;
    }

    public User(String userUid, String firstname, String lastname, LocalDate birthday, String gender, LatLng location) {
        this(userUid);
        this.firstname = firstname;
        this.lastname = lastname;
        this.birthday = birthday;
        this.gender = gender;
        this.location = location;
    }

    public String getDocumentPath() {
        return "users/" + userDocId;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(authId);
        out.writeString(userDocId);
        out.writeString(firstname);
        out.writeString(lastname);
        out.writeString(birthday.toString());
        out.writeString(gender);
        out.writeDouble(location.latitude);
        out.writeDouble(location.longitude);
        out.writeString(imgUri);
    }

    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
        public User createFromParcel(Parcel in) {
            return new User(in);
        }
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    private User(Parcel in) {
        authId = in.readString();
        userDocId = in.readString();
        firstname = in.readString();
        lastname = in.readString();
        birthday = LocalDate.parse(in.readString());
        gender = in.readString();
        location = new LatLng(in.readDouble(), in.readDouble());
        imgUri = in.readString();
    }

    private Map<String, Object> getMap() {
        Map<String, Object> newUserMap = new HashMap<>();
        newUserMap.put("authId", authId);
        newUserMap.put("firstname", firstname);
        newUserMap.put("lastname", lastname);
        newUserMap.put("birthday", birthday);
        newUserMap.put("gender", gender);
        newUserMap.put("location", location);
        return newUserMap;
    }

    public String getAuthId() {
        return authId;
    }

    public void setAuthId(String authId) {
        this.authId = authId;
    }

    public String getUserDocId() {
        return userDocId;
    }

    public void setUserDocId(String userDocId) {
        this.userDocId = userDocId;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }

    public void setBirthday(Timestamp timestamp) {
        this.birthday = timestamp.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public LatLng getLocation() {
        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    public void setLocation(GeoPoint geoPoint) {
        this.location = new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude());
    }

    public String getImgUri() {
        return imgUri;
    }

    public void setImgUri(String imgUri) {
        this.imgUri = imgUri;
    }
}
