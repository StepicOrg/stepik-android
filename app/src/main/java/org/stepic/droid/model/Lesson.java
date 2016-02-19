package org.stepic.droid.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class Lesson implements Parcelable, Serializable {
    private long id;
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
    private boolean is_cached;
    private boolean is_loading;
    private String cover_url;

    public boolean is_loading() {
        return is_loading;
    }

    public synchronized void setIs_loading(boolean is_loading) {
        this.is_loading = is_loading;
    }

    public boolean is_cached() {
        return is_cached;
    }

    public synchronized void setIs_cached(boolean is_cached) {
        this.is_cached = is_cached;
    }

    public long getId() {
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

    public void setId(long id) {
        this.id = id;
    }

    public void setSteps(long[] steps) {
        this.steps = steps;
    }

    public void setTags(int[] tags) {
        this.tags = tags;
    }

    public void setPlaylists(String[] playlists) {
        this.playlists = playlists;
    }

    public void setIs_featured(boolean is_featured) {
        this.is_featured = is_featured;
    }

    public void setIs_prime(boolean is_prime) {
        this.is_prime = is_prime;
    }

    public void setProgress(String progress) {
        this.progress = progress;
    }

    public void setOwner(int owner) {
        this.owner = owner;
    }

    public void setSubscriptions(String[] subscriptions) {
        this.subscriptions = subscriptions;
    }

    public void setViewed_by(int viewed_by) {
        this.viewed_by = viewed_by;
    }

    public void setPassed_by(int passed_by) {
        this.passed_by = passed_by;
    }

    public void setDependencies(String[] dependencies) {
        this.dependencies = dependencies;
    }

    public void setFollowers(String[] followers) {
        this.followers = followers;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setIs_public(boolean is_public) {
        this.is_public = is_public;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public void setCreate_date(String create_date) {
        this.create_date = create_date;
    }

    public void setUpdate_date(String update_date) {
        this.update_date = update_date;
    }

    public void setLearners_group(String learners_group) {
        this.learners_group = learners_group;
    }

    public void setTeacher_group(String teacher_group) {
        this.teacher_group = teacher_group;
    }

    public String getCover_url() {
        return cover_url;
    }

    public void setCover_url(String cover_url) {
        this.cover_url = cover_url;
    }

    public Lesson() {
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
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
        dest.writeByte(is_cached ? (byte) 1 : (byte) 0);
        dest.writeByte(is_loading ? (byte) 1 : (byte) 0);
        dest.writeString(this.cover_url);
    }

    protected Lesson(Parcel in) {
        this.id = in.readLong();
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
        this.is_cached = in.readByte() != 0;
        this.is_loading = in.readByte() != 0;
        this.cover_url = in.readString();
    }

    public static final Creator<Lesson> CREATOR = new Creator<Lesson>() {
        public Lesson createFromParcel(Parcel source) {
            return new Lesson(source);
        }

        public Lesson[] newArray(int size) {
            return new Lesson[size];
        }
    };
}
