package org.stepic.droid.storage.migration;

import androidx.sqlite.db.SupportSQLiteDatabase;

import org.stepic.droid.storage.structure.DbStructureAdaptiveExp;
import org.stepic.droid.storage.structure.DbStructureAssignment;
import org.stepic.droid.storage.structure.DbStructureBlock;
import org.stepic.droid.storage.structure.DbStructureCachedVideo;
import org.stepic.droid.storage.structure.DbStructureCalendarSection;
import org.stepic.droid.storage.structure.DbStructureEnrolledAndFeaturedCourses;
import org.stepic.droid.storage.structure.DbStructureLesson;
import org.stepic.droid.storage.structure.DbStructureNotification;
import org.stepic.droid.storage.structure.DbStructureProgress;
import org.stepic.droid.storage.structure.DbStructureSearchQuery;
import org.stepic.droid.storage.structure.DbStructureSections;
import org.stepic.droid.storage.structure.DbStructureSharedDownloads;
import org.stepic.droid.storage.structure.DbStructureStep;
import org.stepic.droid.storage.structure.DbStructureUnit;
import org.stepic.droid.storage.structure.DbStructureVideoTimestamp;
import org.stepic.droid.storage.structure.DbStructureVideoUrl;
import org.stepic.droid.storage.structure.DbStructureViewQueue;
import org.stepic.droid.storage.structure.DbStructureViewedNotificationsQueue;
import org.stepik.android.cache.personal_deadlines.structure.DbStructureDeadlines;
import org.stepik.android.cache.personal_deadlines.structure.DbStructureDeadlinesBanner;

final class LegacyDatabaseMigrations {
    private static final String TEXT_TYPE = "TEXT";
    private static final String LONG_TYPE = "LONG";
    private static final String INT_TYPE = "INTEGER";
    private static final String BOOLEAN_TYPE = "BOOLEAN";
    private static final String DATETIME_TYPE = "DATETIME";
    private static final String REAL_TYPE = "REAL";
    private static final String WHITESPACE = " ";
    private static final String FALSE_VALUE = "0";
    private static final String TRUE_VALUE = "1";
    private static final String DEFAULT = "DEFAULT";

    static void upgradeFrom32To33(SupportSQLiteDatabase db) {
        DbStructureDeadlines.INSTANCE.createTable(db);
        DbStructureDeadlinesBanner.INSTANCE.createTable(db);
    }

    static void upgradeFrom31To32(SupportSQLiteDatabase db) {
        createAdaptiveExpTable(db);
    }

    static void upgradeFrom30To31(SupportSQLiteDatabase db) {
        createViewedNotificationsQueueTable(db);
    }

    static void upgradeFrom29To30(SupportSQLiteDatabase db) {
        createSearchQueryTable(db);
        createSearchQueryTableSizeLimiterTrigger(db);
    }

    static void upgradeFrom28To29(SupportSQLiteDatabase db) {
        alterColumn(db, DbStructureEnrolledAndFeaturedCourses.ENROLLED_COURSES, DbStructureEnrolledAndFeaturedCourses.Column.AVERAGE_RATING, REAL_TYPE);
        alterColumn(db, DbStructureEnrolledAndFeaturedCourses.FEATURED_COURSES, DbStructureEnrolledAndFeaturedCourses.Column.AVERAGE_RATING, REAL_TYPE);

        alterColumn(db, DbStructureEnrolledAndFeaturedCourses.ENROLLED_COURSES, DbStructureEnrolledAndFeaturedCourses.Column.REVIEW_SUMMARY, INT_TYPE);
        alterColumn(db, DbStructureEnrolledAndFeaturedCourses.FEATURED_COURSES, DbStructureEnrolledAndFeaturedCourses.Column.REVIEW_SUMMARY, INT_TYPE);
    }

    static void upgradeFrom27To28(SupportSQLiteDatabase db) {
        alterColumn(db, DbStructureEnrolledAndFeaturedCourses.ENROLLED_COURSES, DbStructureEnrolledAndFeaturedCourses.Column.PROGRESS, TEXT_TYPE);
        alterColumn(db, DbStructureEnrolledAndFeaturedCourses.FEATURED_COURSES, DbStructureEnrolledAndFeaturedCourses.Column.PROGRESS, TEXT_TYPE);
    }

    static void upgradeFrom26To27(SupportSQLiteDatabase db) {
        alterColumn(db, DbStructureSections.SECTIONS, DbStructureSections.Column.IS_REQUIREMENT_SATISFIED, BOOLEAN_TYPE, TRUE_VALUE);
        alterColumn(db, DbStructureSections.SECTIONS, DbStructureSections.Column.REQUIRED_PERCENT, INT_TYPE);
        alterColumn(db, DbStructureSections.SECTIONS, DbStructureSections.Column.REQUIRED_SECTION, LONG_TYPE);
    }

    static void upgradeFrom24To25(SupportSQLiteDatabase db) {
        alterColumn(db, DbStructureBlock.BLOCKS, DbStructureBlock.Column.CODE_OPTIONS, TEXT_TYPE);
    }

    static void upgradeFrom23To24(SupportSQLiteDatabase db) {
        alterColumn(db, DbStructureEnrolledAndFeaturedCourses.ENROLLED_COURSES, DbStructureEnrolledAndFeaturedCourses.Column.LEARNERS_COUNT, LONG_TYPE);
        alterColumn(db, DbStructureEnrolledAndFeaturedCourses.FEATURED_COURSES, DbStructureEnrolledAndFeaturedCourses.Column.LEARNERS_COUNT, LONG_TYPE);
    }

    static void upgradeFrom22To23(SupportSQLiteDatabase db) {
        alterColumn(db, DbStructureBlock.BLOCKS, DbStructureBlock.Column.EXTERNAL_VIDEO_DURATION, LONG_TYPE);
    }

    static void upgradeFrom21To22(SupportSQLiteDatabase db) {
        createVideoUrlTable(db, DbStructureVideoUrl.externalVideosName);
        alterColumn(db, DbStructureBlock.BLOCKS, DbStructureBlock.Column.EXTERNAL_THUMBNAIL, TEXT_TYPE);
        alterColumn(db, DbStructureBlock.BLOCKS, DbStructureBlock.Column.EXTERNAL_VIDEO_ID, LONG_TYPE);
    }

    static void upgradeFrom18To19(SupportSQLiteDatabase db) {
        alterColumn(db, DbStructureEnrolledAndFeaturedCourses.ENROLLED_COURSES, DbStructureEnrolledAndFeaturedCourses.Column.LAST_STEP_ID, TEXT_TYPE);
        alterColumn(db, DbStructureEnrolledAndFeaturedCourses.FEATURED_COURSES, DbStructureEnrolledAndFeaturedCourses.Column.LAST_STEP_ID, TEXT_TYPE);

        alterColumn(db, DbStructureEnrolledAndFeaturedCourses.ENROLLED_COURSES, DbStructureEnrolledAndFeaturedCourses.Column.IS_ACTIVE, BOOLEAN_TYPE);
        alterColumn(db, DbStructureEnrolledAndFeaturedCourses.FEATURED_COURSES, DbStructureEnrolledAndFeaturedCourses.Column.IS_ACTIVE, BOOLEAN_TYPE);
    }

    static void upgradeFrom17To18(SupportSQLiteDatabase db) {
        alterColumn(db, DbStructureStep.STEPS, DbStructureStep.Column.HAS_SUBMISSION_RESTRICTION, BOOLEAN_TYPE);
        alterColumn(db, DbStructureStep.STEPS, DbStructureStep.Column.MAX_SUBMISSION_COUNT, INT_TYPE);
    }

    static void upgradeFrom16To17(SupportSQLiteDatabase db) {
        alterColumn(db, DbStructureSections.SECTIONS, DbStructureSections.Column.IS_EXAM, BOOLEAN_TYPE);
    }

    static void upgradeFrom15To16(SupportSQLiteDatabase db) {
        alterColumn(db, DbStructureSections.SECTIONS, DbStructureSections.Column.DISCOUNTING_POLICY, INT_TYPE);
    }

    static void upgradeFrom14To15(SupportSQLiteDatabase db) {
        createVideoTimestamp(db);
    }

    static void upgradeFrom13To14(SupportSQLiteDatabase db) {
        alterColumn(db, DbStructureStep.STEPS, DbStructureStep.Column.PEER_REVIEW, TEXT_TYPE);
    }

    static void upgradeFrom12To13(SupportSQLiteDatabase db) {
        alterColumn(db, DbStructureEnrolledAndFeaturedCourses.ENROLLED_COURSES, DbStructureEnrolledAndFeaturedCourses.Column.BEGIN_DATE, TEXT_TYPE);
        alterColumn(db, DbStructureEnrolledAndFeaturedCourses.FEATURED_COURSES, DbStructureEnrolledAndFeaturedCourses.Column.BEGIN_DATE, TEXT_TYPE);

        alterColumn(db, DbStructureEnrolledAndFeaturedCourses.ENROLLED_COURSES, DbStructureEnrolledAndFeaturedCourses.Column.END_DATE, TEXT_TYPE);
        alterColumn(db, DbStructureEnrolledAndFeaturedCourses.FEATURED_COURSES, DbStructureEnrolledAndFeaturedCourses.Column.END_DATE, TEXT_TYPE);
    }

    static void upgradeFrom11To12(SupportSQLiteDatabase db) {
        alterColumn(db, DbStructureSections.SECTIONS, DbStructureSections.Column.TEST_SECTION, BOOLEAN_TYPE);
    }

    static void upgradeFrom9To10(SupportSQLiteDatabase db) {
        createCalendarSection(db, DbStructureCalendarSection.CALENDAR_SECTION);
    }

    static void upgradeFrom8To9(SupportSQLiteDatabase db) {
        alterColumn(db, DbStructureEnrolledAndFeaturedCourses.ENROLLED_COURSES, DbStructureEnrolledAndFeaturedCourses.Column.SCHEDULE_LINK, TEXT_TYPE);
        alterColumn(db, DbStructureEnrolledAndFeaturedCourses.FEATURED_COURSES, DbStructureEnrolledAndFeaturedCourses.Column.SCHEDULE_LINK, TEXT_TYPE);

        alterColumn(db, DbStructureEnrolledAndFeaturedCourses.ENROLLED_COURSES, DbStructureEnrolledAndFeaturedCourses.Column.SCHEDULE_LONG_LINK, TEXT_TYPE);
        alterColumn(db, DbStructureEnrolledAndFeaturedCourses.FEATURED_COURSES, DbStructureEnrolledAndFeaturedCourses.Column.SCHEDULE_LONG_LINK, TEXT_TYPE);
    }

    static void upgradeFrom7To8(SupportSQLiteDatabase db) {
        alterColumn(db, DbStructureStep.STEPS, DbStructureStep.Column.DISCUSSION_COUNT, INT_TYPE);
        alterColumn(db, DbStructureStep.STEPS, DbStructureStep.Column.DISCUSSION_ID, TEXT_TYPE);
    }

    static void upgradeFrom6To7(SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + DbStructureNotification.NOTIFICATIONS_TEMP);
        createNotification(db, DbStructureNotification.NOTIFICATIONS_TEMP);
    }

    static void upgradeFrom5To6(SupportSQLiteDatabase db) {
        alterColumn(db, DbStructureLesson.LESSONS, DbStructureLesson.Column.COVER_URL, TEXT_TYPE);
    }

    static void upgradeFrom4To5(SupportSQLiteDatabase db) {
        alterColumn(db, DbStructureEnrolledAndFeaturedCourses.ENROLLED_COURSES, DbStructureEnrolledAndFeaturedCourses.Column.INTRO_VIDEO_ID, LONG_TYPE);
        alterColumn(db, DbStructureEnrolledAndFeaturedCourses.FEATURED_COURSES, DbStructureEnrolledAndFeaturedCourses.Column.INTRO_VIDEO_ID, LONG_TYPE);
    }

    static void upgradeFrom3To4(SupportSQLiteDatabase db) {
        alterColumn(db, DbStructureEnrolledAndFeaturedCourses.ENROLLED_COURSES, DbStructureEnrolledAndFeaturedCourses.Column.CERTIFICATE, TEXT_TYPE);
        alterColumn(db, DbStructureEnrolledAndFeaturedCourses.FEATURED_COURSES, DbStructureEnrolledAndFeaturedCourses.Column.CERTIFICATE, TEXT_TYPE);
    }

    static void upgradeFrom2To3(SupportSQLiteDatabase db) {
        //in release 0.6 we create progress table with score type = Text, but in database it was Integer, now rename it:
        //http://stackoverflow.com/questions/21199398/sqlite-alter-a-tables-column-type

        String tempTableName = "tmp2to3";
        String renameTableQuery = "ALTER TABLE " + DbStructureProgress.TABLE_NAME + " RENAME TO "
                + tempTableName;
        db.execSQL(renameTableQuery);

        createProgress(db, DbStructureProgress.TABLE_NAME);

        String[] allFields = DbStructureProgress.INSTANCE.getUsedColumns();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < allFields.length; i++) {
            sb.append(allFields[i]);
            if (i != allFields.length - 1) {
                sb.append(", ");
            }
        }
        String fields_correct = sb.toString();
        String insertValues = "INSERT INTO " + DbStructureProgress.TABLE_NAME + "("
                + fields_correct +
                ")" +
                "   SELECT " +
                fields_correct +
                "   FROM " + tempTableName;

        db.execSQL(insertValues);

        String drop = "DROP TABLE " + tempTableName;
        db.execSQL(drop);
    }

    static void upgradeFrom1To2(SupportSQLiteDatabase db) {
        createAssignment(db, DbStructureAssignment.ASSIGNMENTS);
        createProgress(db, DbStructureProgress.TABLE_NAME);
        createViewQueue(db, DbStructureViewQueue.VIEW_QUEUE);
    }

    static void upgradeFrom0To1(SupportSQLiteDatabase db) {
        createCourseTable(db, DbStructureEnrolledAndFeaturedCourses.ENROLLED_COURSES);
        createCourseTable(db, DbStructureEnrolledAndFeaturedCourses.FEATURED_COURSES);
        createSectionTable(db, DbStructureSections.SECTIONS);
        createCachedVideoTable(db, DbStructureCachedVideo.CACHED_VIDEO);
        createUnitsDb(db, DbStructureUnit.UNITS);
        createLessonsDb(db, DbStructureLesson.LESSONS);
        createStepsDb(db, DbStructureStep.STEPS);
        createBlocksDb(db, DbStructureBlock.BLOCKS);
        createShareDownloads(db, DbStructureSharedDownloads.SHARED_DOWNLOADS);
    }

    private static void alterColumn(SupportSQLiteDatabase db, String dbName, String column, String type) {
        String upgrade = "ALTER TABLE " + dbName + " ADD COLUMN "
                + column + " " + type + " ";
        db.execSQL(upgrade);
    }

    private static void alterColumn(SupportSQLiteDatabase db, String dbName, String column, String type, String defaultValue) {
        String upgrade = "ALTER TABLE " + dbName + " ADD COLUMN "
                + column + WHITESPACE + type + WHITESPACE + DEFAULT + WHITESPACE + defaultValue;
        db.execSQL(upgrade);
    }


    private static void createCourseTable(SupportSQLiteDatabase db, String name) {
        String sql = "CREATE TABLE " + name
                + " ("
                + DbStructureEnrolledAndFeaturedCourses.Column.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + DbStructureEnrolledAndFeaturedCourses.Column.COURSE_ID + " LONG, "
                + DbStructureEnrolledAndFeaturedCourses.Column.WORKLOAD + " TEXT, "
                + DbStructureEnrolledAndFeaturedCourses.Column.COVER_LINK + " TEXT, "
                + DbStructureEnrolledAndFeaturedCourses.Column.INTRO_LINK_VIMEO + " TEXT, "
                + DbStructureEnrolledAndFeaturedCourses.Column.COURSE_FORMAT + " TEXT, "
                + DbStructureEnrolledAndFeaturedCourses.Column.TARGET_AUDIENCE + " TEXT, "
                + DbStructureEnrolledAndFeaturedCourses.Column.INSTRUCTORS + " TEXT, "
                + DbStructureEnrolledAndFeaturedCourses.Column.REQUIREMENTS + " TEXT, "
                + DbStructureEnrolledAndFeaturedCourses.Column.DESCRIPTION + " TEXT, "
                + DbStructureEnrolledAndFeaturedCourses.Column.SECTIONS + " TEXT, "
                + DbStructureEnrolledAndFeaturedCourses.Column.TOTAL_UNITS + " INTEGER, "
                + DbStructureEnrolledAndFeaturedCourses.Column.ENROLLMENT + " INTEGER, "
                + DbStructureEnrolledAndFeaturedCourses.Column.IS_FEATURED + " BOOLEAN, "
                + DbStructureEnrolledAndFeaturedCourses.Column.OWNER + " LONG, "
                + DbStructureEnrolledAndFeaturedCourses.Column.IS_CONTEST + " BOOLEAN, "
                + DbStructureEnrolledAndFeaturedCourses.Column.LANGUAGE + " TEXT, "
                + DbStructureEnrolledAndFeaturedCourses.Column.IS_PUBLIC + " BOOLEAN, "
                + DbStructureEnrolledAndFeaturedCourses.Column.IS_CACHED + " BOOLEAN, "
                + DbStructureEnrolledAndFeaturedCourses.Column.IS_LOADING + " BOOLEAN, "
                + DbStructureEnrolledAndFeaturedCourses.Column.TITLE + " TEXT, "
                + DbStructureEnrolledAndFeaturedCourses.Column.SLUG + " TEXT, "
                + DbStructureEnrolledAndFeaturedCourses.Column.SUMMARY + " TEXT, "
                + DbStructureEnrolledAndFeaturedCourses.Column.BEGIN_DATE_SOURCE + " TEXT, "
                + DbStructureEnrolledAndFeaturedCourses.Column.LAST_DEADLINE + " TEXT "
                + ")";
        db.execSQL(sql);
    }

    private static void createSectionTable(SupportSQLiteDatabase db, String name) {
        String sql = "CREATE TABLE " + name
                + " ("
                + DbStructureSections.Column.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + DbStructureSections.Column.SECTION_ID + " LONG, "
                + DbStructureSections.Column.COURSE + " LONG, "
                + DbStructureSections.Column.UNITS + " TEXT, "
                + DbStructureSections.Column.PROGRESS + " TEXT, "
                + DbStructureSections.Column.POSITION + " INTEGER, "
                + DbStructureSections.Column.TITLE + " TEXT, "
                + DbStructureSections.Column.SLUG + " TEXT, "
                + DbStructureSections.Column.BEGIN_DATE + " TEXT, "
                + DbStructureSections.Column.END_DATE + " TEXT, "
                + DbStructureSections.Column.SOFT_DEADLINE + " TEXT, "
                + DbStructureSections.Column.HARD_DEADLINE + " TEXT, "
                + DbStructureSections.Column.GRADING_POLICY + " TEXT, "
                + DbStructureSections.Column.BEGIN_DATE_SOURCE + " TEXT, "
                + DbStructureSections.Column.END_DATE_SOURCE + " TEXT, "
                + DbStructureSections.Column.SOFT_DEADLINE_SOURCE + " TEXT, "
                + DbStructureSections.Column.HARD_DEADLINE_SOURCE + " TEXT, "
                + DbStructureSections.Column.GRADING_POLICY_SOURCE + " TEXT, "
                + DbStructureSections.Column.IS_ACTIVE + " BOOLEAN, "
                + DbStructureSections.Column.IS_CACHED + " BOOLEAN, "
                + DbStructureSections.Column.IS_LOADING + " BOOLEAN, "
                + DbStructureSections.Column.CREATE_DATE + " TEXT, "
                + DbStructureSections.Column.UPDATE_DATE + " TEXT "

                + ")";
        db.execSQL(sql);
    }

    private static void createCachedVideoTable(SupportSQLiteDatabase db, String name) {
        String sql = "CREATE TABLE " + name
                + " ("
                + DbStructureCachedVideo.Column.VIDEO_ID + " LONG, "
                + DbStructureCachedVideo.Column.STEP_ID + " LONG, "
                + DbStructureCachedVideo.Column.URL + " TEXT, "
                + DbStructureCachedVideo.Column.QUALITY + " TEXT, "
                + DbStructureCachedVideo.Column.THUMBNAIL + " TEXT "

                + ")";
        db.execSQL(sql);
    }

    private static void createUnitsDb(SupportSQLiteDatabase db, String name) {
        String sql = "CREATE TABLE " + name
                + " ("
                + DbStructureUnit.Column.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + DbStructureUnit.Column.UNIT_ID + " LONG, "
                + DbStructureUnit.Column.SECTION + " LONG, "
                + DbStructureUnit.Column.LESSON + " LONG, "
                + DbStructureUnit.Column.ASSIGNMENTS + " TEXT, "
                + DbStructureUnit.Column.POSITION + " INTEGER, "
                + DbStructureUnit.Column.PROGRESS + " TEXT, "
                + DbStructureUnit.Column.BEGIN_DATE + " TEXT, "
                + DbStructureUnit.Column.END_DATE + " TEXT, "
                + DbStructureUnit.Column.SOFT_DEADLINE + " TEXT, "
                + DbStructureUnit.Column.HARD_DEADLINE + " TEXT, "
                + DbStructureUnit.Column.GRADING_POLICY + " TEXT, "
                + DbStructureUnit.Column.BEGIN_DATE_SOURCE + " TEXT, "
                + DbStructureUnit.Column.END_DATE_SOURCE + " TEXT, "
                + DbStructureUnit.Column.SOFT_DEADLINE_SOURCE + " TEXT, "
                + DbStructureUnit.Column.HARD_DEADLINE_SOURCE + " TEXT, "
                + DbStructureUnit.Column.GRADING_POLICY_SOURCE + " TEXT, "
                + DbStructureUnit.Column.IS_ACTIVE + " BOOLEAN, "
                + DbStructureUnit.Column.CREATE_DATE + " TEXT, "
                + DbStructureUnit.Column.UPDATE_DATE + " TEXT "
                + ")";
        db.execSQL(sql);
    }

    private static void createLessonsDb(SupportSQLiteDatabase db, String name) {
        String sql = "CREATE TABLE " + name
                + " ("
                + DbStructureLesson.Column.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + DbStructureLesson.Column.LESSON_ID + " LONG, "
                + DbStructureLesson.Column.STEPS + " TEXT, "
                + DbStructureLesson.Column.IS_FEATURED + " BOOLEAN, "
                + DbStructureLesson.Column.IS_PRIME + " BOOLEAN, "
                + DbStructureLesson.Column.PROGRESS + " TEXT, "
                + DbStructureLesson.Column.OWNER + " INTEGER, "
                + DbStructureLesson.Column.SUBSCRIPTIONS + " TEXT, "
                + DbStructureLesson.Column.VIEWED_BY + " INTEGER, "
                + DbStructureLesson.Column.PASSED_BY + " INTEGER, "
                + DbStructureLesson.Column.DEPENDENCIES + " TEXT, "
                + DbStructureLesson.Column.IS_PUBLIC + " BOOLEAN, "
                + DbStructureLesson.Column.TITLE + " TEXT, "
                + DbStructureLesson.Column.SLUG + " TEXT, "
                + DbStructureLesson.Column.CREATE_DATE + " TEXT, "
                + DbStructureLesson.Column.LEARNERS_GROUP + " TEXT, "
                + DbStructureLesson.Column.IS_CACHED + " BOOLEAN, "
                + DbStructureLesson.Column.IS_LOADING + " BOOLEAN, "
                + DbStructureLesson.Column.TEACHER_GROUP + " TEXT "

                + ")";
        db.execSQL(sql);
    }

    private static void createStepsDb(SupportSQLiteDatabase db, String name) {
        String sql = "CREATE TABLE " + name
                + " ("
                + DbStructureStep.Column.STEP_ID + " LONG, "
                + DbStructureStep.Column.LESSON_ID + " LONG, "
                + DbStructureStep.Column.STATUS + " TEXT, "
                + DbStructureStep.Column.PROGRESS + " TEXT, "
                + DbStructureStep.Column.SUBSCRIPTIONS + " TEXT, "
                + DbStructureStep.Column.VIEWED_BY + " LONG, "
                + DbStructureStep.Column.PASSED_BY + " LONG, "
                + DbStructureStep.Column.POSITION + " LONG, "
                + DbStructureStep.Column.CREATE_DATE + " TEXT, "
                + DbStructureStep.Column.IS_CACHED + " BOOLEAN, "
                + DbStructureStep.Column.IS_LOADING + " BOOLEAN, "
                + DbStructureStep.Column.UPDATE_DATE + " TEXT "
                + ")";
        db.execSQL(sql);
    }

    private static void createBlocksDb(SupportSQLiteDatabase db, String name) {
        String sql = "CREATE TABLE " + name
                + " ("
                + DbStructureBlock.Column.STEP_ID + " LONG, "
                + DbStructureBlock.Column.NAME + " TEXT, "
                + DbStructureBlock.Column.TEXT + " TEXT "
                + ")";
        db.execSQL(sql);
    }

    private static void createShareDownloads(SupportSQLiteDatabase db, String name) {
        String sql = "CREATE TABLE " + name
                + " ("
                + DbStructureSharedDownloads.Column.DOWNLOAD_ID + " LONG, "
                + DbStructureSharedDownloads.Column.STEP_ID + " LONG, "
                + DbStructureSharedDownloads.Column.THUMBNAIL + " TEXT, "
                + DbStructureSharedDownloads.Column.QUALITY + " TEXT, "
                + DbStructureSharedDownloads.Column.VIDEO_ID + " LONG "
                + ")";
        db.execSQL(sql);
    }

    private static void createAssignment(SupportSQLiteDatabase db, String name) {
        String sql = "CREATE TABLE " + name
                + " ("
                + DbStructureAssignment.Column.ASSIGNMENT_ID + " LONG, "
                + DbStructureAssignment.Column.UNIT_ID + " LONG, "
                + DbStructureAssignment.Column.STEP_ID + " LONG, "
                + DbStructureAssignment.Column.PROGRESS + " TEXT, "
                + DbStructureAssignment.Column.CREATE_DATE + " TEXT, "
                + DbStructureAssignment.Column.UPDATE_DATE + " TEXT "
                + ")";
        db.execSQL(sql);
    }

    private static void createProgress(SupportSQLiteDatabase db, String name) {
        String sql = "CREATE TABLE " + name
                + " ("
                + DbStructureProgress.Columns.IS_PASSED + " BOOLEAN, "
                + DbStructureProgress.Columns.ID + " TEXT, "
                + DbStructureProgress.Columns.LAST_VIEWED + " TEXT, "
                + DbStructureProgress.Columns.SCORE + " TEXT, "
                + DbStructureProgress.Columns.COST + " INTEGER, "
                + DbStructureProgress.Columns.N_STEPS + " INTEGER, "
                + DbStructureProgress.Columns.N_STEPS_PASSED + " INTEGER "
                + ")";
        db.execSQL(sql);
    }

    private static void createViewQueue(SupportSQLiteDatabase db, String name) {
        String sql = "CREATE TABLE " + name
                + " ("
                + DbStructureViewQueue.Column.STEP_ID + " LONG, "
                + DbStructureViewQueue.Column.ASSIGNMENT_ID + " LONG "
                + ")";
        db.execSQL(sql);
    }

    private static void createNotification(SupportSQLiteDatabase db, String name) {
        String sql = "CREATE TABLE " + name
                + " ("
                + DbStructureNotification.Column.ID + " LONG, "
                + DbStructureNotification.Column.IS_UNREAD + " BOOLEAN, "
                + DbStructureNotification.Column.IS_MUTED + " BOOLEAN, "
                + DbStructureNotification.Column.IS_FAVOURITE + " BOOLEAN, "
                + DbStructureNotification.Column.TIME + " TEXT, "
                + DbStructureNotification.Column.TYPE + " TEXT, "
                + DbStructureNotification.Column.LEVEL + " TEXT, "
                + DbStructureNotification.Column.PRIORITY + " TEXT, "
                + DbStructureNotification.Column.HTML_TEXT + " TEXT, "
                + DbStructureNotification.Column.ACTION + " TEXT, "
                + DbStructureNotification.Column.COURSE_ID + " LONG "
                + ")";
        db.execSQL(sql);
    }

    private static void createCalendarSection(SupportSQLiteDatabase db, String name) {
        String sql = "CREATE TABLE " + name
                + " ("
                + DbStructureCalendarSection.Column.SECTION_ID + " LONG, "
                + DbStructureCalendarSection.Column.EVENT_ID_HARD + " LONG, "
                + DbStructureCalendarSection.Column.EVENT_ID_SOFT + " LONG, "
                + DbStructureCalendarSection.Column.HARD_DEADLINE + " TEXT, "
                + DbStructureCalendarSection.Column.SOFT_DEADLINE + " TEXT "
                + ")";
        db.execSQL(sql);
    }


    private static void createVideoTimestamp(SupportSQLiteDatabase db) {
        String sql = "CREATE TABLE " + DbStructureVideoTimestamp.VIDEO_TIMESTAMP
                + " ("
                + DbStructureVideoTimestamp.Column.VIDEO_ID + WHITESPACE + LONG_TYPE + ", "
                + DbStructureVideoTimestamp.Column.TIMESTAMP + WHITESPACE + LONG_TYPE
                + ")";
        db.execSQL(sql);
    }

    private static void createVideoUrlTable(SupportSQLiteDatabase db, String name) {
        String sql = "CREATE TABLE " + name
                + " ("
                + DbStructureVideoUrl.Column.videoId + WHITESPACE + LONG_TYPE + ", "
                + DbStructureVideoUrl.Column.quality + WHITESPACE + TEXT_TYPE + ", "
                + DbStructureVideoUrl.Column.url + WHITESPACE + TEXT_TYPE
                + ")";
        db.execSQL(sql);
    }

    private static void createSearchQueryTable(SupportSQLiteDatabase db) {
        String sql = "CREATE TABLE " + DbStructureSearchQuery.SEARCH_QUERY
                + " ("
                + DbStructureSearchQuery.Column.QUERY_HASH + WHITESPACE + LONG_TYPE + " PRIMARY KEY, "
                + DbStructureSearchQuery.Column.QUERY_TEXT + WHITESPACE + TEXT_TYPE + ", "
                + DbStructureSearchQuery.Column.QUERY_TIMESTAMP + WHITESPACE + DATETIME_TYPE + WHITESPACE + DEFAULT + WHITESPACE + "(DATETIME('now', 'utc'))"
                + ")";
        db.execSQL(sql);
    }

    private static void createSearchQueryTableSizeLimiterTrigger(SupportSQLiteDatabase db) {
        String sql = "CREATE TRIGGER IF NOT EXISTS" + WHITESPACE + DbStructureSearchQuery.LIMITER_TRIGGER_NAME + WHITESPACE
                + "AFTER INSERT ON" + WHITESPACE + DbStructureSearchQuery.SEARCH_QUERY + WHITESPACE
                + "BEGIN" + WHITESPACE
                + "DELETE FROM" + WHITESPACE + DbStructureSearchQuery.SEARCH_QUERY + WHITESPACE
                + "WHERE" + WHITESPACE + DbStructureSearchQuery.Column.QUERY_TEXT + WHITESPACE
                + "IN"
                + "(SELECT" + WHITESPACE + DbStructureSearchQuery.Column.QUERY_TEXT + WHITESPACE
                + "FROM" + WHITESPACE + DbStructureSearchQuery.SEARCH_QUERY + WHITESPACE
                + "ORDER BY" + WHITESPACE + DbStructureSearchQuery.Column.QUERY_TIMESTAMP + WHITESPACE + "DESC" + WHITESPACE
                + "LIMIT -1 OFFSET" + WHITESPACE + DbStructureSearchQuery.TABLE_SIZE_LIMIT + ");"
                + "END;";
        db.execSQL(sql);
    }

    private static void createViewedNotificationsQueueTable(SupportSQLiteDatabase db) {
        String sql = "CREATE TABLE " + DbStructureViewedNotificationsQueue.VIEWED_NOTIFICATIONS_QUEUE
                + " ("
                + DbStructureViewedNotificationsQueue.Column.NOTIFICATION_ID + WHITESPACE + LONG_TYPE + " PRIMARY KEY"
                + ")";
        db.execSQL(sql);
    }

    private static void createAdaptiveExpTable(SupportSQLiteDatabase db) {
        String sql = "CREATE TABLE IF NOT EXISTS" + WHITESPACE + DbStructureAdaptiveExp.ADAPTIVE_EXP + WHITESPACE
                + " ("
                + DbStructureAdaptiveExp.Column.EXP + WHITESPACE + LONG_TYPE + ", "
                + DbStructureAdaptiveExp.Column.COURSE_ID + WHITESPACE + LONG_TYPE + ", "
                + DbStructureAdaptiveExp.Column.SUBMISSION_ID + WHITESPACE + LONG_TYPE + ", "
                + DbStructureAdaptiveExp.Column.SOLVED_AT + WHITESPACE + DATETIME_TYPE + WHITESPACE + DEFAULT + WHITESPACE + "CURRENT_TIMESTAMP" + ", "
                + "PRIMARY KEY (" + DbStructureAdaptiveExp.Column.COURSE_ID + ", " + DbStructureAdaptiveExp.Column.SUBMISSION_ID + ")"
                + ")";
        db.execSQL(sql);
    }
}
