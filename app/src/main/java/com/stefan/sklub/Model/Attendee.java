package com.stefan.sklub.Model;

import android.os.Parcel;
import android.os.Parcelable;

public class Attendee implements Parcelable {
    private String userDocId;
    private String firstName;
    private String lastName;
    private String imgUrl;

    public Attendee() {}

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

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.userDocId);
        dest.writeString(this.firstName);
        dest.writeString(this.lastName);
        dest.writeString(this.imgUrl);
    }

    protected Attendee(Parcel in) {
        this.userDocId = in.readString();
        this.firstName = in.readString();
        this.lastName = in.readString();
        this.imgUrl = in.readString();
    }

    public static final Creator<Attendee> CREATOR = new Creator<Attendee>() {
        @Override
        public Attendee createFromParcel(Parcel source) {
            return new Attendee(source);
        }

        @Override
        public Attendee[] newArray(int size) {
            return new Attendee[size];
        }
    };

    @Override
    public String toString() {
        return "Attendee{" +
                "userDocId='" + userDocId + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", imgUrl='" + imgUrl + '\'' +
                '}';
    }
}
