package com.example.mlkitzxing;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class textModel implements Parcelable {
    String txt;
    Bitmap bit;

    public textModel(String txt, Bitmap bit) {
        this.txt = txt;
        this.bit = bit;
    }

    protected textModel(Parcel in) {
        txt = in.readString();
        bit = in.readParcelable(Bitmap.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(txt);
        dest.writeParcelable(bit, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<textModel> CREATOR = new Creator<textModel>() {
        @Override
        public textModel createFromParcel(Parcel in) {
            return new textModel(in);
        }

        @Override
        public textModel[] newArray(int size) {
            return new textModel[size];
        }
    };

    public String getTxt() {
        return txt;
    }

    public void setTxt(String txt) {
        this.txt = txt;
    }

    public Bitmap getBit() {
        return bit;
    }

    public void setBit(Bitmap bit) {
        this.bit = bit;
    }
}
