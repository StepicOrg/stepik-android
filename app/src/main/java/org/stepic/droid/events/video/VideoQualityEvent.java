package org.stepic.droid.events.video;

public class VideoQualityEvent {
    String quality;
    private long stepId;

    public String getQuality() {
        return quality;
    }

    public long getStepId() {
        return stepId;
    }

    public VideoQualityEvent(String quality, long stepId) {
        this.quality = quality;
        this.stepId = stepId;
    }
}
