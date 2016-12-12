package org.stepic.droid.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import org.jetbrains.annotations.Nullable;

import java.io.Serializable;

public class Course implements Serializable, Parcelable {
    private long id;
    private String summary;
    private String workload;
    private String cover;
    private String intro;
    private String course_format;
    private String target_audience;
    private String certificate_footer;
    private String certificate_cover_org;
    private long[] instructors;
    private long[] sections;
    private String certificate;
    private String requirements;
    private String description;
    private int total_units;
    private int enrollment;
    private long owner;
    private boolean is_contest;
    private boolean is_featured;
    private boolean is_spoc;
    private boolean is_active;
    private String certificate_link;
    private String title;
    private String begin_date_source;
    private String last_deadline;
    private String begin_date;
    private String end_date;
    private String language;
    private boolean is_public;
    private String slug; //link to ../course/#slug#
    private Video intro_video;
    private long intro_video_id;
    private String schedule_link;
    private String schedule_long_link;
    @SerializedName("last_step")
    private String lastStepId;

    public Course() {

    }

    public String getEnd_date() {
        return end_date;
    }

    public void setEnd_date(String end_date) {
        this.end_date = end_date;
    }

    public String getBegin_date() {
        return begin_date;
    }

    @Nullable
    public String getLastStepId() {
        return lastStepId;
    }

    public void setLastStepId(String lastStepId) {
        this.lastStepId = lastStepId;
    }

    public void setBegin_date(String begin_date) {
        this.begin_date = begin_date;
    }

    public String getSchedule_link() {
        return schedule_link;
    }

    public void setSchedule_link(String schedule_link) {
        this.schedule_link = schedule_link;
    }

    public String getSchedule_long_link() {
        return schedule_long_link;
    }

    public void setSchedule_long_link(String schedule_long_link) {
        this.schedule_long_link = schedule_long_link;
    }

    public void setIntro_video(Video intro_video) {
        this.intro_video = intro_video;
    }

    public long getCourseId() {
        return id;
    }

    public String getSummary() {
        return summary;
    }

    public String getWorkload() {
        return workload;
    }

    public String getCover() {
        return cover;
    }

    public String getIntro() {
        return intro;
    }

    public String getCourse_format() {
        return course_format;
    }

    public String getTarget_audience() {
        return target_audience;
    }

    public long[] getInstructors() {
        return instructors;
    }

    public String getCertificate() {
        return certificate;
    }

    public String getRequirements() {
        return requirements;
    }

    public String getDescription() {
        return description;
    }

    public int getEnrollment() {
        return enrollment;
    }

    public long getOwner() {
        return owner;
    }

    public String getLanguage() {
        return language;
    }

    public String getSlug() {
        return slug;
    }

    public String getTitle() {
        return title;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public void setWorkload(String workload) {
        this.workload = workload;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public void setIntro(String intro) {
        this.intro = intro;
    }

    public void setCourse_format(String course_format) {
        this.course_format = course_format;
    }

    public void setTarget_audience(String target_audience) {
        this.target_audience = target_audience;
    }

    public void setInstructors(long[] instructors) {
        this.instructors = instructors;
    }

    public void setCertificate(String certificate) {
        this.certificate = certificate;
    }

    public void setRequirements(String requirements) {
        this.requirements = requirements;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setEnrollment(int enrollment) {
        this.enrollment = enrollment;
    }

    public void setOwner(long owner) {
        this.owner = owner;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setBegin_date_source(String begin_date_source) {
        this.begin_date_source = begin_date_source;
    }

    public void setLast_deadline(String last_deadline) {
        this.last_deadline = last_deadline;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getBegin_date_source() {
        return begin_date_source;
    }

    public String getLast_deadline() {
        return last_deadline;
    }

    public long getIntro_video_id() {
        return intro_video_id;
    }

    public void setIntro_video_id(long intro_video_id) {
        this.intro_video_id = intro_video_id;
    }

    public long[] getSections() {
        return sections;
    }

    public void setSections(long[] sections) {
        this.sections = sections;
    }

    public Video getIntro_video() {
        return intro_video;
    }

    public boolean is_active() {
        return is_active;
    }

    public void setIs_active(boolean is_active) {
        this.is_active = is_active;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeString(this.summary);
        dest.writeString(this.workload);
        dest.writeString(this.cover);
        dest.writeString(this.intro);
        dest.writeString(this.course_format);
        dest.writeString(this.target_audience);
        dest.writeString(this.certificate_footer);
        dest.writeString(this.certificate_cover_org);
        dest.writeLongArray(this.instructors);
        dest.writeLongArray(this.sections);
        dest.writeString(this.certificate);
        dest.writeString(this.requirements);
        dest.writeString(this.description);
        dest.writeInt(this.total_units);
        dest.writeInt(this.enrollment);
        dest.writeLong(this.owner);
        dest.writeByte(is_contest ? (byte) 1 : (byte) 0);
        dest.writeByte(is_featured ? (byte) 1 : (byte) 0);
        dest.writeByte(is_spoc ? (byte) 1 : (byte) 0);
        dest.writeByte(is_active ? (byte) 1 : (byte) 0);
        dest.writeString(this.certificate_link);
        dest.writeString(this.title);
        dest.writeString(this.begin_date_source);
        dest.writeString(this.last_deadline);
        dest.writeString(this.language);
        dest.writeByte(is_public ? (byte) 1 : (byte) 0);
        dest.writeString(this.slug);
        dest.writeParcelable(this.intro_video, 0);
        dest.writeLong(this.intro_video_id);
        dest.writeString(schedule_link);
        dest.writeString(schedule_long_link);
        dest.writeString(begin_date);
        dest.writeString(end_date);
        dest.writeString(lastStepId);
    }

    protected Course(Parcel in) {
        this.id = in.readLong();
        this.summary = in.readString();
        this.workload = in.readString();
        this.cover = in.readString();
        this.intro = in.readString();
        this.course_format = in.readString();
        this.target_audience = in.readString();
        this.certificate_footer = in.readString();
        this.certificate_cover_org = in.readString();
        this.instructors = in.createLongArray();
        this.sections = in.createLongArray();
        this.certificate = in.readString();
        this.requirements = in.readString();
        this.description = in.readString();
        this.total_units = in.readInt();
        this.enrollment = in.readInt();
        this.owner = in.readLong();
        this.is_contest = in.readByte() != 0;
        this.is_featured = in.readByte() != 0;
        this.is_spoc = in.readByte() != 0;
        this.is_active = in.readByte() != 0;
        this.certificate_link = in.readString();
        this.title = in.readString();
        this.begin_date_source = in.readString();
        this.last_deadline = in.readString();
        this.language = in.readString();
        this.is_public = in.readByte() != 0;
        this.slug = in.readString();
        this.intro_video = in.readParcelable(Video.class.getClassLoader());
        this.intro_video_id = in.readLong();
        schedule_link = in.readString();
        schedule_long_link = in.readString();
        begin_date = in.readString();
        end_date = in.readString();
        lastStepId = in.readString();
    }

    public static final Creator<Course> CREATOR = new Creator<Course>() {
        public Course createFromParcel(Parcel source) {
            return new Course(source);
        }

        public Course[] newArray(int size) {
            return new Course[size];
        }
    };
}
