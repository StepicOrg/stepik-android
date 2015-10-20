package org.stepic.droid.store;

public interface IDownloadManager {

    void addDownload(String url, String fileId);

    boolean isDownloadManagerEnabled();
}
