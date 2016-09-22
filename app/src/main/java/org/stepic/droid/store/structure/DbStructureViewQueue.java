package org.stepic.droid.store.structure;

public final class DbStructureViewQueue extends DBStructureBase {

    private static String[] usedColumns = null;

    public static final String VIEW_QUEUE = "view_queue";

    public static final class Column {
        public static final String ASSIGNMENT_ID = "assignment";
        public static final String STEP_ID = "step";
    }

    public static String[] getUsedColumns() {
        if (usedColumns == null) {
            usedColumns = new String[]{
                    Column.ASSIGNMENT_ID,
                    Column.STEP_ID
            };
        }
        return usedColumns;
    }

}
