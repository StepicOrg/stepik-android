package org.stepic.droid.model;

public class SearchResult {

    String id;
    String score; // it is not String, but let String

    //type=course
    long course;
    String course_cover;
    String course_owner; // it is number
    String course_title;
    String course_slug;

    //type=lesson
    long lesson;
    String lesson_title;
    String lesson_slug;
    String lesson_owner;
    String lesson_cover_url;

    //type=step
    long step;
    int step_position;

    //type=comment
    long comment;
    long comment_parent;
    long comment_user;
    String comment_text;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public long getCourse() {
        return course;
    }

    public void setCourse(long course) {
        this.course = course;
    }

    public String getCourse_cover() {
        return course_cover;
    }

    public void setCourse_cover(String course_cover) {
        this.course_cover = course_cover;
    }

    public String getCourse_owner() {
        return course_owner;
    }

    public void setCourse_owner(String course_owner) {
        this.course_owner = course_owner;
    }

    public String getCourse_title() {
        return course_title;
    }

    public void setCourse_title(String course_title) {
        this.course_title = course_title;
    }

    public String getCourse_slug() {
        return course_slug;
    }

    public void setCourse_slug(String course_slug) {
        this.course_slug = course_slug;
    }

    public long getLesson() {
        return lesson;
    }

    public void setLesson(long lesson) {
        this.lesson = lesson;
    }

    public String getLesson_title() {
        return lesson_title;
    }

    public void setLesson_title(String lesson_title) {
        this.lesson_title = lesson_title;
    }

    public String getLesson_slug() {
        return lesson_slug;
    }

    public void setLesson_slug(String lesson_slug) {
        this.lesson_slug = lesson_slug;
    }

    public String getLesson_owner() {
        return lesson_owner;
    }

    public void setLesson_owner(String lesson_owner) {
        this.lesson_owner = lesson_owner;
    }

    public String getLesson_cover_url() {
        return lesson_cover_url;
    }

    public void setLesson_cover_url(String lesson_cover_url) {
        this.lesson_cover_url = lesson_cover_url;
    }

    public long getStep() {
        return step;
    }

    public void setStep(long step) {
        this.step = step;
    }

    public int getStep_position() {
        return step_position;
    }

    public void setStep_position(int step_position) {
        this.step_position = step_position;
    }

    public long getComment() {
        return comment;
    }

    public void setComment(long comment) {
        this.comment = comment;
    }

    public long getComment_parent() {
        return comment_parent;
    }

    public void setComment_parent(long comment_parent) {
        this.comment_parent = comment_parent;
    }

    public long getComment_user() {
        return comment_user;
    }

    public void setComment_user(long comment_user) {
        this.comment_user = comment_user;
    }

    public String getComment_text() {
        return comment_text;
    }

    public void setComment_text(String comment_text) {
        this.comment_text = comment_text;
    }
}
