package org.stepic.droid.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import org.jetbrains.annotations.Nullable;
import org.stepik.android.model.structure.Progress;
import org.stepik.android.model.structure.Video;

public final class Course implements Parcelable {
    private long id;
    private String summary;
    private String workload;
    private String cover;
    private String intro;
    @SerializedName("course_format")
    private String courseFormat;
    @SerializedName("target_audience")
    private String targetAudience;
    @SerializedName("certificate_footer")
    private String certificateFooter;
    @SerializedName("certificate_cover_org")
    private String certificateCoverOrg;
    private long[] instructors;
    @Nullable
    private long[] sections;
    private String certificate;
    private String requirements;
    private String description;
    @SerializedName("total_units")
    private int totalUnits;
    private int enrollment;
    private long owner;
    @SerializedName("is_contest")
    private boolean isContest;
    @SerializedName("is_featured")
    private boolean isFeatured;
    @SerializedName("is_spoc")
    private boolean isSpoc;
    @SerializedName("is_active")
    private boolean isActive;
    @SerializedName("certificate_link")
    private String certificateLink;
    private String title;
    @SerializedName("last_deadline")
    private String lastDeadline;
    @SerializedName("begin_date")
    private String beginDate;
    @SerializedName("end_date")
    private String endDate;
    private String language;
    @SerializedName("is_public")
    private boolean isPublic;
    private String slug; //link to ../course/#slug#
    @SerializedName("intro_video")
    private Video introVideo;
    @SerializedName("intro_video_id")
    private long introVideoId;
    @SerializedName("schedule_link")
    private String scheduleLink;
    @SerializedName("schedule_long_link")
    private String scheduleLongLink;
    @SerializedName("last_step")
    private String lastStepId;
    @SerializedName("learners_count")
    private long learnersCount;
    @Nullable
    private String progress;
    @SerializedName("review_summary")
    private int reviewSummary;

    private Progress progressObject;
    private double rating;

    public Course() {

    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public int getReviewSummary() {
        return reviewSummary;
    }

    public void setReviewSummary(int reviewSummary) {
        this.reviewSummary = reviewSummary;
    }

    @Nullable
    public Progress getProgressObject() {
        return progressObject;
    }

    public void setProgressObject(Progress progressObject) {
        this.progressObject = progressObject;
    }

    @Nullable
    public String getProgress() {
        return progress;
    }

    public void setProgress(@Nullable String progress) {
        this.progress = progress;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getBeginDate() {
        return beginDate;
    }

    @Nullable
    public String getLastStepId() {
        return lastStepId;
    }

    public void setLastStepId(String lastStepId) {
        this.lastStepId = lastStepId;
    }

    public void setBeginDate(String beginDate) {
        this.beginDate = beginDate;
    }

    public String getScheduleLink() {
        return scheduleLink;
    }

    public void setScheduleLink(String scheduleLink) {
        this.scheduleLink = scheduleLink;
    }

    public String getScheduleLongLink() {
        return scheduleLongLink;
    }

    public void setScheduleLongLink(String scheduleLongLink) {
        this.scheduleLongLink = scheduleLongLink;
    }

    public void setIntroVideo(Video introVideo) {
        this.introVideo = introVideo;
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

    public String getCourseFormat() {
        return courseFormat;
    }

    public String getTargetAudience() {
        return targetAudience;
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

    public long getLearnersCount() {
        return learnersCount;
    }

    public void setLearnersCount(long learnersCount) {
        this.learnersCount = learnersCount;
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

    public void setCourseFormat(String courseFormat) {
        this.courseFormat = courseFormat;
    }

    public void setTargetAudience(String targetAudience) {
        this.targetAudience = targetAudience;
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

    public void setLastDeadline(String lastDeadline) {
        this.lastDeadline = lastDeadline;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getLastDeadline() {
        return lastDeadline;
    }

    public long getIntroVideoId() {
        return introVideoId;
    }

    public void setIntroVideoId(long introVideoId) {
        this.introVideoId = introVideoId;
    }

    @Nullable
    public long[] getSections() {
        return sections;
    }

    public void setSections(@Nullable long[] sections) {
        this.sections = sections;
    }

    public Video getIntroVideo() {
        return introVideo;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        this.isActive = active;
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
        dest.writeString(this.courseFormat);
        dest.writeString(this.targetAudience);
        dest.writeString(this.certificateFooter);
        dest.writeString(this.certificateCoverOrg);
        dest.writeLongArray(this.instructors);
        dest.writeLongArray(this.sections);
        dest.writeString(this.certificate);
        dest.writeString(this.requirements);
        dest.writeString(this.description);
        dest.writeInt(this.totalUnits);
        dest.writeInt(this.enrollment);
        dest.writeLong(this.owner);
        dest.writeByte(isContest ? (byte) 1 : (byte) 0);
        dest.writeByte(isFeatured ? (byte) 1 : (byte) 0);
        dest.writeByte(isSpoc ? (byte) 1 : (byte) 0);
        dest.writeByte(isActive ? (byte) 1 : (byte) 0);
        dest.writeString(this.certificateLink);
        dest.writeString(this.title);
        dest.writeString(this.lastDeadline);
        dest.writeString(this.language);
        dest.writeByte(isPublic ? (byte) 1 : (byte) 0);
        dest.writeString(this.slug);
        dest.writeParcelable(this.introVideo, 0);
        dest.writeLong(this.introVideoId);
        dest.writeString(scheduleLink);
        dest.writeString(scheduleLongLink);
        dest.writeString(beginDate);
        dest.writeString(endDate);
        dest.writeString(lastStepId);
        dest.writeLong(learnersCount);
        dest.writeString(progress);
        dest.writeParcelable(progressObject, flags);
        dest.writeDouble(rating);
        dest.writeInt(reviewSummary);
    }

    protected Course(Parcel in) {
        this.id = in.readLong();
        this.summary = in.readString();
        this.workload = in.readString();
        this.cover = in.readString();
        this.intro = in.readString();
        this.courseFormat = in.readString();
        this.targetAudience = in.readString();
        this.certificateFooter = in.readString();
        this.certificateCoverOrg = in.readString();
        this.instructors = in.createLongArray();
        this.sections = in.createLongArray();
        this.certificate = in.readString();
        this.requirements = in.readString();
        this.description = in.readString();
        this.totalUnits = in.readInt();
        this.enrollment = in.readInt();
        this.owner = in.readLong();
        this.isContest = in.readByte() != 0;
        this.isFeatured = in.readByte() != 0;
        this.isSpoc = in.readByte() != 0;
        this.isActive = in.readByte() != 0;
        this.certificateLink = in.readString();
        this.title = in.readString();
        this.lastDeadline = in.readString();
        this.language = in.readString();
        this.isPublic = in.readByte() != 0;
        this.slug = in.readString();
        this.introVideo = in.readParcelable(Video.class.getClassLoader());
        this.introVideoId = in.readLong();
        scheduleLink = in.readString();
        scheduleLongLink = in.readString();
        beginDate = in.readString();
        endDate = in.readString();
        lastStepId = in.readString();
        learnersCount = in.readLong();
        progress = in.readString();
        progressObject = in.readParcelable(Progress.class.getClassLoader());
        rating = in.readDouble();
        reviewSummary = in.readInt();
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
