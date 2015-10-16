package org.stepic.droid.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Video implements Parcelable, Serializable {
    private int id;
    private String thumbnail;

    private List<VideoUrl> urls;

    private String status;
    private String upload_date;


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.thumbnail);
        dest.writeList(this.urls);
        dest.writeString(this.status);
        dest.writeString(this.upload_date);
    }

    public Video() {
    }

    protected Video(Parcel in) {
        this.id = in.readInt();
        this.thumbnail = in.readString();
        this.urls = new ArrayList<VideoUrl>();
        in.readList(this.urls, List.class.getClassLoader());
        this.status = in.readString();
        this.upload_date = in.readString();
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

    public int getId() {
        return id;
    }

    public String getThumbnail() {
        return thumbnail;
    }
}
