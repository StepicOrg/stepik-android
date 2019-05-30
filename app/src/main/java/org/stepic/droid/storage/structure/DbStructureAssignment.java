package org.stepic.droid.storage.structure;

import kotlin.Deprecated;

@Deprecated(message = "Use one from org.stepik.android.cache.assignment.structure")
public final class DbStructureAssignment {
    public static final String ASSIGNMENTS = "assignments";

    public static final class Column {
        public static final String ASSIGNMENT_ID = "assignment_id";
        public static final String UNIT_ID = "unit_id";
        public static final String STEP_ID = "step_id";
        public static final String PROGRESS = "progress";
        public static final String CREATE_DATE = "create_date";
        public static final String UPDATE_DATE = "update_date";
    }
}
