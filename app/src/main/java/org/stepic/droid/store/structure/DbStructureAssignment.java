package org.stepic.droid.store.structure;

public final class DbStructureAssignment extends DBStructureBase {
    private static String[] usedColumns = null;

    public static final String ASSIGNMENTS = "assignments";

    public static final class Column {
        public static final String ASSIGNMENT_ID = "assignment_id";
        public static final String UNIT_ID = "unit_id";
        public static final String STEP_ID = "step_id";
        public static final String PROGRESS = "progress";
        public static final String CREATE_DATE = "create_date";
        public static final String UPDATE_DATE = "update_date";
    }

    public static String[] getUsedColumns() {
        if (usedColumns == null) {
            usedColumns = new String[]{
                    Column.ASSIGNMENT_ID,
                    Column.UNIT_ID,
                    Column.STEP_ID,
                    Column.ASSIGNMENT_ID,
                    Column.PROGRESS,
                    Column.CREATE_DATE,
                    Column.UPDATE_DATE,

            };
        }
        return usedColumns;
    }
}
