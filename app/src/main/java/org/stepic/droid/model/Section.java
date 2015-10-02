package org.stepic.droid.model;

public class Section {
    private int id;
    private int course; // course id
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCourse() {
        return course;
    }

    public void setCourse(int course) {
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
}
