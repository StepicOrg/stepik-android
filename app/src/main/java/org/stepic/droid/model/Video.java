package org.stepic.droid.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.stepic.droid.base.MainApplication;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Video implements Parcelable, Serializable {
    private long id;
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
        dest.writeLong(this.id);
        dest.writeString(this.thumbnail);
        dest.writeList(this.urls);
        dest.writeString(this.status);
        dest.writeString(this.upload_date);
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

    public void setStatus(String status) {
        this.status = status;
    }

    public void setUpload_date(String upload_date) {
        this.upload_date = upload_date;
    }

    protected Video(Parcel in) {
        this.id = in.readLong();
        this.thumbnail = in.readString();
        this.urls = new ArrayList<VideoUrl>();
        in.readList(this.urls, MainApplication.getAppContext().getClassLoader());
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

    public long getId() {
        return id;
    }

    public String getThumbnail() {
        return thumbnail;
    }
}
