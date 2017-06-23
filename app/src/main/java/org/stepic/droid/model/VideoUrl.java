package org.stepic.droid.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class VideoUrl implements Parcelable, Serializable {
    private String url;
    private String quality;

    public String getUrl() {
        return url;
    }

    public String getQuality() {
        return quality;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setQuality(String quality) {
        this.quality = quality;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.url);
        dest.writeString(this.quality);
    }

    public VideoUrl() {
    }

    public VideoUrl(String url, String quality) {
        this.url = url;
        this.quality = quality;
    }

    protected VideoUrl(Parcel in) {
        this.url = in.readString();
        this.quality = in.readString();
    }

    public static final Creator<VideoUrl> CREATOR = new Creator<VideoUrl>() {
        public VideoUrl createFromParcel(Parcel source) {
            return new VideoUrl(source);
        }

        public VideoUrl[] newArray(int size) {
            return new VideoUrl[size];
        }
    };
}
