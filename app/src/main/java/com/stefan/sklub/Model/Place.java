package com.stefan.sklub.Model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.GeoPoint;

public class Place implements Parcelable {
    private String placeDocId;
    private String name;
    private GeoPoint location;
    private String imgUrl;

    public Place() {

    }

    public Place(String placeDocId, String name, GeoPoint location, String imgUrl) {
        this.placeDocId = placeDocId;
        this.name = name;
        this.location = location;
        this.imgUrl = imgUrl;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(placeDocId);
        out.writeString(name);
        out.writeDouble(location.getLatitude());
        out.writeDouble(location.getLongitude());
        out.writeString(imgUrl);
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
        location = new GeoPoint(in.readDouble(), in.readDouble());
        imgUrl = in.readString();
    }

    @NonNull
    @Override
    public String toString() {
        return "{ placeDocId: " + placeDocId + ", name: " + name + ", location: " + location + ", imgUri: " + imgUrl + " }";
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

    public GeoPoint getLocation() {
        return location;
    }

    public LatLng getLocationAsLatLng() {
        return new LatLng(location.getLatitude(), location.getLongitude());
    }

    public void setLocation(LatLng latLng) {
        if (latLng != null) {
            this.location = new GeoPoint(latLng.latitude, latLng.longitude);
        } else {
            throw new NullPointerException("Place location can't be set to null");
        }
    }

    public void setLocation(GeoPoint geoPoint) {
        this.location = geoPoint;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }
}
