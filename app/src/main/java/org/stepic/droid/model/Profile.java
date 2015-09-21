package org.stepic.droid.model;

import java.io.Serializable;

public class Profile implements Serializable {
    private long id;
    private long bit_field;
    private long level;
    private String first_name;
    private String last_name;
    private Boolean is_private;
    private String avatar;
    private String language;
    private String short_bio;
    private String details;
    private String notification_email_delay;
    private String level_title;
    private boolean subscribed_for_mail;
    private boolean is_staff;
    private boolean is_guest;
    private boolean can_add_lesson;
    private boolean can_add_course;
    private boolean can_add_group;
    private boolean subscribed_for_news_en;
    private boolean subscribed_for_news_ru;

    public long getId() {
        return id;
    }

    public long getBit_field() {
        return bit_field;
    }

    public long getLevel() {
        return level;
    }

    public String getFirst_name() {
        return first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public Boolean getIs_private() {
        return is_private;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getLanguage() {
        return language;
    }

    public String getShort_bio() {
        return short_bio;
    }

    public String getDetails() {
        return details;
    }

    public String getNotification_email_delay() {
        return notification_email_delay;
    }

    public String getLevel_title() {
        return level_title;
    }

    public boolean isSubscribed_for_mail() {
        return subscribed_for_mail;
    }

    public boolean is_staff() {
        return is_staff;
    }

    public boolean is_guest() {
        return is_guest;
    }

    public boolean isCan_add_lesson() {
        return can_add_lesson;
    }

    public boolean isCan_add_course() {
        return can_add_course;
    }

    public boolean isCan_add_group() {
        return can_add_group;
    }

    public boolean isSubscribed_for_news_en() {
        return subscribed_for_news_en;
    }

    public boolean isSubscribed_for_news_ru() {
        return subscribed_for_news_ru;
    }
}
