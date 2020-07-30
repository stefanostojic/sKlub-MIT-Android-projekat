package com.stefan.sklub;

import android.location.Location;

public class User {
    public String firstname;
    public String lastname;
    public String address;
    public Location location;
    public User[] friends;

    public User(String firstname, String lastname, String address, Location location, User[] friends) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.address = address;
        this.location = location;
        this.friends = friends;
    }
}
