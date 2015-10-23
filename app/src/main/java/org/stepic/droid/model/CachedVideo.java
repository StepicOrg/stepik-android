package org.stepic.droid.model;

public class CachedVideo {
    private long videoId;
    private String url;

    public CachedVideo() {}


    public CachedVideo (long videoId, String url) {
        this.videoId = videoId;
        this.url = url;
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
