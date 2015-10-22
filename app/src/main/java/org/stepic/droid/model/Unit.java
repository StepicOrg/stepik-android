package org.stepic.droid.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class Unit implements Serializable, Parcelable {
    private long id;
    private long section;
    private long lesson;
    private long[] assignments;
    private int position;
    private String progress;
    private String begin_date;
    private String end_date;
    private String soft_deadline;
    private String hard_deadline;
    private String grading_policy;
    private String begin_date_source;
    private String end_date_source;
    private String soft_deadline_source;
    private String hard_deadline_source;
    private String grading_policy_source;
    private boolean is_active;
    private String create_date;
    private String update_date;

    public long getId() {
        return id;
    }

    public long getSection() {
        return section;
    }

    public long getLesson() {
        return lesson;
    }

    public long[] getAssignments() {
        return assignments;
    }

    public int getPosition() {
        return position;
    }

    public String getProgress() {
        return progress;
    }

    public String getBegin_date() {
        return begin_date;
    }

    public String getEnd_date() {
        return end_date;
    }

    public String getSoft_deadline() {
        return soft_deadline;
    }

    public String getHard_deadline() {
        return hard_deadline;
    }

    public String getGrading_policy() {
        return grading_policy;
    }

    public String getBegin_date_source() {
        return begin_date_source;
    }

    public String getEnd_date_source() {
        return end_date_source;
    }

    public String getSoft_deadline_source() {
        return soft_deadline_source;
    }

    public String getHard_deadline_source() {
        return hard_deadline_source;
    }

    public String getGrading_policy_source() {
        return grading_policy_source;
    }

    public boolean is_active() {
        return is_active;
    }

    public String getCreate_date() {
        return create_date;
    }

    public String getUpdate_date() {
        return update_date;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setSection(long section) {
        this.section = section;
    }

    public void setLesson(long lesson) {
        this.lesson = lesson;
    }

    public void setAssignments(long[] assignments) {
        this.assignments = assignments;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public void setProgress(String progress) {
        this.progress = progress;
    }

    public void setBegin_date(String begin_date) {
        this.begin_date = begin_date;
    }

    public void setEnd_date(String end_date) {
        this.end_date = end_date;
    }

    public void setSoft_deadline(String soft_deadline) {
        this.soft_deadline = soft_deadline;
    }

    public void setHard_deadline(String hard_deadline) {
        this.hard_deadline = hard_deadline;
    }

    public void setGrading_policy(String grading_policy) {
        this.grading_policy = grading_policy;
    }

    public void setBegin_date_source(String begin_date_source) {
        this.begin_date_source = begin_date_source;
    }

    public void setEnd_date_source(String end_date_source) {
        this.end_date_source = end_date_source;
    }

    public void setSoft_deadline_source(String soft_deadline_source) {
        this.soft_deadline_source = soft_deadline_source;
    }

    public void setHard_deadline_source(String hard_deadline_source) {
        this.hard_deadline_source = hard_deadline_source;
    }

    public void setGrading_policy_source(String grading_policy_source) {
        this.grading_policy_source = grading_policy_source;
    }

    public void setIs_active(boolean is_active) {
        this.is_active = is_active;
    }

    public void setCreate_date(String create_date) {
        this.create_date = create_date;
    }

    public void setUpdate_date(String update_date) {
        this.update_date = update_date;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeLong(this.section);
        dest.writeLong(this.lesson);
        dest.writeLongArray(this.assignments);
        dest.writeInt(this.position);
        dest.writeString(this.progress);
        dest.writeString(this.begin_date);
        dest.writeString(this.end_date);
        dest.writeString(this.soft_deadline);
        dest.writeString(this.hard_deadline);
        dest.writeString(this.grading_policy);
        dest.writeString(this.begin_date_source);
        dest.writeString(this.end_date_source);
        dest.writeString(this.soft_deadline_source);
        dest.writeString(this.hard_deadline_source);
        dest.writeString(this.grading_policy_source);
        dest.writeByte(is_active ? (byte) 1 : (byte) 0);
        dest.writeString(this.create_date);
        dest.writeString(this.update_date);
    }

    public Unit() {
    }

    protected Unit(Parcel in) {
        this.id = in.readLong();
        this.section = in.readLong();
        this.lesson = in.readLong();
        this.assignments = in.createLongArray();
        this.position = in.readInt();
        this.progress = in.readString();
        this.begin_date = in.readString();
        this.end_date = in.readString();
        this.soft_deadline = in.readString();
        this.hard_deadline = in.readString();
        this.grading_policy = in.readString();
        this.begin_date_source = in.readString();
        this.end_date_source = in.readString();
        this.soft_deadline_source = in.readString();
        this.hard_deadline_source = in.readString();
        this.grading_policy_source = in.readString();
        this.is_active = in.readByte() != 0;
        this.create_date = in.readString();
        this.update_date = in.readString();
    }

    public static final Creator<Unit> CREATOR = new Creator<Unit>() {
        public Unit createFromParcel(Parcel source) {
            return new Unit(source);
        }

        public Unit[] newArray(int size) {
            return new Unit[size];
        }
    };
}
