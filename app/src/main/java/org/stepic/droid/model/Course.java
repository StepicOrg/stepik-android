package org.stepic.droid.model;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.yandex.metrica.YandexMetrica;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.stepic.droid.R;
import org.stepic.droid.base.MainApplication;
import org.stepic.droid.configuration.IConfig;

import java.io.Serializable;
import java.util.Locale;

import javax.inject.Inject;

public class Course implements Serializable, Parcelable {

    @Inject
    transient IConfig mConfig;

    private transient Context mContext;

    private transient DateTimeFormatter mFormatForView;

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
    private String language;
    private boolean is_public;
    private String slug; //link to ../course/#slug#
    private boolean is_cached;
    private boolean is_loading;

    @Deprecated
    public boolean is_loading() {
        return is_loading;
    }

    @Deprecated
    public synchronized void setIs_loading(boolean is_loading) {
        this.is_loading = is_loading;
    }

    @Deprecated
    public boolean is_cached() {
        return is_cached;
    }

    @Deprecated
    public synchronized void setIs_cached(boolean is_cached) {
        this.is_cached = is_cached;
    }

    private transient DateTime mBeginDateTime = null;

    private transient DateTime mEndDateTime = null;

    private String formatForView = null;

    public Course() {
        mContext = MainApplication.getAppContext();
        MainApplication.component(MainApplication.getAppContext()).inject(this);

        mFormatForView = DateTimeFormat
                .forPattern(mConfig.getDatePatternForView())
                .withZone(DateTimeZone.getDefault())
                .withLocale(Locale.getDefault());
    }

    public String getDateOfCourse() {
        if (formatForView != null) return formatForView;

        StringBuilder sb = new StringBuilder();

        if (begin_date_source == null && last_deadline == null) {
            sb.append("");
        } else if (last_deadline == null) {
            sb.append(mContext.getResources().getString(R.string.begin_date));
            sb.append(": ");

            try {
                sb.append(getPresentOfDate(begin_date_source));
            } catch (Throwable throwable) {
                YandexMetrica.reportError("present_date_course_begin", throwable);
                return "";
            }
        } else if (begin_date_source != null) {
            //both is not null

            try {

                sb.append(getPresentOfDate(begin_date_source));

                sb.append(" - ");

                sb.append(getPresentOfDate(last_deadline));
            } catch (Throwable throwable) {
                YandexMetrica.reportError("present_date_course_last", throwable);
                return "";
            }
        }
        formatForView = sb.toString();
        return formatForView;
    }

    private String getPresentOfDate(String dateInISOformat) {
        DateTime dateTime = new DateTime(dateInISOformat);
        return mFormatForView.print(dateTime);
    }


    @Nullable
    public DateTime getEndDateTime() {
        if (mEndDateTime != null)
            return mEndDateTime;

        if (last_deadline == null) {
            mEndDateTime = null; //infinity
        } else {
            mEndDateTime = new DateTime(last_deadline);
        }
        return mEndDateTime;

    }

    @Nullable
    public DateTime getBeginDateTime() {
        if (mBeginDateTime != null)
            return mBeginDateTime;

        if (begin_date_source == null) {
            mBeginDateTime = null; //infinity
        } else {
            mBeginDateTime = new DateTime(begin_date_source);
        }
        return mBeginDateTime;
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

    public String getCertificate_footer() {
        return certificate_footer;
    }

    public String getCertificate_cover_org() {
        return certificate_cover_org;
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

    public int getTotal_units() {
        return total_units;
    }

    public int getEnrollment() {
        return enrollment;
    }

    public boolean is_featured() {
        return is_featured;
    }

    public boolean is_spoc() {
        return is_spoc;
    }

    public String getCertificate_link() {
        return certificate_link;
    }

    public long getOwner() {
        return owner;
    }

    public boolean is_contest() {
        return is_contest;
    }

    public String getLanguage() {
        return language;
    }

    public boolean is_public() {
        return is_public;
    }

    public String getSlug() {
        return slug;
    }

    public String getTitle() {
        return title;
    }

    public void setmFormatForView(DateTimeFormatter mFormatForView) {
        this.mFormatForView = mFormatForView;
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

    public void setCertificate_footer(String certificate_footer) {
        this.certificate_footer = certificate_footer;
    }

    public void setCertificate_cover_org(String certificate_cover_org) {
        this.certificate_cover_org = certificate_cover_org;
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

    public void setTotal_units(int total_units) {
        this.total_units = total_units;
    }

    public void setEnrollment(int enrollment) {
        this.enrollment = enrollment;
    }

    public void setOwner(long owner) {
        this.owner = owner;
    }

    public void setIs_contest(boolean is_contest) {
        this.is_contest = is_contest;
    }

    public void setIs_featured(boolean is_featured) {
        this.is_featured = is_featured;
    }

    public void setIs_spoc(boolean is_spoc) {
        this.is_spoc = is_spoc;
    }

    public void setIs_active(boolean is_active) {
        this.is_active = is_active;
    }

    public void setCertificate_link(String certificate_link) {
        this.certificate_link = certificate_link;
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

    public void setIs_public(boolean is_public) {
        this.is_public = is_public;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public void setmBeginDateTime(DateTime mBeginDateTime) {
        this.mBeginDateTime = mBeginDateTime;
    }

    public void setmEndDateTime(DateTime mEndDateTime) {
        this.mEndDateTime = mEndDateTime;
    }

    public void setFormatForView(String formatForView) {
        this.formatForView = formatForView;
    }

    public String getBegin_date_source() {
        return begin_date_source;
    }

    public String getLast_deadline() {
        return last_deadline;
    }


    public long[] getSections() {
        return sections;
    }

    public void setSections(long[] sections) {
        this.sections = sections;
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
        dest.writeByte(is_cached ? (byte) 1 : (byte) 0);
        dest.writeByte(is_loading ? (byte) 1 : (byte) 0);
        dest.writeSerializable(this.mBeginDateTime);
        dest.writeSerializable(this.mEndDateTime);
        dest.writeString(this.formatForView);
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
        this.is_cached = in.readByte() != 0;
        this.is_loading = in.readByte() != 0;
        this.mBeginDateTime = (DateTime) in.readSerializable();
        this.mEndDateTime = (DateTime) in.readSerializable();
        this.formatForView = in.readString();
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
