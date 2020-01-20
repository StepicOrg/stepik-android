package org.stepic.droid.storage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.stepic.droid.storage.migration.MigrationFrom33To34;
import org.stepic.droid.storage.migration.MigrationFrom34To35;
import org.stepic.droid.storage.migration.MigrationFrom35To36;
import org.stepic.droid.storage.migration.MigrationFrom36To37;
import org.stepic.droid.storage.migration.MigrationFrom37To38;
import org.stepic.droid.storage.migration.MigrationFrom38To39;
import org.stepic.droid.storage.migration.MigrationFrom39To40;
import org.stepic.droid.storage.migration.MigrationFrom40To41;
import org.stepic.droid.storage.migration.MigrationFrom41To42;
import org.stepic.droid.storage.migration.MigrationFrom42To43;
import org.stepic.droid.storage.migration.MigrationFrom43To44;
import org.stepic.droid.storage.migration.MigrationFrom44To45;
import org.stepic.droid.storage.migration.MigrationFrom45To46;
import org.stepic.droid.storage.migration.MigrationFrom46To47;
import org.stepic.droid.storage.migration.MigrationFrom47To48;
import org.stepic.droid.storage.migration.MigrationFrom48To49;
import org.stepic.droid.storage.migration.MigrationFrom49To50;
import org.stepic.droid.storage.migration.MigrationFrom50To51;
import org.stepic.droid.storage.structure.DatabaseInfo;
import org.stepic.droid.storage.structure.DbStructureAdaptiveExp;
import org.stepic.droid.storage.structure.DbStructureAssignment;
import org.stepic.droid.storage.structure.DbStructureBlock;
import org.stepic.droid.storage.structure.DbStructureCachedVideo;
import org.stepic.droid.storage.structure.DbStructureCalendarSection;
import org.stepic.droid.storage.structure.DbStructureCertificateViewItem;
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

import javax.inject.Inject;

public final class DatabaseHelper extends SQLiteOpenHelper {
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


    @Inject
    public DatabaseHelper(Context context) {
        super(context, DatabaseInfo.FILE_NAME, null, DatabaseInfo.VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createCourseTable(db, DbStructureEnrolledAndFeaturedCourses.ENROLLED_COURSES);
        createCourseTable(db, DbStructureEnrolledAndFeaturedCourses.FEATURED_COURSES);
        createSectionTable(db, DbStructureSections.SECTIONS);
        createCachedVideoTable(db, DbStructureCachedVideo.CACHED_VIDEO);
        createUnitsDb(db, DbStructureUnit.UNITS);
        createLessonsDb(db, DbStructureLesson.LESSONS);
        createStepsDb(db, DbStructureStep.STEPS);
        createBlocksDb(db, DbStructureBlock.BLOCKS);
        createShareDownloads(db, DbStructureSharedDownloads.SHARED_DOWNLOADS);

        //from version 2:
        createAssignment(db, DbStructureAssignment.ASSIGNMENTS);
        createProgress(db, DbStructureProgress.TABLE_NAME);
        createViewQueue(db, DbStructureViewQueue.VIEW_QUEUE);


//Use new manner for upgrade, it is more safety and maintainability (but may be less effective in attachView) :
        upgradeFrom3To4(db);
        upgradeFrom4To5(db);
        upgradeFrom5To6(db);
        upgradeFrom6To7(db);
        upgradeFrom7To8(db);
        upgradeFrom8To9(db);
        upgradeFrom9To10(db);
        upgradeFrom10To11(db);
        upgradeFrom11To12(db);
        upgradeFrom12To13(db);
        upgradeFrom13To14(db);
        upgradeFrom14To15(db);
        upgradeFrom15To16(db);
        upgradeFrom16To17(db);
        upgradeFrom17To18(db);
        upgradeFrom18To19(db);
        upgradeFrom19To20();
        upgradeFrom20To21(db);
        upgradeFrom21To22(db);
        upgradeFrom22To23(db);
        upgradeFrom23To24(db);
        upgradeFrom24To25(db);
        upgradeFrom25To26(db);
        upgradeFrom26To27(db);
        upgradeFrom27To28(db);
        upgradeFrom28To29(db);
        upgradeFrom29To30(db);
        upgradeFrom30To31(db);
        upgradeFrom31To32(db);
        upgradeFrom32To33(db);
        upgradeFrom33To34(db);
        upgradeFrom34To35(db);
        upgradeFrom35To36(db);
        upgradeFrom36To37(db);
        upgradeFrom37To38(db);
        upgradeFrom38To39(db);
        upgradeFrom39To40(db);
        upgradeFrom40To41(db);
        upgradeFrom41To42(db);
        upgradeFrom42To43(db);
        upgradeFrom43To44(db);
        upgradeFrom44To45(db);
        upgradeFrom45To46(db);
        upgradeFrom46To47(db);
        upgradeFrom47To48(db);
        upgradeFrom48To49(db);
        upgradeFrom49To50(db);
        upgradeFrom50To51(db);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
//            update from 1 to 2
            createAssignment(db, DbStructureAssignment.ASSIGNMENTS);
            createProgress(db, DbStructureProgress.TABLE_NAME);
            createViewQueue(db, DbStructureViewQueue.VIEW_QUEUE);
        }

        if (oldVersion < 3) {
            //update from 2 to 3
            String upgradeToV3 =
                    "ALTER TABLE " + DbStructureCachedVideo.CACHED_VIDEO + " ADD COLUMN "
                            + DbStructureCachedVideo.Column.QUALITY + " TEXT ";
            db.execSQL(upgradeToV3);

            upgradeToV3 = "ALTER TABLE " + DbStructureSharedDownloads.SHARED_DOWNLOADS + " ADD COLUMN "
                    + DbStructureSharedDownloads.Column.QUALITY + " TEXT ";
            db.execSQL(upgradeToV3);


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
        if (oldVersion < 4) {
            upgradeFrom3To4(db);
        }

        if (oldVersion < 5) {
            upgradeFrom4To5(db);
        }

        if (oldVersion < 6) {
            upgradeFrom5To6(db);
        }

        if (oldVersion < 7) {
            upgradeFrom6To7(db);
        }

        if (oldVersion < 8) {
            upgradeFrom7To8(db);
        }

        if (oldVersion < 9) {
            upgradeFrom8To9(db);
        }

        if (oldVersion < 10) {
            upgradeFrom9To10(db);
        }

        if (oldVersion < 11) {
            upgradeFrom10To11(db);
        }

        if (oldVersion < 12) {
            upgradeFrom11To12(db);
        }

        if (oldVersion < 13) {
            upgradeFrom12To13(db);
        }

        if (oldVersion < 14) {
            upgradeFrom13To14(db);
        }

        if (oldVersion < 15) {
            upgradeFrom14To15(db);
        }

        if (oldVersion < 16) {
            upgradeFrom15To16(db);
        }

        if (oldVersion < 17) {
            upgradeFrom16To17(db);
        }

        if (oldVersion < 18) {
            upgradeFrom17To18(db);
        }

        if (oldVersion < 19) {
            upgradeFrom18To19(db);
        }

        if (oldVersion < 20) {
            upgradeFrom19To20();
        }

        if (oldVersion < 21) {
            upgradeFrom20To21(db);
        }

        if (oldVersion < 22) {
            upgradeFrom21To22(db);
        }

        if (oldVersion < 23) {
            upgradeFrom22To23(db);
        }

        if (oldVersion < 24) {
            upgradeFrom23To24(db);
        }

        if (oldVersion < 25) {
            upgradeFrom24To25(db);
        }

        if (oldVersion < 26) {
            upgradeFrom25To26(db);
        }

        if (oldVersion < 27) {
            upgradeFrom26To27(db);
        }

        if (oldVersion < 28) {
            upgradeFrom27To28(db);
        }

        if (oldVersion < 29) {
            upgradeFrom28To29(db);
        }

        if (oldVersion < 30) {
            upgradeFrom29To30(db);
        }

        if (oldVersion < 31) {
            upgradeFrom30To31(db);
        }

        if (oldVersion < 32) {
            upgradeFrom31To32(db);
        }

        if (oldVersion < 33) {
            upgradeFrom32To33(db);
        }

        if (oldVersion < 34) {
            upgradeFrom33To34(db);
        }

        if (oldVersion < 35) {
            upgradeFrom34To35(db);
        }

        if (oldVersion < 36) {
            upgradeFrom35To36(db);
        }

        if (oldVersion < 37) {
            upgradeFrom36To37(db);
        }

        if (oldVersion < 38) {
            upgradeFrom37To38(db);
        }

        if (oldVersion < 39) {
            upgradeFrom38To39(db);
        }

        if (oldVersion < 40) {
            upgradeFrom39To40(db);
        }

        if (oldVersion < 41) {
            upgradeFrom40To41(db);
        }

        if (oldVersion < 42) {
            upgradeFrom41To42(db);
        }

        if (oldVersion < 43) {
            upgradeFrom42To43(db);
        }

        if (oldVersion < 44) {
            upgradeFrom43To44(db);
        }

        if (oldVersion < 45) {
            upgradeFrom44To45(db);
        }

        if (oldVersion < 46) {
            upgradeFrom45To46(db);
        }

        if (oldVersion < 47) {
            upgradeFrom46To47(db);
        }

        if (oldVersion < 48) {
            upgradeFrom47To48(db);
        }

        if (oldVersion < 49) {
            upgradeFrom48To49(db);
        }

        if (oldVersion < 50) {
            upgradeFrom49To50(db);
        }

        if (oldVersion < 51) {
            upgradeFrom50To51(db);
        }
    }

    private void upgradeFrom50To51(SQLiteDatabase db) {
        MigrationFrom50To51.INSTANCE.migrate(db);
    }

    private void upgradeFrom49To50(SQLiteDatabase db) {
        MigrationFrom49To50.INSTANCE.migrate(db);
    }

    private void upgradeFrom48To49(SQLiteDatabase db) {
        MigrationFrom48To49.INSTANCE.migrate(db);
    }

    private void upgradeFrom47To48(SQLiteDatabase db) {
        MigrationFrom47To48.INSTANCE.migrate(db);
    }

    private void upgradeFrom46To47(SQLiteDatabase db) {
        MigrationFrom46To47.INSTANCE.migrate(db);
    }

    private void upgradeFrom45To46(SQLiteDatabase db) {
        MigrationFrom45To46.INSTANCE.migrate(db);
    }

    private void upgradeFrom44To45(SQLiteDatabase db) {
        MigrationFrom44To45.INSTANCE.migrate(db);
    }

    private void upgradeFrom43To44(SQLiteDatabase db) {
        MigrationFrom43To44.INSTANCE.migrate(db);
    }

    private void upgradeFrom42To43(SQLiteDatabase db) {
        MigrationFrom42To43.INSTANCE.migrate(db);
    }

    private void upgradeFrom41To42(SQLiteDatabase db) {
        MigrationFrom41To42.INSTANCE.migrate(db);
    }

    private void upgradeFrom40To41(SQLiteDatabase db) {
        MigrationFrom40To41.INSTANCE.migrate(db);
    }

    private void upgradeFrom39To40(SQLiteDatabase db) {
        MigrationFrom39To40.INSTANCE.migrate(db);
    }

    private void upgradeFrom38To39(SQLiteDatabase db) {
        MigrationFrom38To39.INSTANCE.migrate(db);
    }

    private void upgradeFrom37To38(SQLiteDatabase db) {
        MigrationFrom37To38.INSTANCE.migrate(db);
    }

    private void upgradeFrom36To37(SQLiteDatabase db) {
        MigrationFrom36To37.INSTANCE.migrate(db);
    }

    private void upgradeFrom35To36(SQLiteDatabase db) {
        MigrationFrom35To36.INSTANCE.migrate(db);
    }

    private void upgradeFrom34To35(SQLiteDatabase db) {
        MigrationFrom34To35.INSTANCE.migrate(db);
    }

    private void upgradeFrom33To34(SQLiteDatabase db) {
        MigrationFrom33To34.INSTANCE.migrate(db);
    }

    private void upgradeFrom32To33(SQLiteDatabase db) {
        DbStructureDeadlines.INSTANCE.createTable(db);
        DbStructureDeadlinesBanner.INSTANCE.createTable(db);
    }

    private void upgradeFrom31To32(SQLiteDatabase db) {
        createAdaptiveExpTable(db);
    }

    private void upgradeFrom30To31(SQLiteDatabase db) {
        createViewedNotificationsQueueTable(db);
    }

    private void upgradeFrom29To30(SQLiteDatabase db) {
        createSearchQueryTable(db);
        createSearchQueryTableSizeLimiterTrigger(db);
    }

    private void upgradeFrom28To29(SQLiteDatabase db) {
        alterColumn(db, DbStructureEnrolledAndFeaturedCourses.ENROLLED_COURSES, DbStructureEnrolledAndFeaturedCourses.Column.AVERAGE_RATING, REAL_TYPE);
        alterColumn(db, DbStructureEnrolledAndFeaturedCourses.FEATURED_COURSES, DbStructureEnrolledAndFeaturedCourses.Column.AVERAGE_RATING, REAL_TYPE);

        alterColumn(db, DbStructureEnrolledAndFeaturedCourses.ENROLLED_COURSES, DbStructureEnrolledAndFeaturedCourses.Column.REVIEW_SUMMARY, INT_TYPE);
        alterColumn(db, DbStructureEnrolledAndFeaturedCourses.FEATURED_COURSES, DbStructureEnrolledAndFeaturedCourses.Column.REVIEW_SUMMARY, INT_TYPE);
    }

    private void upgradeFrom27To28(SQLiteDatabase db) {
        alterColumn(db, DbStructureEnrolledAndFeaturedCourses.ENROLLED_COURSES, DbStructureEnrolledAndFeaturedCourses.Column.PROGRESS, TEXT_TYPE);
        alterColumn(db, DbStructureEnrolledAndFeaturedCourses.FEATURED_COURSES, DbStructureEnrolledAndFeaturedCourses.Column.PROGRESS, TEXT_TYPE);
    }

    private void upgradeFrom26To27(SQLiteDatabase db) {
        alterColumn(db, DbStructureSections.SECTIONS, DbStructureSections.Column.IS_REQUIREMENT_SATISFIED, BOOLEAN_TYPE, TRUE_VALUE);
        alterColumn(db, DbStructureSections.SECTIONS, DbStructureSections.Column.REQUIRED_PERCENT, INT_TYPE);
        alterColumn(db, DbStructureSections.SECTIONS, DbStructureSections.Column.REQUIRED_SECTION, LONG_TYPE);
    }

    private void upgradeFrom25To26(SQLiteDatabase db) {}

    private void upgradeFrom24To25(SQLiteDatabase db) {
        alterColumn(db, DbStructureBlock.BLOCKS, DbStructureBlock.Column.CODE_OPTIONS, TEXT_TYPE);
    }

    private void upgradeFrom23To24(SQLiteDatabase db) {
        alterColumn(db, DbStructureEnrolledAndFeaturedCourses.ENROLLED_COURSES, DbStructureEnrolledAndFeaturedCourses.Column.LEARNERS_COUNT, LONG_TYPE);
        alterColumn(db, DbStructureEnrolledAndFeaturedCourses.FEATURED_COURSES, DbStructureEnrolledAndFeaturedCourses.Column.LEARNERS_COUNT, LONG_TYPE);
    }

    private void upgradeFrom22To23(SQLiteDatabase db) {
        alterColumn(db, DbStructureBlock.BLOCKS, DbStructureBlock.Column.EXTERNAL_VIDEO_DURATION, LONG_TYPE);
    }

    private void upgradeFrom21To22(SQLiteDatabase db) {
        createVideoUrlTable(db, DbStructureVideoUrl.externalVideosName);
        alterColumn(db, DbStructureBlock.BLOCKS, DbStructureBlock.Column.EXTERNAL_THUMBNAIL, TEXT_TYPE);
        alterColumn(db, DbStructureBlock.BLOCKS, DbStructureBlock.Column.EXTERNAL_VIDEO_ID, LONG_TYPE);
    }

    private void upgradeFrom20To21(SQLiteDatabase db) {
//        createCourseLastInteractionTable(db); // There was creating of table, but not it is not needed
    }

    private void upgradeFrom19To20() {
        // NO ACTION FOR LEGACY
    }

    private void upgradeFrom18To19(SQLiteDatabase db) {
        alterColumn(db, DbStructureEnrolledAndFeaturedCourses.ENROLLED_COURSES, DbStructureEnrolledAndFeaturedCourses.Column.LAST_STEP_ID, TEXT_TYPE);
        alterColumn(db, DbStructureEnrolledAndFeaturedCourses.FEATURED_COURSES, DbStructureEnrolledAndFeaturedCourses.Column.LAST_STEP_ID, TEXT_TYPE);

        alterColumn(db, DbStructureEnrolledAndFeaturedCourses.ENROLLED_COURSES, DbStructureEnrolledAndFeaturedCourses.Column.IS_ACTIVE, BOOLEAN_TYPE);
        alterColumn(db, DbStructureEnrolledAndFeaturedCourses.FEATURED_COURSES, DbStructureEnrolledAndFeaturedCourses.Column.IS_ACTIVE, BOOLEAN_TYPE);
    }

    private void upgradeFrom17To18(SQLiteDatabase db) {
        alterColumn(db, DbStructureStep.STEPS, DbStructureStep.Column.HAS_SUBMISSION_RESTRICTION, BOOLEAN_TYPE);
        alterColumn(db, DbStructureStep.STEPS, DbStructureStep.Column.MAX_SUBMISSION_COUNT, INT_TYPE);
    }

    private void upgradeFrom16To17(SQLiteDatabase db) {
        alterColumn(db, DbStructureSections.SECTIONS, DbStructureSections.Column.IS_EXAM, BOOLEAN_TYPE);
    }

    private void upgradeFrom15To16(SQLiteDatabase db) {
        alterColumn(db, DbStructureSections.SECTIONS, DbStructureSections.Column.DISCOUNTING_POLICY, INT_TYPE);
    }

    private void upgradeFrom14To15(SQLiteDatabase db) {
        createVideoTimestamp(db);
    }

    private void upgradeFrom13To14(SQLiteDatabase db) {
        alterColumn(db, DbStructureStep.STEPS, DbStructureStep.Column.PEER_REVIEW, TEXT_TYPE);
    }

    private void upgradeFrom12To13(SQLiteDatabase db) {
        alterColumn(db, DbStructureEnrolledAndFeaturedCourses.ENROLLED_COURSES, DbStructureEnrolledAndFeaturedCourses.Column.BEGIN_DATE, TEXT_TYPE);
        alterColumn(db, DbStructureEnrolledAndFeaturedCourses.FEATURED_COURSES, DbStructureEnrolledAndFeaturedCourses.Column.BEGIN_DATE, TEXT_TYPE);

        alterColumn(db, DbStructureEnrolledAndFeaturedCourses.ENROLLED_COURSES, DbStructureEnrolledAndFeaturedCourses.Column.END_DATE, TEXT_TYPE);
        alterColumn(db, DbStructureEnrolledAndFeaturedCourses.FEATURED_COURSES, DbStructureEnrolledAndFeaturedCourses.Column.END_DATE, TEXT_TYPE);
    }

    private void upgradeFrom11To12(SQLiteDatabase db) {
        alterColumn(db, DbStructureSections.SECTIONS, DbStructureSections.Column.TEST_SECTION, BOOLEAN_TYPE);
    }

    private void upgradeFrom10To11(SQLiteDatabase db) {
        createCertificateView(db, DbStructureCertificateViewItem.CERTIFICATE_VIEW_ITEM);
    }

    private void upgradeFrom9To10(SQLiteDatabase db) {
        createCalendarSection(db, DbStructureCalendarSection.CALENDAR_SECTION);
    }

    private void upgradeFrom8To9(SQLiteDatabase db) {
        alterColumn(db, DbStructureEnrolledAndFeaturedCourses.ENROLLED_COURSES, DbStructureEnrolledAndFeaturedCourses.Column.SCHEDULE_LINK, TEXT_TYPE);
        alterColumn(db, DbStructureEnrolledAndFeaturedCourses.FEATURED_COURSES, DbStructureEnrolledAndFeaturedCourses.Column.SCHEDULE_LINK, TEXT_TYPE);

        alterColumn(db, DbStructureEnrolledAndFeaturedCourses.ENROLLED_COURSES, DbStructureEnrolledAndFeaturedCourses.Column.SCHEDULE_LONG_LINK, TEXT_TYPE);
        alterColumn(db, DbStructureEnrolledAndFeaturedCourses.FEATURED_COURSES, DbStructureEnrolledAndFeaturedCourses.Column.SCHEDULE_LONG_LINK, TEXT_TYPE);
    }

    private void upgradeFrom7To8(SQLiteDatabase db) {
        alterColumn(db, DbStructureStep.STEPS, DbStructureStep.Column.DISCUSSION_COUNT, INT_TYPE);
        alterColumn(db, DbStructureStep.STEPS, DbStructureStep.Column.DISCUSSION_ID, TEXT_TYPE);
    }

    private void upgradeFrom6To7(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + DbStructureNotification.NOTIFICATIONS_TEMP);
        createNotification(db, DbStructureNotification.NOTIFICATIONS_TEMP);
    }

    private void upgradeFrom3To4(SQLiteDatabase db) {
        alterColumn(db, DbStructureEnrolledAndFeaturedCourses.ENROLLED_COURSES, DbStructureEnrolledAndFeaturedCourses.Column.CERTIFICATE, TEXT_TYPE);
        alterColumn(db, DbStructureEnrolledAndFeaturedCourses.FEATURED_COURSES, DbStructureEnrolledAndFeaturedCourses.Column.CERTIFICATE, TEXT_TYPE);
    }

    private void upgradeFrom4To5(SQLiteDatabase db) {
        alterColumn(db, DbStructureEnrolledAndFeaturedCourses.ENROLLED_COURSES, DbStructureEnrolledAndFeaturedCourses.Column.INTRO_VIDEO_ID, LONG_TYPE);
        alterColumn(db, DbStructureEnrolledAndFeaturedCourses.FEATURED_COURSES, DbStructureEnrolledAndFeaturedCourses.Column.INTRO_VIDEO_ID, LONG_TYPE);
    }

    private void upgradeFrom5To6(SQLiteDatabase db) {
        alterColumn(db, DbStructureLesson.LESSONS, DbStructureLesson.Column.COVER_URL, TEXT_TYPE);
    }

    private void alterColumn(SQLiteDatabase db, String dbName, String column, String type) {
        String upgrade = "ALTER TABLE " + dbName + " ADD COLUMN "
                + column + " " + type + " ";
        db.execSQL(upgrade);
    }

    private void alterColumn(SQLiteDatabase db, String dbName, String column, String type, String defaultValue) {
        String upgrade = "ALTER TABLE " + dbName + " ADD COLUMN "
                + column + WHITESPACE + type + WHITESPACE + DEFAULT + WHITESPACE + defaultValue;
        db.execSQL(upgrade);
    }


    private void createCourseTable(SQLiteDatabase db, String name) {
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

    private void createSectionTable(SQLiteDatabase db, String name) {
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

    private void createCachedVideoTable(SQLiteDatabase db, String name) {
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

    private void createUnitsDb(SQLiteDatabase db, String name) {
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

    private void createLessonsDb(SQLiteDatabase db, String name) {
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

    private void createStepsDb(SQLiteDatabase db, String name) {
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

    private void createBlocksDb(SQLiteDatabase db, String name) {
        String sql = "CREATE TABLE " + name
                + " ("
                + DbStructureBlock.Column.STEP_ID + " LONG, "
                + DbStructureBlock.Column.NAME + " TEXT, "
                + DbStructureBlock.Column.TEXT + " TEXT "
                + ")";
        db.execSQL(sql);
    }

    private void createShareDownloads(SQLiteDatabase db, String name) {
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

    private void createAssignment(SQLiteDatabase db, String name) {
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

    private void createProgress(SQLiteDatabase db, String name) {
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

    private void createViewQueue(SQLiteDatabase db, String name) {
        String sql = "CREATE TABLE " + name
                + " ("
                + DbStructureViewQueue.Column.STEP_ID + " LONG, "
                + DbStructureViewQueue.Column.ASSIGNMENT_ID + " LONG "
                + ")";
        db.execSQL(sql);
    }

    private void createNotification(SQLiteDatabase db, String name) {
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

    private void createCalendarSection(SQLiteDatabase db, String name) {
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

    private void createCertificateView(SQLiteDatabase db, String name) {
        String sql = "CREATE TABLE " + name
                + " ("
                + DbStructureCertificateViewItem.Column.CERTIFICATE_ID + " LONG, "
                + DbStructureCertificateViewItem.Column.TITLE + " TEXT, "
                + DbStructureCertificateViewItem.Column.COVER_FULL_PATH + " TEXT, "
                + DbStructureCertificateViewItem.Column.TYPE + " INTEGER, "
                + DbStructureCertificateViewItem.Column.FULL_PATH + " TEXT, "
                + DbStructureCertificateViewItem.Column.GRADE + " TEXT, "
                + DbStructureCertificateViewItem.Column.ISSUE_DATE + " TEXT "
                + ")";
        db.execSQL(sql);
    }


    private void createVideoTimestamp(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + DbStructureVideoTimestamp.VIDEO_TIMESTAMP
                + " ("
                + DbStructureVideoTimestamp.Column.VIDEO_ID + WHITESPACE + LONG_TYPE + ", "
                + DbStructureVideoTimestamp.Column.TIMESTAMP + WHITESPACE + LONG_TYPE
                + ")";
        db.execSQL(sql);
    }

    private void createVideoUrlTable(SQLiteDatabase db, String name) {
        String sql = "CREATE TABLE " + name
                + " ("
                + DbStructureVideoUrl.Column.videoId + WHITESPACE + LONG_TYPE + ", "
                + DbStructureVideoUrl.Column.quality + WHITESPACE + TEXT_TYPE + ", "
                + DbStructureVideoUrl.Column.url + WHITESPACE + TEXT_TYPE
                + ")";
        db.execSQL(sql);
    }

    private void createSearchQueryTable(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + DbStructureSearchQuery.SEARCH_QUERY
                + " ("
                + DbStructureSearchQuery.Column.QUERY_HASH + WHITESPACE + LONG_TYPE + " PRIMARY KEY, "
                + DbStructureSearchQuery.Column.QUERY_TEXT + WHITESPACE + TEXT_TYPE + ", "
                + DbStructureSearchQuery.Column.QUERY_TIMESTAMP + WHITESPACE + DATETIME_TYPE + WHITESPACE + DEFAULT + WHITESPACE + "(DATETIME('now', 'utc'))"
                + ")";
        db.execSQL(sql);
    }

    private void createSearchQueryTableSizeLimiterTrigger(SQLiteDatabase db) {
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

    private void createViewedNotificationsQueueTable(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + DbStructureViewedNotificationsQueue.VIEWED_NOTIFICATIONS_QUEUE
                + " ("
                + DbStructureViewedNotificationsQueue.Column.NOTIFICATION_ID + WHITESPACE + LONG_TYPE + " PRIMARY KEY"
                + ")";
        db.execSQL(sql);
    }

    private void createAdaptiveExpTable(SQLiteDatabase db) {
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
