package org.stepic.droid.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class Step implements Parcelable, Serializable {
    private int id;
    private int lesson;
    private String status;
    private Block block;
    private String progress;
    private String[] subscriptions;
    private long viewed_by;
    private long passed_by;
    private String create_date;
    private String update_date;

    public int getId() {
        return id;
    }

    public int getLesson() {
        return lesson;
    }

    public String getStatus() {
        return status;
    }

    public Block getBlock() {
        return block;
    }

    public String getProgress() {
        return progress;
    }

    public String[] getSubscriptions() {
        return subscriptions;
    }

    public long getViewed_by() {
        return viewed_by;
    }

    public long getPassed_by() {
        return passed_by;
    }

    public String getCreate_date() {
        return create_date;
    }

    public String getUpdate_date() {
        return update_date;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeInt(this.lesson);
        dest.writeString(this.status);
        dest.writeParcelable(this.block, flags);
        dest.writeString(this.progress);
        dest.writeStringArray(this.subscriptions);
        dest.writeLong(this.viewed_by);
        dest.writeLong(this.passed_by);
        dest.writeString(this.create_date);
        dest.writeString(this.update_date);
    }

    public Step() {
    }

    protected Step(Parcel in) {
        this.id = in.readInt();
        this.lesson = in.readInt();
        this.status = in.readString();
        this.block = in.readParcelable(Block.class.getClassLoader());
        this.progress = in.readString();
        this.subscriptions = in.createStringArray();
        this.viewed_by = in.readLong();
        this.passed_by = in.readLong();
        this.create_date = in.readString();
        this.update_date = in.readString();
    }

    public static final Creator<Step> CREATOR = new Creator<Step>() {
        public Step createFromParcel(Parcel source) {
            return new Step(source);
        }

        public Step[] newArray(int size) {
            return new Step[size];
        }
    };
}
