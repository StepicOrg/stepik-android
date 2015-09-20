package org.stepic.droid.model;

import android.content.Context;

import org.stepic.droid.R;
import org.stepic.droid.base.MainApplication;
import org.stepic.droid.configuration.IConfig;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.inject.Inject;

public class Course implements Serializable {

    @Inject
    IConfig mConfig;
    Context mContext;

    private SimpleDateFormat mFormatFromServer;
    private SimpleDateFormat mFormatForView;
    private TimeZone mTimeZone;

    public Course() {
        mContext = MainApplication.getAppContext();
        MainApplication.component(MainApplication.getAppContext()).inject(this);
        mTimeZone = TimeZone.getDefault();
        mFormatFromServer = new SimpleDateFormat(mConfig.getDatePattern());
        mFormatFromServer.setTimeZone(mTimeZone);
        mFormatForView = new SimpleDateFormat(mConfig.getDatePatternForView(), Locale.getDefault());

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
            Date from;
            try {
                from = mFormatFromServer.parse(begin_date_source);
                String from_str = mFormatForView.format(from);
                sb.append(from_str);
            } catch (ParseException e) {
                return "";
            }

        } else if (begin_date_source != null) {
            //both is not null
            Date from = null, to = null;
            try {
                from = mFormatFromServer.parse(begin_date_source);
                String from_str = mFormatForView.format(from);
                sb.append(from_str);

                sb.append(" - ");

                to = mFormatFromServer.parse(last_deadline);
                String to_str = mFormatForView.format(to);
                sb.append(to_str);

            } catch (ParseException e) {
                return "";
            }
        }


        return sb.toString();
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
