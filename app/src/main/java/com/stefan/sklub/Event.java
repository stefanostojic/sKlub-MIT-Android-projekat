package com.stefan.sklub;

import android.location.Location;

public class Event {
    public String name;
    public String organiser;
    public String address;
    public Location location;

    public Event(String name, String organiser, String address, Location location) {
        this.name = name;
        this.organiser = organiser;
        this.address = address;
        this.location = location;
    }
}
