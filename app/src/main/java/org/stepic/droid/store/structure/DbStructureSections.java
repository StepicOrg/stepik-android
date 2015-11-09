package org.stepic.droid.store.structure;

public final class DbStructureSections extends DBStructureBase {

    private static String[] mUsedColumns = null;

    public static final String SECTIONS = "sections";

    public static final class Column {
        public static final String ID = "_id";
        public static final String SECTION_ID = "section_id";
        public static final String COURSE = "course";
        public static final String UNITS = "units";
        public static final String POSITION = "position";
        public static final String PROGRESS = "section_progress";
        public static final String TITLE = "title";
        public static final String SLUG = "slug";
        public static final String BEGIN_DATE = "begin_date";
        public static final String END_DATE = "end_date";
        public static final String SOFT_DEADLINE = "soft_deadline";
        public static final String HARD_DEADLINE = "hard_deadline";
        public static final String GRADING_POLICY = "grading_policy";
        public static final String BEGIN_DATE_SOURCE = "begin_data_source";
        public static final String END_DATE_SOURCE = "end_data_source";
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
                    Column.SECTION_ID,
                    Column.TITLE,
                    Column.SLUG,
                    Column.IS_ACTIVE,
                    Column.BEGIN_DATE,
                    Column.SOFT_DEADLINE,
                    Column.HARD_DEADLINE,
                    Column.COURSE,
                    Column.POSITION,
                    Column.UNITS,
                    Column.IS_CACHED,
                    Column.IS_LOADING,
            };
        }
        return mUsedColumns;
    }
}
