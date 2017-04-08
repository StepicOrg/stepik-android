package org.stepic.droid.storage.structure;

public final class DbStructureProgress extends DBStructureBase{
    private static String[] usedColumns = null;

    public static final String PROGRESS = "progress";

    public static final class Column {
        public static final String ID = "progress_id";
        public static final String LAST_VIEWED = "last_viewed";
        public static final String SCORE = "score";
        public static final String COST = "cost";
        public static final String N_STEPS = "n_steps";
        public static final String N_STEPS_PASSED = "n_steps_passed";
        public static final String IS_PASSED = "is_passed";

    }

    public static String[] getUsedColumns() {
        if (usedColumns == null) {
            usedColumns = new String[]{
                    Column.ID,
                    Column.LAST_VIEWED,
                    Column.SCORE,
                    Column.COST,
                    Column.N_STEPS,
                    Column.N_STEPS_PASSED,
                    Column.IS_PASSED,
            };
        }
        return usedColumns;
    }
}
