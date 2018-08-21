package org.stepic.droid.util;

import org.jetbrains.annotations.Nullable;

import java.io.File;

public class FileUtil {
    public static void cleanDirectory(@Nullable File fileOrDirectory) {
        if (fileOrDirectory == null) return;
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                cleanDirectory(child);

        fileOrDirectory.delete();
    }
}