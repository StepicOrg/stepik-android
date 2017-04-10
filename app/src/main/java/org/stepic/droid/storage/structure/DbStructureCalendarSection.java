package org.stepic.droid.storage.structure;

public class DbStructureCalendarSection extends DBStructureBase {
    private static String[] usedColumns = null;

    public static final String CALENDAR_SECTION = "calendar_section";

    public static final class Column {

        public static final String SECTION_ID = "section_id";
        public static final String EVENT_ID_HARD = "event_id_hard";
        public static final String EVENT_ID_SOFT = "event_id_soft";
        public static final String HARD_DEADLINE = "hard_deadline";
        public static final String SOFT_DEADLINE = "soft_deadline";

    }

    public static String[] getUsedColumns() {
        if (usedColumns == null) {
            usedColumns = new String[]{
                    Column.SECTION_ID,
                    Column.EVENT_ID_HARD,
                    Column.HARD_DEADLINE,
            };
        }
        return usedColumns;
    }
}
