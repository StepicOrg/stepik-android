package org.stepic.droid.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class Step implements Parcelable, Serializable {
    private long id;
    private long lesson;
    private long position;
    private String status;
    private Block block;
    private String progress;
    private String[] subscriptions;
    private long viewed_by;
    private long passed_by;
    private String create_date;
    private String update_date;
    private boolean is_cached;

    public boolean is_cached() {
        return is_cached;
    }

    public void setIs_cached(boolean is_cached) {
        this.is_cached = is_cached;
    }

    public long getId() {
        return id;
    }

    public long getLesson() {
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

    public long getPosition() {
        return position;
    }

    public void setPosition(long position) {
        this.position = position;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setLesson(long lesson) {
        this.lesson = lesson;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setBlock(Block block) {
        this.block = block;
    }

    public void setProgress(String progress) {
        this.progress = progress;
    }

    public void setSubscriptions(String[] subscriptions) {
        this.subscriptions = subscriptions;
    }

    public void setViewed_by(long viewed_by) {
        this.viewed_by = viewed_by;
    }

    public void setPassed_by(long passed_by) {
        this.passed_by = passed_by;
    }

    public void setCreate_date(String create_date) {
        this.create_date = create_date;
    }

    public void setUpdate_date(String update_date) {
        this.update_date = update_date;
    }

    public Step() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeLong(this.lesson);
        dest.writeLong(this.position);
        dest.writeString(this.status);
        dest.writeParcelable(this.block, 0);
        dest.writeString(this.progress);
        dest.writeStringArray(this.subscriptions);
        dest.writeLong(this.viewed_by);
        dest.writeLong(this.passed_by);
        dest.writeString(this.create_date);
        dest.writeString(this.update_date);
        dest.writeByte(is_cached ? (byte) 1 : (byte) 0);
    }

    protected Step(Parcel in) {
        this.id = in.readLong();
        this.lesson = in.readLong();
        this.position = in.readLong();
        this.status = in.readString();
        this.block = in.readParcelable(Block.class.getClassLoader());
        this.progress = in.readString();
        this.subscriptions = in.createStringArray();
        this.viewed_by = in.readLong();
        this.passed_by = in.readLong();
        this.create_date = in.readString();
        this.update_date = in.readString();
        this.is_cached = in.readByte() != 0;
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
