package org.stepic.droid.events;

public class DownloadingIsLoadedSuccessfullyEvent {
    long downloadId;

    public DownloadingIsLoadedSuccessfullyEvent(long downloadId) {
        this.downloadId = downloadId;
    }

    public long getDownloadId() {
        return downloadId;
    }
}
