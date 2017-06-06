package org.stepic.droid.storage.structure;

public final class DbStructureBlock {
    public static final String BLOCKS = "blocks";

    public static final class Column {
        public static final String STEP_ID = "step_id";
        public static final String NAME = "name";
        public static final String TEXT = "block_text";

        public static final String EXTERNAL_THUMBNAIL = "optional_thumbnail";
        public static final String EXTERNAL_VIDEO_ID = "optional_video_id";
    }
}
