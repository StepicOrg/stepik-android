package org.stepic.droid.util;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.stepic.droid.base.MainApplication;
import org.stepic.droid.preferences.UserPreferences;

import java.io.File;
import java.io.FileOutputStream;

import javax.inject.Inject;

public class ImageOnDisk implements Target {

    @Inject
    UserPreferences mUserPreferences;

    private final String nameOnDisk;

    public ImageOnDisk(String nameOnDisk) {
        MainApplication.component().inject(this);
        this.nameOnDisk = nameOnDisk;
    }

    @Override
    public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                File downloadFolderAndFile = new File(mUserPreferences.getUserDownloadFolder(), nameOnDisk);

                try {
                    downloadFolderAndFile.createNewFile();
                    FileOutputStream ostream = new FileOutputStream(downloadFolderAndFile);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, ostream);
                    ostream.close();
                } catch (Exception ignored) {
                }
            }
        }).start();

    }

    @Override
    public void onBitmapFailed(Drawable errorDrawable) {

    }

    @Override
    public void onPrepareLoad(Drawable placeHolderDrawable) {

    }
}
