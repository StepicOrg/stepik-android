package org.stepic.droid.model;

public class DownloadEntity {
    private long downloadId;
    private long stepId;
    private long videoId;
    private String thumbnail;
    private String quality;

    public DownloadEntity() {
    }

    public DownloadEntity(long downloadId, long stepId, long videoId, String thumbnail, String quality) {
        this.downloadId = downloadId;
        this.stepId = stepId;
        this.videoId = videoId;
        this.thumbnail = thumbnail;
        this.quality = quality;
    }

    public String getQuality() {
        return quality;
    }

    public void setQuality(String quality) {
        this.quality = quality;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
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
