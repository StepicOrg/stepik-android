package org.stepic.droid.store.structure;

public final class DbStructureUnit extends DBStructureBase {

    private static String[] mUsedColumns = null;

    public static final String UNITS = "units";

    public static final class Column {
        @Deprecated
        public static final String ID = "_id";
        public static final String UNIT_ID = "unit_id";
        public static final String SECTION = "section_id";
        public static final String LESSON = "lesson";
        public static final String ASSIGNMENTS = "assignments";
        public static final String POSITION = "position";
        public static final String PROGRESS = "progress";
        public static final String BEGIN_DATE = "begin_date";
        public static final String END_DATE = "end_date";
        public static final String SOFT_DEADLINE = "soft_deadline";
        public static final String HARD_DEADLINE = "hard_deadline";
        public static final String GRADING_POLICY = "grading_policy";
        public static final String BEGIN_DATE_SOURCE = "begin_date_source";
        public static final String END_DATE_SOURCE = "end_date_source";
        public static final String SOFT_DEADLINE_SOURCE = "soft_deadline_source";
        public static final String HARD_DEADLINE_SOURCE = "hard_deadline_source";
        public static final String GRADING_POLICY_SOURCE = "grading_policy_source";
        public static final String IS_ACTIVE = "is_active";
        public static final String CREATE_DATE = "create_date";
        public static final String UPDATE_DATE = "update_date";
        public static final String IS_CACHED = "is_cached";
        public static final String IS_LOADING = "is_loading";
    }

    public static String[] getUsedColumns() {
        if (mUsedColumns == null) {
            mUsedColumns = new String[]{
                    Column.ID,
                    Column.UNIT_ID,
                    Column.LESSON,
                    Column.ASSIGNMENTS,
                    Column.POSITION,
                    Column.PROGRESS,
                    Column.BEGIN_DATE,
                    Column.END_DATE,
                    Column.SOFT_DEADLINE,
                    Column.HARD_DEADLINE,
                    Column.GRADING_POLICY,
                    Column.BEGIN_DATE_SOURCE,
                    Column.END_DATE_SOURCE,
                    Column.SOFT_DEADLINE_SOURCE,
                    Column.HARD_DEADLINE_SOURCE,
                    Column.GRADING_POLICY_SOURCE,
                    Column.IS_ACTIVE,
                    Column.CREATE_DATE,
                    Column.UPDATE_DATE,
                    Column.SECTION,
                    Column.IS_CACHED,
                    Column.IS_LOADING,
            };
        }
        return mUsedColumns;
    }

}
