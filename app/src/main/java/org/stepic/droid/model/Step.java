package org.stepic.droid.model;

public class Step {
    private int id;
    private int lesson;
    private String status;
    private Block block;
    private String progress;
    private String[] subscriptions;
    private long viewed_by;
    private long passed_by;
    private String create_date;
    private String update_date;

    public int getId() {
        return id;
    }

    public int getLesson() {
        return lesson;
    }

    public String getStatus() {
        return status;
    }

    public Block getBlock() {
        return block;
    }

    public String getProgress() {
        return progress;
    }

    public String[] getSubscriptions() {
        return subscriptions;
    }

    public long getViewed_by() {
        return viewed_by;
    }

    public long getPassed_by() {
        return passed_by;
    }

    public String getCreate_date() {
        return create_date;
    }

    public String getUpdate_date() {
        return update_date;
    }
}
