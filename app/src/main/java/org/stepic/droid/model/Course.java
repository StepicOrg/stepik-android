package org.stepic.droid.model;

import android.content.Context;
import android.util.Log;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.stepic.droid.R;
import org.stepic.droid.base.MainApplication;
import org.stepic.droid.configuration.IConfig;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.joda.time.LocalTime;

import javax.inject.Inject;

public class Course implements Serializable {

    //    private final int mOffsetInMillis;
    @Inject
    IConfig mConfig;
    Context mContext;

//    private DateTimeFormatter mFormatFromServer;
    private DateTimeFormatter mFormatForView;

    public Course() {
        mContext = MainApplication.getAppContext();
        MainApplication.component(MainApplication.getAppContext()).inject(this);

//        mFormatFromServer = DateTimeFormat
//                .forPattern(mConfig.getDatePattern())
//                .withZoneUTC();
        mFormatForView = DateTimeFormat
                .forPattern(mConfig.getDatePatternForView())
                .withZone(DateTimeZone.getDefault());

    }


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
    private boolean is_featured;
    private boolean is_spoc;
    private String certificate_link;
    private String title;
    private String begin_date_source;
    private String last_deadline;


    public String getDateOfCourse() {
        //todo: cache Date interval of course
        StringBuilder sb = new StringBuilder();

        if (begin_date_source == null && last_deadline == null) {
            sb.append("");
        } else if (last_deadline == null) {
            sb.append(mContext.getResources().getString(R.string.begin_date));
            sb.append(": ");

            try {
                sb.append(getPresentOfDate(begin_date_source));
            } catch (Throwable throwable) {
                return "";
            }

        } else if (begin_date_source != null) {
            //both is not null

            try {

                sb.append(getPresentOfDate(begin_date_source));

                sb.append(" - ");

                sb.append(getPresentOfDate(last_deadline));
            } catch (Throwable throwable) {
                return "";
            }
        }

        return sb.toString();
    }

    private String getPresentOfDate (String dateInISOformat) {
        DateTime dateTime = new DateTime(dateInISOformat);
        String result = mFormatForView.print(dateTime);
        return result;
    }

    public long getId() {
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

    public String getTitle() {
        return title;
    }
}
