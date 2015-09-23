package org.stepic.droid.model;

import android.content.Context;
import android.support.annotation.Nullable;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.stepic.droid.R;
import org.stepic.droid.base.MainApplication;
import org.stepic.droid.configuration.IConfig;

import java.io.Serializable;

import javax.inject.Inject;

public class Course implements Serializable {

    @Inject
    IConfig mConfig;

    Context mContext;

    private DateTimeFormatter mFormatForView;

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

    private DateTime mBeginDateTime = null;

    private DateTime mEndDateTime = null;

    private String formatForView = null;

    public Course() {
        mContext = MainApplication.getAppContext();
        MainApplication.component(MainApplication.getAppContext()).inject(this);

        mFormatForView = DateTimeFormat
                .forPattern(mConfig.getDatePatternForView())
                .withZone(DateTimeZone.getDefault());
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
                sb.append(getPresentOfDate(begin_date_source, mBeginDateTime));
            } catch (Throwable throwable) {
                return "";
            }
        } else if (begin_date_source != null) {
            //both is not null

            try {

                sb.append(getPresentOfDate(begin_date_source, mBeginDateTime));

                sb.append(" - ");

                sb.append(getPresentOfDate(last_deadline, mEndDateTime));
            } catch (Throwable throwable) {
                return "";
            }
        }
        formatForView = sb.toString();
        return formatForView;
    }

    private String getPresentOfDate(String dateInISOformat, DateTime dateTime) {
        dateTime = new DateTime(dateInISOformat);
        String result = mFormatForView.print(dateTime);
        return result;
    }


    @Nullable
    public DateTime getEndDateTime() {
        if (mEndDateTime != null)
            return mEndDateTime;

        if (last_deadline == null)
        {
            mEndDateTime = null; //infinity
        }
        else
        {
            mEndDateTime = new DateTime(last_deadline);
        }
        return mEndDateTime;

    }

    @Nullable
    public DateTime getBeginDateTime() {
        if (mBeginDateTime != null)
            return mBeginDateTime;

        if (begin_date_source == null)
        {
            mBeginDateTime = null; //infinity
        }
        else
        {
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
}
