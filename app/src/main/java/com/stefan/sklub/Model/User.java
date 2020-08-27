package com.stefan.sklub.Model;

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
    private String userUid;
    private String userDocId;
    private String firstName;
    private String lastName;
    private LocalDate birthday;
    private String gender;
    private LatLng location;
    private String imgUrl;

    public User() {

    }

    public User(String userUid) {
        this.userUid = userUid;
    }

    public User(String userUid, String firstName, String lastName, LocalDate birthday, String gender) {
        this(userUid);
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthday = birthday;
        this.gender = gender;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(userUid);
        out.writeString(userDocId);
        out.writeString(firstName);
        out.writeString(lastName);
        out.writeString(birthday.toString());
        out.writeString(gender);
        out.writeDouble(location.latitude);
        out.writeDouble(location.longitude);
        out.writeString(imgUrl);
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
        userUid = in.readString();
        userDocId = in.readString();
        firstName = in.readString();
        lastName = in.readString();
        birthday = LocalDate.parse(in.readString());
        gender = in.readString();
        location = new LatLng(in.readDouble(), in.readDouble());
        imgUrl = in.readString();
    }
//
//    public String toString() {
//        return "{ userUid: " + userUid + ", firstname: " + firstName + ", lastname: " + lastName + ", birthday: " + birthday.toString() + ", gender: " + gender + ", location: " + location.toString() + ", imgUri: " + imgUrl + " }";
//    }

    @Override
    public String toString() {
        return "User{" +
                "userUid='" + userUid + '\'' +
                ", userDocId='" + userDocId + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", birthday=" + birthday +
                ", gender='" + gender + '\'' +
                ", location=" + location +
                ", imgUrl='" + imgUrl + '\'' +
                '}';
    }

    public String getUserUid() {
        return userUid;
    }

    public void setUserUid(String userUid) {
        this.userUid = userUid;
    }

    public String getUserDocId() {
        return userDocId;
    }

    public void setUserDocId(String userDocId) {
        this.userDocId = userDocId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
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

    public GeoPoint getLocationAsGeoPoint() {
        return new GeoPoint(location.latitude, location.longitude);
    }

    public void setLocation(GeoPoint geoPoint) {
        this.location = new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude());
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }
}
