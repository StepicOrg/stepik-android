package org.stepic.droid.util;

import android.content.Context;
import android.support.v4.content.ContextCompat;

import org.jetbrains.annotations.NotNull;

import java.io.File;

public class StorageUtil {
    enum SDState {
        sdcardMounted,
        sdCardNotMounted,
        sdCardNotAvailableOnDevice,
        accessToStorageRestricted
    }

    @NotNull
    public static SDState getSDState(Context context) {
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
}
