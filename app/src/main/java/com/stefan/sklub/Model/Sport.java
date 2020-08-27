package com.stefan.sklub.Model;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

public class Sport {
    private String name;
    private String imgUri;

    public Sport() {

    }

    public static String[] getSportNames() {
        return new String[] { "Basketball", "Cycling", "Football", "Frisbee", "Ice skating", "Mountain climbing", "Running", "Swimming", "Volleyball" };
    }

//    public static String getImgUri(String name) {
//        if (name.equals("Basketball")) {
//            return "basketball.png";
//        } else if (name.equals("Frisbee")) {
//            return "frisbee.png";
//        } else if (name.equals("Ice skating")){
//            return "ice_skating.png";
//        } else if (name.equals("Mountain climbing")) {
//            return "mountain_climbing.png";
//        } else if (name.equals("Running")) {
//            return "running.png";
//        } else {
//            return "volleyball.png";
//        }
//    }

    public static Uri getImgUri(String name) {
        return Uri.parse("android.resource://com.stefan.sklub/drawable/" + name.replace(" ", "_").toLowerCase());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImgUri() {
        return imgUri;
    }

    public void setImgUri(String imgUri) {
        this.imgUri = imgUri;
    }
}
