package org.stepic.droid.util;

import java.io.File;

public class CleanerUtil {
    public static void CleanDirectory(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                CleanDirectory(child);

        fileOrDirectory.delete();
    }
}
