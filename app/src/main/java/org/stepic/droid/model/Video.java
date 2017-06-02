package org.stepic.droid.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.stepic.droid.base.App;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Video implements Parcelable, Serializable {
    private long id;
    private String thumbnail;

    private List<VideoUrl> urls;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeString(this.thumbnail);
        dest.writeList(this.urls);
    }

    public Video() {
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public void setUrls(List<VideoUrl> urls) {
        this.urls = urls;
    }

    protected Video(Parcel in) {
        this.id = in.readLong();
        this.thumbnail = in.readString();
        this.urls = new ArrayList<>();
        in.readList(this.urls, App.Companion.getAppContext().getClassLoader());
    }

    public static final Creator<Video> CREATOR = new Creator<Video>() {
        public Video createFromParcel(Parcel source) {
            return new Video(source);
        }

        public Video[] newArray(int size) {
            return new Video[size];
        }
    };

    public List<VideoUrl> getUrls() {
        return urls;
    }

    public long getId() {
        return id;
    }

    public String getThumbnail() {
        return thumbnail;
    }
}
