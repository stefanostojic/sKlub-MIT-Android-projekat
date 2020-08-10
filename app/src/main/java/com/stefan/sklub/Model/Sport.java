package com.stefan.sklub.Model;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

public class Sport implements Parcelable {
    private String name;
    private String imgUri;

    public Sport() {

    }

    public static String[] getSportNames() {
        return new String[] { "Basketball", "Frisbee", "Ice skating", "Mountain climbing", "Running", "Volleyball" };
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
        String image_name = "";
        if (name.equals("Basketball")) {
            image_name = "basketball";
        } else if (name.equals("Frisbee")) {
            image_name = "frisbee";
        } else if (name.equals("Ice skating")){
            image_name = "ice_skating";
        } else if (name.equals("Mountain climbing")) {
            image_name = "mountain_climbing";
        } else if (name.equals("Running")) {
            image_name = "running";
        } else {
            image_name = "volleyball";
        }

        return Uri.parse("android.resource://com.stefan.sklub/drawable/" + image_name);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(name);
        out.writeString(imgUri);
    }

    public static final Parcelable.Creator<Sport> CREATOR = new Parcelable.Creator<Sport>() {
        public Sport createFromParcel(Parcel in) {
            return new Sport(in);
        }

        public Sport[] newArray(int size) {
            return new Sport[size];
        }
    };

    private Sport(Parcel in) {
        name = in.readString();
        imgUri = in.readString();
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
