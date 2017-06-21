package org.stepic.droid.model;

public final class CachedVideo {
    private long stepId;
    private long videoId;
    private String url;
    private String thumbnail;
    private String quality;

    public CachedVideo() {}

    public CachedVideo (long stepId, long videoId, String url, String thumbnail_url) {
        this.stepId = stepId;
        this.videoId = videoId;
        this.url = url;
        this.thumbnail = thumbnail_url;
    }


    public String getQuality() {
        return quality;
    }

    public void setQuality(String quality) {
        this.quality = quality;
    }

    public long getStepId() {
        return stepId;
    }

    public void setStepId(long stepId) {
        this.stepId = stepId;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public long getVideoId() {
        return videoId;
    }

    public void setVideoId(long videoId) {
        this.videoId = videoId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
