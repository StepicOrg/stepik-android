package org.stepic.droid.storage.structure;

@Deprecated
public final class DbStructureCachedVideo {
    public static final String CACHED_VIDEO = "cached_video";

    public static final class Column {
        public static final String STEP_ID = "step_id";
        public static final String VIDEO_ID = "_id";
        public static final String THUMBNAIL = "thumbnail_store_url";
        public static final String URL = "store_url";
        public static final String QUALITY = "quality";
    }
}
