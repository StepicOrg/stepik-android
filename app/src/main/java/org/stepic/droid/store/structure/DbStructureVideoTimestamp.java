package org.stepic.droid.store.structure;

public class DbStructureVideoTimestamp extends DBStructureBase {
    private static String[] usedColumns = null;

    public static final String VIDEO_TIMESTAMP = "video_timestamps";

    public static final class Column {
        public static final String VIDEO_ID = "video_id";
        public static final String TIMESTAMP = "timestamp";
    }

    public static String[] getUsedColumns() {
        if (usedColumns == null) {
            usedColumns = new String[]{
                    Column.VIDEO_ID,
                    Column.TIMESTAMP
            };
        }
        return usedColumns;
    }
}
