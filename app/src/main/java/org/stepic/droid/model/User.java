package org.stepic.droid.model;

public class User {

    private int id;
    private int profile;
    private boolean is_private;
    private String details;
    private String first_name;
    private String last_name;
    private String avatar;
    private String level_title;
    private int level;
    private int score_learn;
    private int score_teach;
    private int[] leaders;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getProfile() {
        return profile;
    }

    public void setProfile(int profile) {
        this.profile = profile;
    }

    public boolean is_private() {
        return is_private;
    }

    public void setIs_private(boolean is_private) {
        this.is_private = is_private;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getLevel_title() {
        return level_title;
    }

    public void setLevel_title(String level_title) {
        this.level_title = level_title;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getScore_learn() {
        return score_learn;
    }

    public void setScore_learn(int score_learn) {
        this.score_learn = score_learn;
    }

    public int getScore_teach() {
        return score_teach;
    }

    public void setScore_teach(int score_teach) {
        this.score_teach = score_teach;
    }

    public int[] getLeaders() {
        return leaders;
    }

    public void setLeaders(int[] leaders) {
        this.leaders = leaders;
    }
}
