package org.stepic.droid.model;

public class DownloadEntity {
    private long downloadId;
    private long stepId;
    private long videoId;

    public DownloadEntity() {
    }

    public DownloadEntity(long downloadId, long stepId, long videoId) {
        this.downloadId = downloadId;
        this.stepId = stepId;
        this.videoId = videoId;
    }

    public void setDownloadId(long downloadId) {
        this.downloadId = downloadId;
    }

    public void setStepId(long stepId) {
        this.stepId = stepId;
    }

    public void setVideoId(long videoId) {
        this.videoId = videoId;
    }

    public long getDownloadId() {
        return downloadId;
    }

    public long getStepId() {
        return stepId;
    }

    public long getVideoId() {
        return videoId;
    }
}
