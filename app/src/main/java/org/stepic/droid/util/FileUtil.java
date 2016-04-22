package org.stepic.droid.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class FileUtil {
    public static void cleanDirectory(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                cleanDirectory(child);

        fileOrDirectory.delete();
    }

    public static String saveImageToDisk(String filename, final String urlPath, File root) {
        String filepath = (new File(root, filename)).getPath();
        int count;
        try {
            URL url = new URL(urlPath);
            URLConnection connection = url.openConnection();
            connection.connect();

            // this will be useful so that you can show a typical 0-100%
            // progress bar
            int lengthOfFile = connection.getContentLength();

            // download the file
            InputStream input = new BufferedInputStream(url.openStream(),
                    8192);

            File file = new File(filepath);
            file.createNewFile();
            // Output stream
            OutputStream output = new FileOutputStream(file);

            byte data[] = new byte[1024];

            long total = 0;

            while ((count = input.read(data)) != -1) {
                total += count;
                // writing data to file
                output.write(data, 0, count);
            }

            // flushing output
            output.flush();

            // closing streams
            output.close();
            input.close();

        } catch (Exception e) {
            filepath = null;
        }

        return filepath;
    }

    public static long getFileOrFolderSizeInKb(File f) {
        return getFileSize(f)/1024;
    }

    private static long getFileSize(File f) {

        long size = 0;
        if (f.isDirectory()) {
            for (File file : f.listFiles()) {
                size += getFileSize(file);
            }
        } else {
            size = f.length();
        }
        return size;

    }
}