package org.stepic.droid.model;

public class CourseProperty {
    private final String title;
    private final String text;

    public CourseProperty(String title, String text) {
        this.title = title;
        this.text = text;
    }

    public String getTitle() {
        return title;
    }

    public String getText() {
        return text;
    }
}
