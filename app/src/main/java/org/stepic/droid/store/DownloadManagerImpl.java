package org.stepic.droid.store;

import android.app.DownloadManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;

import org.stepic.droid.preferences.UserPreferences;

import java.io.File;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class DownloadManagerImpl implements IDownloadManager {

    DownloadManager mSystemDownloadManager;
    UserPreferences mUserPrefs;
    Context mContext;

    @Inject
    public DownloadManagerImpl(Context context, UserPreferences preferences, DownloadManager dm) {
        mUserPrefs = preferences;
        mContext = context;
        mSystemDownloadManager = dm;
    }


    @Override
    public synchronized void addDownload(String url, String fileId) {
        if (!isDownloadManagerEnabled() || url == null)
            return;

        url = url.trim();
        if (url.length() == 0)
            return;

        try {
            Log.i("downloading", "starting download");

            File downloadFolderAndFile = new File(mUserPrefs.getDownloadFolder(), fileId);
            if (downloadFolderAndFile.exists()) {
                //we do not need download the file, because we already have it.
                return;
            }
            Uri target = Uri.fromFile(downloadFolderAndFile);

            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            request.setDestinationUri(target);
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
            request.setVisibleInDownloadsUi(false);

            if (mUserPrefs.isNetworkMobileAllowed()) {
                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
            } else {
                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
            }

            mSystemDownloadManager.enqueue(request);

        } catch (Exception ex) {
            Log.i("downloading", "downloading is failed");
        }

    }

    @Override
    public synchronized boolean isDownloadManagerEnabled() {
        if (mContext == null) {
            return false;
        }

        int state = mContext.getPackageManager()
                .getApplicationEnabledSetting("com.android.providers.downloads");

        if (state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED ||
                state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER
                || state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED) {
            //download manager is disabled
            return false;
        }
        return true;
    }
}
