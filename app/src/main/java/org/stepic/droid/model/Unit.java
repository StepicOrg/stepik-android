package org.stepic.droid.model;

public class Unit {
    private long id;
    private int section;
    private long lessons;
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

    public int getSection() {
        return section;
    }

    public long getLessons() {
        return lessons;
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
}
