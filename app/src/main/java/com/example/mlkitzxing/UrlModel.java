package com.example.mlkitzxing;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class UrlModel implements Parcelable {
    String title;
    String url;
    Bitmap img;

    public UrlModel(String title, String url, Bitmap img) {
        this.title = title;
        this.url = url;
        this.img = img;
    }

    protected UrlModel(Parcel in) {
        title = in.readString();
        url = in.readString();
        img = in.readParcelable(Bitmap.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(url);
        dest.writeParcelable(img, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<UrlModel> CREATOR = new Creator<UrlModel>() {
        @Override
        public UrlModel createFromParcel(Parcel in) {
            return new UrlModel(in);
        }

        @Override
        public UrlModel[] newArray(int size) {
            return new UrlModel[size];
        }
    };

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Bitmap getImg() {
        return img;
    }

    public void setImg(Bitmap img) {
        this.img = img;
    }
}
