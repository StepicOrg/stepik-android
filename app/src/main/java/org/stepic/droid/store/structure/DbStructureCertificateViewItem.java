package org.stepic.droid.store.structure;

public class DbStructureCertificateViewItem {
    private static String[] usedColumns = null;

    public static final String CERTIFICATE_VIEW_ITEM = "certificate_view_item";

    public static final class Column {

        public static final String CERTIFICATE_ID = "certificate_id";
        public static final String TITLE = "title";
        public static final String COVER_FULL_PATH = "cover_full_path";
        public static final String TYPE = "type";
        public static final String FULL_PATH = "full_path";
        public static final String GRADE = "grade";
        public static final String ISSUE_DATE = "issue_date";

    }

    public static String[] getUsedColumns() {
        if (usedColumns == null) {
            usedColumns = new String[]{
                    Column.CERTIFICATE_ID,
                    Column.TITLE,
                    Column.COVER_FULL_PATH,
                    Column.TYPE,
                    Column.FULL_PATH,
                    Column.GRADE,
                    Column.ISSUE_DATE
            };
        }
        return usedColumns;
    }
}
