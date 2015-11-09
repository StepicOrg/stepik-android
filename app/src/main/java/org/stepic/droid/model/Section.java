package org.stepic.droid.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.stepic.droid.base.MainApplication;
import org.stepic.droid.configuration.IConfig;
import org.stepic.droid.util.DateTimeHelper;

import java.io.Serializable;
import java.util.Locale;

import javax.inject.Inject;

public class Section implements Serializable, Parcelable {

    @Inject
    IConfig mConfig;

    private DateTimeFormatter mFormatForView;

    private long id;
    private long course; // course id
    private long[] units;
    private int position;
    private String progress;
    private String title;
    private String slug;
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
    private boolean is_cached;
    private boolean is_loading;

    public boolean is_loading() {
        return is_loading;
    }

    public void setIs_loading(boolean is_loading) {
        this.is_loading = is_loading;
    }

    public boolean is_cached() {
        return is_cached;
    }

    public void setIs_cached(boolean is_cached) {
        this.is_cached = is_cached;
    }

    private DateTime mBeginDateTime = null;
    private DateTime mSoftDeadline = null;
    private DateTime mHardDeadline = null;

    private String formatted_begin_date;
    private String formatted_soft_deadline;
    private String formatted_hard_deadline;

    public Section() {
        MainApplication.component(MainApplication.getAppContext()).inject(this);

        mFormatForView = DateTimeFormat
                .forPattern(mConfig.getDatePattern())
                .withZone(DateTimeZone.getDefault())
                .withLocale(Locale.getDefault());
    }

    public String getFormattedBeginDate() {
        if (formatted_begin_date != null) return formatted_begin_date;
        formatted_begin_date = DateTimeHelper.getPresentOfDate(begin_date, mFormatForView, mBeginDateTime);
        return formatted_begin_date;
    }

    public String getFormattedSoftDeadline() {
        if (formatted_soft_deadline != null) return formatted_soft_deadline;
        formatted_soft_deadline = DateTimeHelper.getPresentOfDate(soft_deadline, mFormatForView, mSoftDeadline);
        return formatted_soft_deadline;
    }

    public String getFormattedHardDeadline() {
        if (formatted_hard_deadline != null) return formatted_hard_deadline;
        formatted_hard_deadline = DateTimeHelper.getPresentOfDate(hard_deadline, mFormatForView, mHardDeadline);
        return formatted_hard_deadline;
    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getCourse() {
        return course;
    }

    public void setCourse(long course) {
        this.course = course;
    }

    public long[] getUnits() {
        return units;
    }

    public void setUnits(long[] units) {
        this.units = units;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getProgress() {
        return progress;
    }

    public void setProgress(String progress) {
        this.progress = progress;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getBegin_date() {
        return begin_date;
    }

    public void setBegin_date(String begin_date) {
        this.begin_date = begin_date;
    }

    public String getEnd_date() {
        return end_date;
    }

    public void setEnd_date(String end_date) {
        this.end_date = end_date;
    }

    public String getSoft_deadline() {
        return soft_deadline;
    }

    public void setSoft_deadline(String soft_deadline) {
        this.soft_deadline = soft_deadline;
    }

    public String getHard_deadline() {
        return hard_deadline;
    }

    public void setHard_deadline(String hard_deadline) {
        this.hard_deadline = hard_deadline;
    }

    public String getGrading_policy() {
        return grading_policy;
    }

    public void setGrading_policy(String grading_policy) {
        this.grading_policy = grading_policy;
    }

    public String getBegin_date_source() {
        return begin_date_source;
    }

    public void setBegin_date_source(String begin_date_source) {
        this.begin_date_source = begin_date_source;
    }

    public String getEnd_date_source() {
        return end_date_source;
    }

    public void setEnd_date_source(String end_date_source) {
        this.end_date_source = end_date_source;
    }

    public String getSoft_deadline_source() {
        return soft_deadline_source;
    }

    public void setSoft_deadline_source(String soft_deadline_source) {
        this.soft_deadline_source = soft_deadline_source;
    }

    public String getHard_deadline_source() {
        return hard_deadline_source;
    }

    public void setHard_deadline_source(String hard_deadline_source) {
        this.hard_deadline_source = hard_deadline_source;
    }

    public String getGrading_policy_source() {
        return grading_policy_source;
    }

    public void setGrading_policy_source(String grading_policy_source) {
        this.grading_policy_source = grading_policy_source;
    }

    public boolean is_active() {
        return is_active;
    }

    public void setIs_active(boolean is_active) {
        this.is_active = is_active;
    }

    public String getCreate_date() {
        return create_date;
    }

    public void setCreate_date(String create_date) {
        this.create_date = create_date;
    }

    public String getUpdate_date() {
        return update_date;
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
        dest.writeLong(this.course);
        dest.writeLongArray(this.units);
        dest.writeInt(this.position);
        dest.writeString(this.progress);
        dest.writeString(this.title);
        dest.writeString(this.slug);
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
        dest.writeByte(is_cached ? (byte) 1 : (byte) 0);
        dest.writeByte(is_loading ? (byte) 1 : (byte) 0);
        dest.writeSerializable(this.mBeginDateTime);
        dest.writeSerializable(this.mSoftDeadline);
        dest.writeSerializable(this.mHardDeadline);
        dest.writeString(this.formatted_begin_date);
        dest.writeString(this.formatted_soft_deadline);
        dest.writeString(this.formatted_hard_deadline);
    }

    protected Section(Parcel in) {
        this.id = in.readLong();
        this.course = in.readLong();
        this.units = in.createLongArray();
        this.position = in.readInt();
        this.progress = in.readString();
        this.title = in.readString();
        this.slug = in.readString();
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
        this.is_cached = in.readByte() != 0;
        this.is_loading = in.readByte() != 0;
        this.mBeginDateTime = (DateTime) in.readSerializable();
        this.mSoftDeadline = (DateTime) in.readSerializable();
        this.mHardDeadline = (DateTime) in.readSerializable();
        this.formatted_begin_date = in.readString();
        this.formatted_soft_deadline = in.readString();
        this.formatted_hard_deadline = in.readString();
    }

    public static final Creator<Section> CREATOR = new Creator<Section>() {
        public Section createFromParcel(Parcel source) {
            return new Section(source);
        }

        public Section[] newArray(int size) {
            return new Section[size];
        }
    };
}
