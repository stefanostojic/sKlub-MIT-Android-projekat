package com.stefan.sklub.Model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Event implements Parcelable, Comparable<Event> {
    private String eventDocId;
    private String name;
    private String description;
    private User organiser;
    private LocalDateTime date;
    private Place place;
    private String sport;
    private List<Attendee> attendees;
    private List<Comment> comments;

    public Event() {
        this.organiser = new User();
        this.place = new Place();
        this.attendees = new ArrayList<>();
        this.comments = new ArrayList<>();
    }

    @Override
    public String toString() {
        return "Event{" +
                "eventDocId='" + eventDocId + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", organiser=" + organiser +
                ", date=" + date +
                ", place=" + place +
                ", sport='" + sport + '\'' +
                ", attendees=" + attendees +
                ", comments=" + comments +
                '}';
    }
//    public String toString() {
//        return "{ eventDocId: " + eventDocId + ", name: " + name + ", organiser: " + organiser + ", date: " + date.toString() + ", place: " + place.toString() + ", sport: " + sport + " }";
//    }

    @Override
    public int compareTo(Event event2) {
        if (date.equals(event2.date))
            return 0;
        else if (date.isAfter(event2.date))
            return 1;
        else
            return -1;
    }

    public String getEventDocId() {
        return eventDocId;
    }

    public void setEventDocId(String eventDocId) {
        this.eventDocId = eventDocId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public User getOrganiser() {
        return organiser;
    }

    public void setOrganiser(User organiser) {
        this.organiser = organiser;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public Place getPlace() {
        return place;
    }

    public void setPlace(Place place) {
        this.place = place;
    }

    public String getSport() {
        return sport;
    }

    public void setSport(String sport) {
        this.sport = sport;
    }

    public List<Attendee> getAttendees() {
        return attendees;
    }

    public void setAttendees(List<Attendee> attendees) {
        this.attendees = attendees;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.eventDocId);
        dest.writeString(this.name);
        dest.writeString(this.description);
        dest.writeParcelable(this.organiser, flags);
        dest.writeSerializable(this.date);
        dest.writeParcelable(this.place, flags);
        dest.writeString(this.sport);
        dest.writeTypedList(this.attendees);
        dest.writeTypedList(this.comments);
    }

    protected Event(Parcel in) {
        this.eventDocId = in.readString();
        this.name = in.readString();
        this.description = in.readString();
        this.organiser = in.readParcelable(User.class.getClassLoader());
        this.date = (LocalDateTime) in.readSerializable();
        this.place = in.readParcelable(Place.class.getClassLoader());
        this.sport = in.readString();
        this.attendees = in.createTypedArrayList(Attendee.CREATOR);
        this.comments = in.createTypedArrayList(Comment.CREATOR);
    }

    public static final Creator<Event> CREATOR = new Creator<Event>() {
        @Override
        public Event createFromParcel(Parcel source) {
            return new Event(source);
        }

        @Override
        public Event[] newArray(int size) {
            return new Event[size];
        }
    };
}
