package org.stepic.droid.model;

public class Attempt {
    private long id;
    private DatasetBase dataset;
    private String dataset_url;
    private String time;
    private String status;
    private String time_left;
    private long step;
    private long user;

    public Attempt() {
    }

    public Attempt(long step) {
        this.step = step;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public DatasetBase getDataset() {
        return dataset;
    }

    public void setDataset(DatasetBase dataset) {
        this.dataset = dataset;
    }

    public String getDataset_url() {
        return dataset_url;
    }

    public void setDataset_url(String dataset_url) {
        this.dataset_url = dataset_url;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTime_left() {
        return time_left;
    }

    public void setTime_left(String time_left) {
        this.time_left = time_left;
    }

    public long getStep() {
        return step;
    }

    public void setStep(long step) {
        this.step = step;
    }

    public long getUser() {
        return user;
    }

    public void setUser(long user) {
        this.user = user;
    }
}
