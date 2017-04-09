package org.stepic.droid.storage.structure;

public final class DbStructureStep extends DBStructureBase {

    private static String[] usedColumns = null;

    public static final String STEPS = "steps";

    public static final class Column {
        public static final String STEP_ID = "step_id";
        public static final String LESSON_ID = "lesson_id";
        public static final String STATUS = "status";
        public static final String PROGRESS = "progress";
        public static final String SUBSCRIPTIONS = "subscription";
        public static final String VIEWED_BY = "viewed_by";
        public static final String PASSED_BY = "passed_by";
        public static final String CREATE_DATE = "create_date";
        public static final String UPDATE_DATE = "update_date";
        public static final String POSITION = "position";
        public static final String IS_CACHED = "is_cached";
        public static final String IS_LOADING = "is_loading";
        public static final String DISCUSSION_COUNT = "discussion_count";
        public static final String DISCUSSION_ID = "discussion_id";
        public static final String PEER_REVIEW = "has_peer_review";
        public static final String HAS_SUBMISSION_RESTRICTION = "has_submission_restriction";
        public static final String MAX_SUBMISSION_COUNT = "max_submission_count";

    }

    public static String[] getUsedColumns() {
        if (usedColumns == null) {
            usedColumns = new String[]{
                    Column.STEP_ID,
                    Column.LESSON_ID,
                    Column.STATUS,
                    Column.PROGRESS,
                    Column.SUBSCRIPTIONS,
                    Column.VIEWED_BY,
                    Column.PASSED_BY,
                    Column.CREATE_DATE,
                    Column.UPDATE_DATE,
                    Column.POSITION,
                    Column.IS_CACHED,
                    Column.IS_LOADING,
                    Column.DISCUSSION_COUNT,
                    Column.DISCUSSION_ID,
                    Column.PEER_REVIEW,
                    Column.HAS_SUBMISSION_RESTRICTION,
                    Column.MAX_SUBMISSION_COUNT
            };
        }
        return usedColumns;
    }
}
