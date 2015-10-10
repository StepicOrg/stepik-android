package org.stepic.droid.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class Lesson implements Parcelable, Serializable {
    private int id;
    private long[] steps;
    //    private String actions;
    private int[] tags;
    private String playlists[];
    private boolean is_featured;
    private boolean is_prime;
    private String progress;
    private int owner;
    private String[] subscriptions;
    private int viewed_by;
    private int passed_by;
    private String[] dependencies;
    private String[] followers;
    private String language;
    private boolean is_public;
    private String title;
    private String slug;
    private String create_date;
    private String update_date;
    private String learners_group;
    private String teacher_group;

    public int getId() {
        return id;
    }

    public long[] getSteps() {
        return steps;
    }

    public int[] getTags() {
        return tags;
    }

    public String[] getPlaylists() {
        return playlists;
    }

    public boolean is_featured() {
        return is_featured;
    }

    public boolean is_prime() {
        return is_prime;
    }

    public String getProgress() {
        return progress;
    }

    public int getOwner() {
        return owner;
    }

    public String[] getSubscriptions() {
        return subscriptions;
    }

    public int getViewed_by() {
        return viewed_by;
    }

    public int getPassed_by() {
        return passed_by;
    }

    public String[] getDependencies() {
        return dependencies;
    }

    public String[] getFollowers() {
        return followers;
    }

    public String getLanguage() {
        return language;
    }

    public boolean is_public() {
        return is_public;
    }

    public String getTitle() {
        return title;
    }

    public String getSlug() {
        return slug;
    }

    public String getCreate_date() {
        return create_date;
    }

    public String getUpdate_date() {
        return update_date;
    }

    public String getLearners_group() {
        return learners_group;
    }

    public String getTeacher_group() {
        return teacher_group;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeLongArray(this.steps);
        dest.writeIntArray(this.tags);
        dest.writeStringArray(this.playlists);
        dest.writeByte(is_featured ? (byte) 1 : (byte) 0);
        dest.writeByte(is_prime ? (byte) 1 : (byte) 0);
        dest.writeString(this.progress);
        dest.writeInt(this.owner);
        dest.writeStringArray(this.subscriptions);
        dest.writeInt(this.viewed_by);
        dest.writeInt(this.passed_by);
        dest.writeStringArray(this.dependencies);
        dest.writeStringArray(this.followers);
        dest.writeString(this.language);
        dest.writeByte(is_public ? (byte) 1 : (byte) 0);
        dest.writeString(this.title);
        dest.writeString(this.slug);
        dest.writeString(this.create_date);
        dest.writeString(this.update_date);
        dest.writeString(this.learners_group);
        dest.writeString(this.teacher_group);
    }

    public Lesson() {
    }

    protected Lesson(Parcel in) {
        this.id = in.readInt();
        this.steps = in.createLongArray();
        this.tags = in.createIntArray();
        this.playlists = in.createStringArray();
        this.is_featured = in.readByte() != 0;
        this.is_prime = in.readByte() != 0;
        this.progress = in.readString();
        this.owner = in.readInt();
        this.subscriptions = in.createStringArray();
        this.viewed_by = in.readInt();
        this.passed_by = in.readInt();
        this.dependencies = in.createStringArray();
        this.followers = in.createStringArray();
        this.language = in.readString();
        this.is_public = in.readByte() != 0;
        this.title = in.readString();
        this.slug = in.readString();
        this.create_date = in.readString();
        this.update_date = in.readString();
        this.learners_group = in.readString();
        this.teacher_group = in.readString();
    }

    public static final Parcelable.Creator<Lesson> CREATOR = new Parcelable.Creator<Lesson>() {
        public Lesson createFromParcel(Parcel source) {
            return new Lesson(source);
        }

        public Lesson[] newArray(int size) {
            return new Lesson[size];
        }
    };
}
