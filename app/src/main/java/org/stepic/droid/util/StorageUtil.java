package org.stepic.droid.util;

import android.content.Context;
import android.support.v4.content.ContextCompat;

import org.jetbrains.annotations.Nullable;
import org.stepic.droid.base.MainApplication;

import java.io.File;

public class StorageUtil {
    public enum SDState {
        sdcardMounted,
        sdCardNotMounted,
        sdCardNotAvailableOnDevice,
        accessToStorageRestricted
    }

    @Nullable
    public static SDState getSDState(@Nullable Context context) {
        if (context == null) return null;

        File[] files = ContextCompat.getExternalFilesDirs(context, null);
        if (files == null || files.length <= 0) {
            return SDState.accessToStorageRestricted;
        }

        if (files.length == 1) {
            return SDState.sdCardNotAvailableOnDevice;
        } else {
            //files.length >= 2
            if (files[1] == null) {
                return SDState.sdCardNotMounted;
            } else {
                return SDState.sdcardMounted;
            }
        }
    }

    public static File[] getRawAppDirs() {
        return ContextCompat.getExternalFilesDirs(MainApplication.getAppContext(), null);
    }
}
