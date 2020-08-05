package com.stefan.sklub;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.GeoPoint;

import java.util.Date;

public class Place implements Parcelable {
    private String placeDocId;
    private String name;
    private LatLng location;
    private String imgUri;

    public Place() {

    }

    public Place(String placeDocId, String name, LatLng location, String imgUri) {
        this.placeDocId = placeDocId;
        this.name = name;
        this.location = location;
        this.imgUri = imgUri;
    }

    public String documentPath() {
        return "places/" + placeDocId;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(placeDocId);
        out.writeString(name);
        out.writeDouble(location.latitude);
        out.writeDouble(location.longitude);
        out.writeString(imgUri);
    }

    public static final Parcelable.Creator<Place> CREATOR = new Parcelable.Creator<Place>() {
        public Place createFromParcel(Parcel in) {
            return new Place(in);
        }

        public Place[] newArray(int size) {
            return new Place[size];
        }
    };

    private Place(Parcel in) {
        placeDocId = in.readString();
        name = in.readString();
        location = new LatLng(in.readDouble(), in.readDouble());
        imgUri = in.readString();
    }

    public String getPlaceDocId() {
        return placeDocId;
    }

    public void setPlaceDocId(String placeDocId) {
        this.placeDocId = placeDocId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
