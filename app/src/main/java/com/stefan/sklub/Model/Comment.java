package com.stefan.sklub.Model;

import android.os.Parcel;
import android.os.Parcelable;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Comment implements Parcelable, Comparable<Comment> {
    private String userDocId;
    private String firstName;
    private String lastName;
    private LocalDateTime dateTime;
    private String text;

    public Comment() {}

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

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
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
        dest.writeSerializable(this.dateTime);
        dest.writeString(this.text);
    }

    protected Comment(Parcel in) {
        this.userDocId = in.readString();
        this.firstName = in.readString();
        this.lastName = in.readString();
        this.dateTime = (LocalDateTime) in.readSerializable();
        this.text = in.readString();
    }

    public static final Creator<Comment> CREATOR = new Creator<Comment>() {
        @Override
        public Comment createFromParcel(Parcel source) {
            return new Comment(source);
        }

        @Override
        public Comment[] newArray(int size) {
            return new Comment[size];
        }
    };

    @Override
    public int compareTo(Comment secondComment) {

        if (dateTime.equals(secondComment.dateTime))
            return 0;
        else if (dateTime.isAfter(secondComment.dateTime))
            return 1;
        else
            return -1;
    }

    @Override
    public String toString() {
        return "Comment{" +
                "userDocId='" + userDocId + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", dateTime=" + dateTime.format(DateTimeFormatter.ofPattern("HH:mm, dd.MM.yyyy.")) +
                ", text='" + text + '\'' +
                '}';
    }
}
