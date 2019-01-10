package org.stepic.droid.storage.structure;

@Deprecated
public final class DbStructureUnit {
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
    }

}
