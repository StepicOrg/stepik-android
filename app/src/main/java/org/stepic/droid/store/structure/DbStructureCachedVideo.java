package org.stepic.droid.store.structure;

public final class DbStructureCachedVideo extends DBStructureBase {

    private static String[] usedColumns = null;

    public static final String CACHED_VIDEO = "cached_video";

    public static final class Column {

        public static final String STEP_ID = "step_id";
        public static final String VIDEO_ID = "_id";
        public static final String THUMBNAIL = "thumbnail_store_url";
        public static final String URL = "store_url";
        public static final String QUALITY = "quality";
    }

    public static String[] getUsedColumns() {
        if (usedColumns == null) {
            usedColumns = new String[]{
                    Column.VIDEO_ID,
                    Column.URL,
                    Column.STEP_ID,
                    Column.THUMBNAIL,
                    Column.QUALITY
            };
        }
        return usedColumns;
    }

}
