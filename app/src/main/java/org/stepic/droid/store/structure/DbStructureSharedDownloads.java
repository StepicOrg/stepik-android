package org.stepic.droid.store.structure;

public final class DbStructureSharedDownloads {
    private static String[] usedColumns = null;

    public static final String SHARED_DOWNLOADS = "shared_downloads";

    public static final class Column {
        public static final String DOWNLOAD_ID = "download_id";
        public static final String STEP_ID = "step_id";
        public static final String VIDEO_ID = "video_id";
        public static final String THUMBNAIL = "thumbnail";
        public static final String QUALITY = "quality";


    }

    public static String[] getUsedColumns() {
        if (usedColumns == null) {
            usedColumns = new String[]{
                    Column.DOWNLOAD_ID,
                    Column.STEP_ID,
                    Column.VIDEO_ID,
                    Column.THUMBNAIL,
                    Column.QUALITY
            };
        }
        return usedColumns;
    }

}