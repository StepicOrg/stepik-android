package org.stepic.droid.store.structure;

public class DbStructureCalendarSection extends DBStructureBase {
    private static String[] mUsedColumns = null;

    public static final String CALENDAR_SECTION = "calendar_section";

    public static final class Column {

        public static final String SECTION_ID = "section_id";
        public static final String EVENT_ID = "event_id";
        public static final String LAST_STORED_DEADLINE = "last_stored_deadline";
    }

    public static String[] getUsedColumns() {
        if (mUsedColumns == null) {
            mUsedColumns = new String[]{
                    Column.SECTION_ID,
                    Column.EVENT_ID,
                    Column.LAST_STORED_DEADLINE
            };
        }
        return mUsedColumns;
    }
}
