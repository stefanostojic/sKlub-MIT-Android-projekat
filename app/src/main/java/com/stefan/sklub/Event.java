package com.stefan.sklub;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.firebase.Timestamp;

import java.time.LocalDateTime;
import java.time.ZoneId;

public class Event implements Parcelable{
    private String eventId;
    private String name;
    private User organiser;
    private LocalDateTime date;
    private Place place;

    public Event() {
        this.organiser = new User();
        this.place = new Place();
    }

    public Event(String eventId, String name, LocalDateTime date) {
        this.eventId = eventId;
        this.name = name;
        this.date = date;
    }

    public Event(String eventId, String name, User organiser, LocalDateTime date, Place place) {
        this(eventId, name, date);
        this.organiser = organiser;
        this.place = place;
    }

    public String documentPath() {
        return "events/" + eventId;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(eventId);
        out.writeString(name);
        out.writeParcelable(organiser, 0);
        out.writeString(date.toString());
        out.writeParcelable(place, 0);
    }

    public static final Parcelable.Creator<Event> CREATOR = new Parcelable.Creator<Event>() {
        public Event createFromParcel(Parcel in) {
            return new Event(in);
        }

        public Event[] newArray(int size) {
            return new Event[size];
        }
    };

    private Event(Parcel in) {
        eventId = in.readString();
        name = in.readString();
        organiser = in.readParcelable(getClass().getClassLoader());
        date = LocalDateTime.parse(in.readString());
        place = in.readParcelable(getClass().getClassLoader());
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public void setDate(Timestamp timestamp) {
        if (timestamp != null) {
            Log.i("event.setDate(): ", "timestamp is not NULL: " + timestamp.toString());

            this.date = timestamp.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            if (this.date == null)
                Log.i("event.setDate(): ", "this.date is NULL");
            else {
                Log.i("event.setDate(): ", "this.date is not NULL: " + this.date.toString());
            }
        } else {
            Log.i("event.setDate(): ", "timestamp is NULL");
        }
    }

    public Place getPlace() {
        return place;
    }

    public void setPlace(Place place) {
        this.place = place;
    }
}
