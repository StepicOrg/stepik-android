package org.stepic.droid.util;

import android.content.Context;
import android.os.StatFs;
import android.support.v4.content.ContextCompat;

import org.jetbrains.annotations.Nullable;
import org.stepic.droid.base.App;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
            if (files[1] == null || getTotalMemorySize(files[1]) <= 0L) {
                return SDState.sdCardNotMounted;
            } else {
                return SDState.sdcardMounted;
            }
        }
    }

    public static File[] getRawAppDirs() {
        return ContextCompat.getExternalFilesDirs(App.getAppContext(), null);
    }

    public static long getAvailableMemorySize(File path) {
        try {
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSize();
            long availableBlocks = stat.getAvailableBlocks();
            return availableBlocks * blockSize;
        }
        catch (Exception ex){
            return 0L;
        }
    }

    public static long getTotalMemorySize(File path) {
        try {
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSize();
            long totalBlocks = stat.getBlockCount();
            return totalBlocks * blockSize;
        }
        catch (Exception ex){
            return 0L;
        }
    }

    public static void moveFile(String inputPath, String inputFile, String outputPath) throws IOException {
        inputPath+=File.separator;
        outputPath+=File.separator;

        InputStream in = null;
        OutputStream out = null;

            //create output directory if it doesn't exist
            File dir = new File (outputPath);
            if (!dir.exists())
            {
                dir.mkdirs();
            }


            in = new FileInputStream(inputPath + inputFile);
            out = new FileOutputStream(outputPath + inputFile);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;

            // write the output file
            out.flush();
            out.close();
            out = null;

            // delete the original file
            new File(inputPath + inputFile).delete();


    }
}
