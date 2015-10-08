package org.stepic.droid.model;

public class Lesson {
    private int id;
    private int[] steps;
    //    private String actions;
    private int[] tags;
    private String playlists[];
    private boolean is_featured;
    private boolean is_prime;
    private String progress;
    private int owner;
    private String[] subscriptions;
    private int viewed_by;
    private int passed_by;
    private String[] dependencies;
    private String[] followers;
    private String language;
    private boolean is_public;
    private String title;
    private String slug;
    private String create_date;
    private String update_date;
    private String learners_group;
    private String teacher_group;

    public int getId() {
        return id;
    }

    public int[] getSteps() {
        return steps;
    }

    public int[] getTags() {
        return tags;
    }

    public String[] getPlaylists() {
        return playlists;
    }

    public boolean is_featured() {
        return is_featured;
    }

    public boolean is_prime() {
        return is_prime;
    }

    public String getProgress() {
        return progress;
    }

    public int getOwner() {
        return owner;
    }

    public String[] getSubscriptions() {
        return subscriptions;
    }

    public int getViewed_by() {
        return viewed_by;
    }

    public int getPassed_by() {
        return passed_by;
    }

    public String[] getDependencies() {
        return dependencies;
    }

    public String[] getFollowers() {
        return followers;
    }

    public String getLanguage() {
        return language;
    }

    public boolean is_public() {
        return is_public;
    }

    public String getTitle() {
        return title;
    }

    public String getSlug() {
        return slug;
    }

    public String getCreate_date() {
        return create_date;
    }

    public String getUpdate_date() {
        return update_date;
    }

    public String getLearners_group() {
        return learners_group;
    }

    public String getTeacher_group() {
        return teacher_group;
    }
}
