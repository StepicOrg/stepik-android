package org.stepic.droid.store.structure;

public class DbStructureStep extends DBStructureBase {

    private static String[] mUsedColumns = null;

    public static final String STEPS = "steps";

    public static final class Column {
        public static final String STEP_ID = "step_id";
        public static final String LESSON_ID = "lesson_id";
        public static final String STATUS = "status";
        public static final String PROGRESS = "progress";
        public static final String SUBSCRIPTIONS = "subscription";
        public static final String VIEWED_BY = "step_id";
        public static final String PASSED_BY = "step_id";
        public static final String CREATE_DATE = "step_id";
        public static final String UPDATE_DATE = "step_id";
    }

    public static String[] getUsedColumns() {
        if (mUsedColumns == null) {
            mUsedColumns = new String[]{
                    Column.STEP_ID,
                    Column.LESSON_ID,
                    Column.STATUS,
                    Column.PROGRESS,
                    Column.SUBSCRIPTIONS,
                    Column.VIEWED_BY,
                    Column.PASSED_BY,
                    Column.CREATE_DATE,
                    Column.UPDATE_DATE,
            };
        }
        return mUsedColumns;
    }
}
