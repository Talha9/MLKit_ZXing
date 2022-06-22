package com.example.mlkitzxing;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class WifiModel implements Parcelable {
    String ssid;
    String password;
    int type;
    Bitmap img;

    public WifiModel(String ssid, String password, int type, Bitmap img) {
        this.ssid = ssid;
        this.password = password;
        this.type = type;
        this.img = img;
    }

    protected WifiModel(Parcel in) {
        ssid = in.readString();
        password = in.readString();
        type = in.readInt();
        img = in.readParcelable(Bitmap.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(ssid);
        dest.writeString(password);
        dest.writeInt(type);
        dest.writeParcelable(img, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<WifiModel> CREATOR = new Creator<WifiModel>() {
        @Override
        public WifiModel createFromParcel(Parcel in) {
            return new WifiModel(in);
        }

        @Override
        public WifiModel[] newArray(int size) {
            return new WifiModel[size];
        }
    };

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Bitmap getImg() {
        return img;
    }

    public void setImg(Bitmap img) {
        this.img = img;
    }
}
