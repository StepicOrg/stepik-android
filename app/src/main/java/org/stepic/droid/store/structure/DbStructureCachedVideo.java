package org.stepic.droid.store.structure;

public class DbStructureCachedVideo extends DBStructureBase {

    private static String[] mUsedColumns = null;

    public static final String CACHED_VIDEO = "cached_video";

    public static final class Column {
        public static final String VIDEO_ID = "_id";
        public static final String URL = "store_url";

    }

    public static String[] getUsedColumns() {
        if (mUsedColumns == null) {
            mUsedColumns = new String[]{
                    Column.VIDEO_ID,
                    Column.URL
            };
        }
        return mUsedColumns;
    }

}
