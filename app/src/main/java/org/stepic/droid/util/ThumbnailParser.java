package org.stepic.droid.util;

import android.net.Uri;

import java.io.File;

public class ThumbnailParser {

    public static Uri getUriForThumbnail(String thumbnail) {
        Uri uri;
        if (thumbnail.startsWith("http")) {
            uri = Uri.parse(thumbnail);
        } else {
            uri = Uri.fromFile(new File(thumbnail));
        }
        return uri;
    }

}
