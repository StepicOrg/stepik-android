package org.stepic.droid.store.operations;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.stepic.droid.model.Assignment;
import org.stepic.droid.model.Block;
import org.stepic.droid.model.CachedVideo;
import org.stepic.droid.model.Course;
import org.stepic.droid.model.DownloadEntity;
import org.stepic.droid.model.Lesson;
import org.stepic.droid.model.Progress;
import org.stepic.droid.model.Section;
import org.stepic.droid.model.Step;
import org.stepic.droid.model.Unit;
import org.stepic.droid.model.Video;
import org.stepic.droid.store.structure.DBStructureCourses;
import org.stepic.droid.store.structure.DbStructureAssignment;
import org.stepic.droid.store.structure.DbStructureBlock;
import org.stepic.droid.store.structure.DbStructureCachedVideo;
import org.stepic.droid.store.structure.DbStructureLesson;
import org.stepic.droid.store.structure.DbStructureProgress;
import org.stepic.droid.store.structure.DbStructureSections;
import org.stepic.droid.store.structure.DbStructureSharedDownloads;
import org.stepic.droid.store.structure.DbStructureStep;
import org.stepic.droid.store.structure.DbStructureUnit;
import org.stepic.droid.store.structure.DbStructureViewQueue;
import org.stepic.droid.util.DbParseHelper;
import org.stepic.droid.web.ViewAssignment;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Singleton;

@Singleton
public class DatabaseManager extends DbManagerBase {

    public void addAssignment(Assignment assignment) {
        try {
            open();
            ContentValues values = new ContentValues();

            values.put(DbStructureAssignment.Column.ASSIGNMENT_ID, assignment.getId());
            values.put(DbStructureAssignment.Column.CREATE_DATE, assignment.getCreate_date());
            values.put(DbStructureAssignment.Column.PROGRESS, assignment.getProgress());
            values.put(DbStructureAssignment.Column.STEP_ID, assignment.getStep());
            values.put(DbStructureAssignment.Column.UNIT_ID, assignment.getUnit());
            values.put(DbStructureAssignment.Column.UPDATE_DATE, assignment.getUpdate_date());


            if (isAssignmentInDb(assignment.getId())) {
                database.update(DbStructureAssignment.ASSIGNMENTS, values, DbStructureAssignment.Column.ASSIGNMENT_ID + "=" + assignment.getId(), null);
            } else {
                database.insert(DbStructureAssignment.ASSIGNMENTS, null, values);
            }

        } finally {
            close();
        }
    }


    public long getAssignmentIdByStepId(long stepId) {
        try {
            open();
            String Query = "Select * from " + DbStructureAssignment.ASSIGNMENTS + " where " + DbStructureAssignment.Column.STEP_ID + " =?";
            Cursor cursor = database.rawQuery(Query, new String[]{stepId + ""});

            cursor.moveToFirst();

            if (!cursor.isAfterLast()) {
                long assignmentId = cursor.getLong(cursor.getColumnIndex(DbStructureAssignment.Column.ASSIGNMENT_ID));
                cursor.close();
                return assignmentId;
            }
            cursor.close();
            return -1;
        } finally {
            close();
        }

    }

    public enum Table {
        enrolled(DBStructureCourses.ENROLLED_COURSES),
        featured(DBStructureCourses.FEATURED_COURSES);

        private String description;

        Table(String description) {
            this.description = description;
        }

        private String getStoreName() {
            return description;
        }
    }

    @Nullable
    public Step getStepById(long stepId) {
        try {
            open();

            String Query = "Select * from " + DbStructureStep.STEPS + " where " + DbStructureStep.Column.STEP_ID + " = " + stepId;
            Cursor cursor = database.rawQuery(Query, null);

            cursor.moveToFirst();

            if (!cursor.isAfterLast()) {
                Step step = parseStep(cursor);
                cursor.close();
                return step;
            }
            cursor.close();
            return null;
        } finally {
            close();
        }
    }

    @Nullable
    public Lesson getLessonById(long lessonId) {
        try {
            open();

            String Query = "Select * from " + DbStructureLesson.LESSONS + " where " + DbStructureLesson.Column.LESSON_ID + " = " + lessonId;
            Cursor cursor = database.rawQuery(Query, null);

            cursor.moveToFirst();

            if (!cursor.isAfterLast()) {
                Lesson lesson = parseLesson(cursor);
                cursor.close();
                return lesson;
            }
            cursor.close();
            return null;
        } finally {
            close();
        }
    }

    @Nullable
    public Section getSectionById(long sectionId) {
        try {
            open();

            String Query = "Select * from " + DbStructureSections.SECTIONS + " where " + DbStructureSections.Column.SECTION_ID + " = " + sectionId;
            Cursor cursor = database.rawQuery(Query, null);

            cursor.moveToFirst();

            if (!cursor.isAfterLast()) {
                Section section = parseSection(cursor);
                cursor.close();
                return section;
            }
            cursor.close();
            return null;
        } finally {
            close();
        }
    }

    @Nullable
    public Course getCourseById(long courseId, Table type) {
        try {
            open();

            String Query = "Select * from " + type.getStoreName() + " where " + DBStructureCourses.Column.COURSE_ID + " = " + courseId;
            Cursor cursor = database.rawQuery(Query, null);

            cursor.moveToFirst();

            if (!cursor.isAfterLast()) {
                Course course = parseCourse(cursor);
                cursor.close();
                return course;
            }
            cursor.close();
            return null;
        } finally {
            close();
        }
    }


    @Nullable
    public Unit getUnitByLessonId(long lessonId) {
        try {
            open();

            String Query = "Select * from " + DbStructureUnit.UNITS + " where " + DbStructureUnit.Column.LESSON + " = " + lessonId;
            Cursor cursor = database.rawQuery(Query, null);

            cursor.moveToFirst();

            if (!cursor.isAfterLast()) {
                Unit unit = parseUnit(cursor);
                cursor.close();
                return unit;
            }
            cursor.close();
            return null;
        } finally {
            close();
        }
    }

    public List<DownloadEntity> getAllDownloadEntities() {
        try {
            open();
            List<DownloadEntity> downloadEntities = new ArrayList<>();
            Cursor cursor = getDownloadEntitiesCursor();

            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                DownloadEntity de = parseDownloadEntity(cursor);
                downloadEntities.add(de);
                cursor.moveToNext();
            }
            cursor.close();
            return downloadEntities;

        } finally {
            close();
        }
    }

    public DatabaseManager(Context context) {
        super(context);
    }

    public boolean existStepInCourse(@NotNull Step step, @NotNull Course course) {
        try {
            open();
            Section section = getSectionOfStep(step);
            return section != null && section.getCourse() == course.getCourseId();
        } finally {
            close();
        }
    }

    public boolean existStepInSection(@NotNull Step step, @NotNull Section section) {
        try {
            open();
            Unit unit = getUnitOfStep(step);
            return unit != null && unit.getSection() == section.getId();
        } finally {
            close();
        }
    }

    public boolean existStepInUnit(@NotNull Step step, @NotNull Unit unit) {

        try {
            open();
            Lesson lesson = getLessonOfStep(step);
            return lesson != null && lesson.getId() == unit.getLesson();
        } finally {
            close();
        }
    }

    public boolean existStepIntLesson(@NotNull Step step, @NotNull Lesson lesson) {
        try {
            open();
            if (!isStepInDb(step)) {
                return false;
            }

            return step.getLesson() == lesson.getId();
        } finally {
            close();
        }
    }

    public boolean isCourseLoading(Course course, DatabaseManager.Table type) {
        if (course == null) return false;//// FIXME: 18.11.15 investiagate why null
        try {
            open();
            String Query = "Select * from " + type.getStoreName() + " where " + DBStructureCourses.Column.COURSE_ID + " = " + course.getCourseId();
            Cursor cursor = database.rawQuery(Query, null);
            if (cursor.getCount() <= 0) {
                cursor.close();
                return false;
            }
            cursor.moveToFirst();
            int indexIsLoading = cursor.getColumnIndex(DBStructureCourses.Column.IS_LOADING);
            boolean isLoading = cursor.getInt(indexIsLoading) > 0;
            cursor.close();
            return isLoading;
        } finally {
            close();
        }
    }

    public boolean isCourseCached(Course course, DatabaseManager.Table type) {
        try {
            open();
            String Query = "Select * from " + type.getStoreName() + " where " + DBStructureCourses.Column.COURSE_ID + " = " + course.getCourseId();
            Cursor cursor = database.rawQuery(Query, null);
            if (cursor.getCount() <= 0) {
                cursor.close();
                return false;
            }
            cursor.moveToFirst();
            int indexIsCached = cursor.getColumnIndex(DBStructureCourses.Column.IS_CACHED);
            boolean isCached = cursor.getInt(indexIsCached) > 0;
            cursor.close();
            return isCached;
        } finally {
            close();
        }
    }

    public boolean isSectionLoading(Section section) {
        try {
            open();
            String Query = "Select * from " + DbStructureSections.SECTIONS + " where " + DbStructureSections.Column.SECTION_ID + " = " + section.getId();
            Cursor cursor = database.rawQuery(Query, null);
            if (cursor.getCount() <= 0) {
                cursor.close();
                return false;
            }
            cursor.moveToFirst();
            int indexIsLoading = cursor.getColumnIndex(DbStructureSections.Column.IS_LOADING);
            boolean isLoading = cursor.getInt(indexIsLoading) > 0;
            cursor.close();
            return isLoading;
        } finally {
            close();
        }
    }

    public boolean isSectionCached(Section section) {
        try {
            open();
            String Query = "Select * from " + DbStructureSections.SECTIONS + " where " + DbStructureSections.Column.SECTION_ID + " = " + section.getId();
            Cursor cursor = database.rawQuery(Query, null);
            if (cursor.getCount() <= 0) {
                cursor.close();
                return false;
            }
            cursor.moveToFirst();
            int indexIsCached = cursor.getColumnIndex(DbStructureSections.Column.IS_CACHED);
            boolean isCached = cursor.getInt(indexIsCached) > 0;
            cursor.close();
            return isCached;
        } finally {
            close();
        }
    }


    public boolean isUnitLoading(Unit unit) {
        try {
            open();
            String Query = "Select * from " + DbStructureUnit.UNITS + " where " + DbStructureUnit.Column.UNIT_ID + " = " + unit.getId();
            Cursor cursor = database.rawQuery(Query, null);
            if (cursor.getCount() <= 0) {
                cursor.close();
                return false;
            }
            cursor.moveToFirst();
            int indexIsLoading = cursor.getColumnIndex(DbStructureUnit.Column.IS_LOADING);
            boolean isLoading = cursor.getInt(indexIsLoading) > 0;
            cursor.close();
            return isLoading;
        } finally {
            close();
        }
    }

    public boolean isUnitCached(Unit unit) {
        try {
            open();
            String Query = "Select * from " + DbStructureUnit.UNITS + " where " + DbStructureUnit.Column.UNIT_ID + " = " + unit.getId();
            Cursor cursor = database.rawQuery(Query, null);
            if (cursor.getCount() <= 0) {
                cursor.close();
                return false;
            }
            cursor.moveToFirst();

            int indexIsCached = cursor.getColumnIndex(DbStructureUnit.Column.IS_CACHED);
            boolean isCached = cursor.getInt(indexIsCached) > 0;
            cursor.close();
            return isCached;
        } finally {
            close();
        }
    }


    public boolean isLessonLoading(Lesson lesson) {
        try {
            open();
            String Query = "Select * from " + DbStructureLesson.LESSONS + " where " + DbStructureLesson.Column.LESSON_ID + " = " + lesson.getId();
            Cursor cursor = database.rawQuery(Query, null);
            if (cursor.getCount() <= 0) {
                cursor.close();
                return false;
            }
            cursor.moveToFirst();
            int indexIsLoading = cursor.getColumnIndex(DbStructureLesson.Column.IS_LOADING);
            boolean isLoading = cursor.getInt(indexIsLoading) > 0;
            cursor.close();
            return isLoading;
        } finally {
            close();
        }
    }

    public boolean isLessonCached(Lesson lesson) {
        try {
            open();
            String Query = "Select * from " + DbStructureLesson.LESSONS + " where " + DbStructureLesson.Column.LESSON_ID + " = " + lesson.getId();
            Cursor cursor = database.rawQuery(Query, null);
            if (cursor.getCount() <= 0) {
                cursor.close();
                return false;
            }
            cursor.moveToFirst();

            int indexIsCached = cursor.getColumnIndex(DbStructureLesson.Column.IS_CACHED);
            boolean isCached = cursor.getInt(indexIsCached) > 0;
            cursor.close();
            return isCached;
        } finally {
            close();
        }
    }


    public boolean isStepLoading(Step step) {
        try {
            open();
            String Query = "Select * from " + DbStructureStep.STEPS + " where " + DbStructureStep.Column.STEP_ID + " = " + step.getId();
            Cursor cursor = database.rawQuery(Query, null);
            if (cursor.getCount() <= 0) {
                cursor.close();
                return false;
            }
            cursor.moveToFirst();
            int indexIsLoading = cursor.getColumnIndex(DbStructureStep.Column.IS_LOADING);
            boolean isLoading = cursor.getInt(indexIsLoading) > 0;
            cursor.close();
            return isLoading;
        } finally {
            close();
        }
    }

    public boolean isStepCached(Step step) {
        try {
            open();
            String Query = "Select * from " + DbStructureStep.STEPS + " where " + DbStructureStep.Column.STEP_ID + " = " + step.getId();
            Cursor cursor = database.rawQuery(Query, null);
            if (cursor.getCount() <= 0) {
                cursor.close();
                return false;
            }
            cursor.moveToFirst();
            int indexIsCached = cursor.getColumnIndex(DbStructureStep.Column.IS_CACHED);
            boolean isCached = cursor.getInt(indexIsCached) > 0;
            cursor.close();
            return isCached;
        } finally {
            close();
        }
    }

    public void updateOnlyCachedLoadingStep(Step step) {
        try {
            open();
            ContentValues cv = new ContentValues();
            cv.put(DbStructureStep.Column.IS_LOADING, step.is_loading());
            cv.put(DbStructureStep.Column.IS_CACHED, step.is_cached());

            database.update(DbStructureStep.STEPS, cv, DbStructureStep.Column.STEP_ID + "=" + step.getId(), null);
        } finally {
            close();
        }
    }

    public void updateOnlyCachedLoadingUnit(Unit unit) {
        try {
            open();
            ContentValues cv = new ContentValues();
            cv.put(DbStructureUnit.Column.IS_LOADING, unit.is_loading());
            cv.put(DbStructureUnit.Column.IS_CACHED, unit.is_cached());

            database.update(DbStructureUnit.UNITS, cv, DbStructureUnit.Column.UNIT_ID + "=" + unit.getId(), null);
        } finally {
            close();
        }
    }

    public void updateOnlyCachedLoadingLesson(Lesson lesson) {
        try {
            open();
            ContentValues cv = new ContentValues();
            cv.put(DbStructureLesson.Column.IS_LOADING, lesson.is_loading());
            cv.put(DbStructureLesson.Column.IS_CACHED, lesson.is_cached());

            database.update(DbStructureLesson.LESSONS, cv, DbStructureLesson.Column.LESSON_ID + "=" + lesson.getId(), null);
        } finally {
            close();
        }
    }

    public void updateOnlyCachedLoadingSection(Section section) {
        try {
            open();
            ContentValues cv = new ContentValues();
            cv.put(DbStructureSections.Column.IS_LOADING, section.is_loading());
            cv.put(DbStructureSections.Column.IS_CACHED, section.is_cached());

            database.update(DbStructureSections.SECTIONS, cv, DbStructureSections.Column.SECTION_ID + "=" + section.getId(), null);
        } finally {
            close();
        }
    }

    public void updateOnlyCachedLoadingCourse(Course course, Table type) {
        try {
            open();
            ContentValues cv = new ContentValues();
            cv.put(DBStructureCourses.Column.IS_LOADING, course.is_loading());
            cv.put(DBStructureCourses.Column.IS_CACHED, course.is_cached());

            database.update(type.getStoreName(), cv, DBStructureCourses.Column.COURSE_ID + "=" + course.getCourseId(), null);
        } finally {
            close();
        }
    }


    @Nullable
    private Lesson getLessonOfStep(Step step) {
        if (!isStepInDb(step)) {
            return null;
        }
        String lessonQuery = "Select * from " + DbStructureLesson.LESSONS + " where " + DbStructureLesson.Column.LESSON_ID + " = " + step.getLesson();
        Cursor lessonCursor = database.rawQuery(lessonQuery, null);
        if (lessonCursor.getCount() <= 0) {
            lessonCursor.close();
            return null;
        }
        lessonCursor.moveToFirst();
        Lesson lesson = parseLesson(lessonCursor);
        lessonCursor.close();
        return lesson;
    }

    @Nullable
    private Unit getUnitOfStep(Step step) {
        Lesson lesson = getLessonOfStep(step);
        if (lesson == null) return null;

        String unitQuery = "Select * from " + DbStructureUnit.UNITS + " where " + DbStructureUnit.Column.LESSON + " = " + lesson.getId();
        Cursor unitCursor = database.rawQuery(unitQuery, null);
        if (unitCursor.getCount() <= 0) {
            unitCursor.close();
            return null;
        }
        unitCursor.moveToFirst();
        Unit unit = parseUnit(unitCursor);
        unitCursor.close();
        return unit;
    }

    @Nullable
    private Section getSectionOfStep(Step step) {
        Unit unit = getUnitOfStep(step);
        if (unit == null) return null;

        String sectionQuery = "Select * from " + DbStructureSections.SECTIONS + " where " + DbStructureSections.Column.SECTION_ID + " = " + unit.getSection();
        Cursor sectionCursor = database.rawQuery(sectionQuery, null);
        if (sectionCursor.getCount() <= 0) {
            sectionCursor.close();
            return null;
        }
        sectionCursor.moveToFirst();
        Section section = parseSection(sectionCursor);
        sectionCursor.close();
        return section;
    }


    public List<Course> getAllCourses(DatabaseManager.Table type) {

        try {
            open();
            List<Course> courses = new ArrayList<>();
            Cursor cursor = getCourseCursor(type);

            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                Course course = parseCourse(cursor);
                courses.add(course);
                cursor.moveToNext();
            }
            cursor.close();
            return courses;

        } finally {
            close();
        }
    }

    public void addCourse(Course course, DatabaseManager.Table type) {

        try {
            open();
            ContentValues values = new ContentValues();

            values.put(DBStructureCourses.Column.COURSE_ID, course.getCourseId());
            values.put(DBStructureCourses.Column.SUMMARY, course.getSummary());
            values.put(DBStructureCourses.Column.COVER_LINK, course.getCover());
            values.put(DBStructureCourses.Column.INTRO_LINK_VIMEO, course.getIntro());
            values.put(DBStructureCourses.Column.TITLE, course.getTitle());
            values.put(DBStructureCourses.Column.LANGUAGE, course.getLanguage());
            values.put(DBStructureCourses.Column.BEGIN_DATE_SOURCE, course.getBegin_date_source());
            values.put(DBStructureCourses.Column.LAST_DEADLINE, course.getLast_deadline());
            values.put(DBStructureCourses.Column.DESCRIPTION, course.getDescription());

            String instructorsParsed = DbParseHelper.parseLongArrayToString(course.getInstructors());
            values.put(DBStructureCourses.Column.INSTRUCTORS, instructorsParsed);

            values.put(DBStructureCourses.Column.REQUIREMENTS, course.getRequirements());
            values.put(DBStructureCourses.Column.ENROLLMENT, course.getEnrollment());

            String sectionsParsed = DbParseHelper.parseLongArrayToString(course.getSections());
            values.put(DBStructureCourses.Column.SECTIONS, sectionsParsed);

//            values.put(DBStructureCourses.Column.IS_CACHED, course.is_cached());
//            values.put(DBStructureCourses.Column.IS_LOADING, course.is_loading());


            if (isCourseInDB(course, type)) {
                database.update(type.getStoreName(), values, DBStructureCourses.Column.COURSE_ID + "=" + course.getCourseId(), null);
            } else {
                database.insert(type.getStoreName(), null, values);
            }

        } finally {
            close();
        }


    }

    public void deleteCourse(Course course, DatabaseManager.Table type) {
        try {
            open();
            long courseId = course.getCourseId();
            database.delete(type.getStoreName(),
                    DBStructureCourses.Column.COURSE_ID + " = " + courseId,
                    null);
        } finally {
            close();
        }
    }

    private boolean isCourseInDB(Course course, DatabaseManager.Table type) {
        String Query = "Select * from " + type.getStoreName() + " where " + DBStructureCourses.Column.COURSE_ID + " = " + course.getCourseId();
        Cursor cursor = database.rawQuery(Query, null);
        if (cursor.getCount() <= 0) {
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }

    public void addSection(Section section) {
        try {
            open();
            ContentValues values = new ContentValues();

            values.put(DbStructureSections.Column.SECTION_ID, section.getId());
            values.put(DbStructureSections.Column.TITLE, section.getTitle());
            values.put(DbStructureSections.Column.SLUG, section.getSlug());
            values.put(DbStructureSections.Column.IS_ACTIVE, section.is_active());
            values.put(DbStructureSections.Column.BEGIN_DATE, section.getBegin_date());
            values.put(DbStructureSections.Column.SOFT_DEADLINE, section.getSoft_deadline());
            values.put(DbStructureSections.Column.HARD_DEADLINE, section.getHard_deadline());
            values.put(DbStructureSections.Column.COURSE, section.getCourse());
            values.put(DbStructureSections.Column.POSITION, section.getPosition());
            values.put(DbStructureSections.Column.UNITS, DbParseHelper.parseLongArrayToString(section.getUnits()));
//            values.put(DbStructureSections.Column.IS_CACHED, section.is_cached());
//            values.put(DbStructureSections.Column.IS_LOADING, section.is_loading());

            if (isSectionInDb(section)) {
                database.update(DbStructureSections.SECTIONS, values, DbStructureSections.Column.SECTION_ID + "=" + section.getId(), null);
            } else {
                database.insert(DbStructureSections.SECTIONS, null, values);
            }
        } finally {
            close();
        }
    }

    public void addStep(Step step) {
        try {
            open();

            ContentValues values = new ContentValues();

            values.put(DbStructureStep.Column.STEP_ID, step.getId());
            values.put(DbStructureStep.Column.LESSON_ID, step.getLesson());
            values.put(DbStructureStep.Column.STATUS, step.getStatus());
            values.put(DbStructureStep.Column.PROGRESS, step.getProgress());
            values.put(DbStructureStep.Column.SUBSCRIPTIONS, DbParseHelper.parseStringArrayToString(step.getSubscriptions()));
            values.put(DbStructureStep.Column.VIEWED_BY, step.getViewed_by());
            values.put(DbStructureStep.Column.PASSED_BY, step.getPassed_by());
            values.put(DbStructureStep.Column.CREATE_DATE, step.getCreate_date());
            values.put(DbStructureStep.Column.UPDATE_DATE, step.getUpdate_date());
            values.put(DbStructureStep.Column.POSITION, step.getPosition());
//            values.put(DbStructureStep.Column.IS_CACHED, step.is_cached());
//            values.put(DbStructureStep.Column.IS_LOADING, step.is_loading());

            if (isStepInDb(step)) {
                database.update(DbStructureStep.STEPS, values, DbStructureStep.Column.STEP_ID + "=" + step.getId(), null);
            } else {
                database.insert(DbStructureStep.STEPS, null, values);
            }

            addBlock(step);
        } finally {
            close();
        }
    }

    private void addBlock(Step step) {
        Block block = step.getBlock();
        if (block == null) return;
        ContentValues values = new ContentValues();
        values.put(DbStructureBlock.Column.STEP_ID, step.getId());
        values.put(DbStructureBlock.Column.NAME, block.getName());
        values.put(DbStructureBlock.Column.TEXT, block.getText());

        database.insert(DbStructureBlock.BLOCKS, null, values);
    }

    public void deleteSection(Section section) {
        try {
            open();
            long sectionId = section.getId();
            database.delete(DbStructureSections.SECTIONS,
                    DbStructureSections.Column.SECTION_ID + " = " + sectionId,
                    null);

        } finally {
            close();
        }
    }

    public List<Section> getAllSections() {
        try {
            open();
            List<Section> sections = new ArrayList<>();

            Cursor cursor = getSectionCursor();
            cursor.moveToFirst();

            while (!cursor.isAfterLast()) {
                Section section = parseSection(cursor);
                sections.add(section);
                cursor.moveToNext();
            }

            cursor.close();
            return sections;
        } finally {
            close();
        }
    }

    public List<Section> getAllSectionsOfCourse(Course course) {
        try {
            open();
            List<Section> sections = new ArrayList<>();

            String Query = "Select * from " + DbStructureSections.SECTIONS + " where " + DbStructureSections.Column.COURSE + " = " + course.getCourseId();
            Cursor cursor = database.rawQuery(Query, null);

            cursor.moveToFirst();

            while (!cursor.isAfterLast()) {
                Section section = parseSection(cursor);
                sections.add(section);
                cursor.moveToNext();
            }

            cursor.close();
            return sections;
        } finally {
            close();
        }
    }

    public List<Unit> getAllUnitsOfSection(long sectionId) {
        try {
            open();
            List<Unit> units = new ArrayList<>();

            String Query = "Select * from " + DbStructureUnit.UNITS + " where " + DbStructureUnit.Column.SECTION + " = " + sectionId;
            Cursor cursor = database.rawQuery(Query, null);

            cursor.moveToFirst();

            while (!cursor.isAfterLast()) {
                Unit unit = parseUnit(cursor);
                units.add(unit);
                cursor.moveToNext();
            }

            cursor.close();
            return units;
        } finally {
            close();
        }
    }

    public List<Step> getStepsOfLesson(long lessonId) {
        try {
            open();
            List<Step> steps = new ArrayList<>();

            String Query = "Select * from " + DbStructureStep.STEPS + " where " + DbStructureStep.Column.LESSON_ID + " = " + lessonId;
            Cursor cursor = database.rawQuery(Query, null);

            cursor.moveToFirst();

            while (!cursor.isAfterLast()) {
                Step step = parseStep(cursor);
                steps.add(step);
                cursor.moveToNext();
            }

            cursor.close();
            return steps;
        } finally {
            close();
        }
    }

    public Lesson getLessonOfUnit(Unit unit) {
        try {
            open();

            String Query = "Select * from " + DbStructureLesson.LESSONS + " where " + DbStructureLesson.Column.LESSON_ID + " = " + unit.getLesson();
            Cursor cursor = database.rawQuery(Query, null);

            cursor.moveToFirst();

            Lesson lesson = null;
            if (!cursor.isAfterLast()) {
                lesson = parseLesson(cursor);
            }

            cursor.close();
            return lesson;
        } finally {
            close();
        }
    }


    private boolean isSectionInDb(Section section) {

        String Query = "Select * from " + DbStructureSections.SECTIONS + " where " + DbStructureSections.Column.SECTION_ID + " = " + section.getId();
        Cursor cursor = database.rawQuery(Query, null);
        if (cursor.getCount() <= 0) {
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }

    private boolean isStepInDb(Step step) {
        return isStepInDb(step.getId());
    }

    private boolean isStepInDb(long stepId) {
        String Query = "Select * from " + DbStructureStep.STEPS + " where " + DbStructureStep.Column.STEP_ID + " = " + stepId;
        Cursor cursor = database.rawQuery(Query, null);
        if (cursor.getCount() <= 0) {
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }

    public void addVideo(CachedVideo cachedVideo) {
        try {
            Log.i("downloading", "pre open for video id " + cachedVideo.getVideoId());
            open();
            Log.i("downloading", "after open for video id " + cachedVideo.getVideoId());
            ContentValues values = new ContentValues();

            values.put(DbStructureCachedVideo.Column.VIDEO_ID, cachedVideo.getVideoId());
            values.put(DbStructureCachedVideo.Column.STEP_ID, cachedVideo.getStepId());
            values.put(DbStructureCachedVideo.Column.URL, cachedVideo.getUrl());
            values.put(DbStructureCachedVideo.Column.THUMBNAIL, cachedVideo.getThumbnail());

            database.insert(DbStructureCachedVideo.CACHED_VIDEO, null, values);
        } finally {
            close();
        }
    }

    public void deleteDownloadEntityByDownloadId(long downloadId) {
        try {
            open();
            database.delete(DbStructureSharedDownloads.SHARED_DOWNLOADS,
                    "\"" + DbStructureSharedDownloads.Column.DOWNLOAD_ID + "\"" + " = " + downloadId,
                    null);
        } finally {
            close();
        }
    }

    public boolean isExistDownloadEntityByVideoId(long videoId) {
        try {
            open();
            String Query = "Select * from " + DbStructureSharedDownloads.SHARED_DOWNLOADS + " where " + DbStructureSharedDownloads.Column.VIDEO_ID + " = " + videoId;
            Cursor cursor = database.rawQuery(Query, null);
            if (cursor.getCount() <= 0) {
                cursor.close();
                return false;
            }
            cursor.close();
            return true;
        } finally {
            close();
        }

    }

    public void deleteVideo(Video video) {
        try {
            open();
            long videoId = video.getId();
            database.delete(DbStructureCachedVideo.CACHED_VIDEO,
                    "\"" + DbStructureCachedVideo.Column.VIDEO_ID + "\"" + " = " + videoId,
                    null);
        } finally {
            close();
        }
    }

    public void deleteVideoByUrl(String path) {
        try {
            open();
            database.delete(DbStructureCachedVideo.CACHED_VIDEO,
                    DbStructureCachedVideo.Column.URL + " = " + "\"" + path + "\"",
                    null);
        } finally {
            close();
        }
    }

    public void deleteUnit(Unit unit) {
        try {
            open();
            long unitId = unit.getId();
            database.delete(DbStructureUnit.UNITS,
                    "\"" + DbStructureUnit.Column.UNIT_ID + "\"" + " = " + unitId,
                    null);
        } finally {
            close();
        }
    }

    public void deleteStep(Step step) {
        try {
            open();
            long stepId = step.getId();
            database.delete(DbStructureStep.STEPS,
                    "\"" + DbStructureStep.Column.STEP_ID + "\"" + " = " + stepId,
                    null);
        } finally {
            close();
        }
    }

    public void deleteLesson(Lesson lesson) {
        try {
            open();
            long lessonId = lesson.getId();
            database.delete(DbStructureLesson.LESSONS,
                    "\"" + DbStructureLesson.Column.LESSON_ID + "\"" + " = " + lessonId,
                    null);
        } finally {
            close();
        }
    }


    public List<String> getPathsForAllCachedVideo() {
        try {
            open();
            List<String> cachedPaths = new ArrayList<>();

            Cursor cursor = getCachedVideosCursor();
            cursor.moveToFirst();

            while (!cursor.isAfterLast()) {
                CachedVideo cachedVideo = parseCachedVideo(cursor);
                cachedPaths.add(cachedVideo.getUrl());
                cursor.moveToNext();
            }

            cursor.close();
            return cachedPaths;
        } finally {
            close();
        }
    }

    /**
     * getPath of cached video
     *
     * @param video video which we check for contains in db
     * @return null if video not existing in db, otherwise path to disk
     */
    public String getPathToVideoIfExist(@NotNull Video video) {
        try {
            open();
            String Query = "Select * from " + DbStructureCachedVideo.CACHED_VIDEO + " where " + DbStructureCachedVideo.Column.VIDEO_ID + " = " + video.getId();
            Cursor cursor = database.rawQuery(Query, null);
            if (cursor.getCount() <= 0) {
                cursor.close();
                return null;
            }
            cursor.moveToFirst();
            int columnNumberOfPath = cursor.getColumnIndex(DbStructureCachedVideo.Column.URL);
            String path = cursor.getString(columnNumberOfPath);
            cursor.close();
            return path;

        } finally {
            close();
        }
    }


    public DownloadEntity getDownloadEntityIfExist(Long downloadId) {
        try {
            open();
            String Query = "Select * from " + DbStructureSharedDownloads.SHARED_DOWNLOADS + " where " + DbStructureSharedDownloads.Column.DOWNLOAD_ID + " = " + downloadId;
            Cursor cursor = database.rawQuery(Query, null);
            if (cursor.getCount() <= 0) {
                cursor.close();
                return null;
            }
            cursor.moveToFirst();
            DownloadEntity downloadEntity = parseDownloadEntity(cursor);
            cursor.close();
            return downloadEntity;

        } finally {
            close();
        }
    }

    public void clearCacheCourses(DatabaseManager.Table type) {
        List<Course> courses = getAllCourses(type);

        for (Course courseItem : courses) {
            deleteCourse(courseItem, type);
        }
    }

    public void addUnit(Unit unit) {
        try {
            open();

            ContentValues values = new ContentValues();

            values.put(DbStructureUnit.Column.UNIT_ID, unit.getId());
            values.put(DbStructureUnit.Column.SECTION, unit.getSection());
            values.put(DbStructureUnit.Column.LESSON, unit.getLesson());
            values.put(DbStructureUnit.Column.ASSIGNMENTS, DbParseHelper.parseLongArrayToString(unit.getAssignments()));
            values.put(DbStructureUnit.Column.POSITION, unit.getPosition());
            values.put(DbStructureUnit.Column.PROGRESS, unit.getProgress());
            values.put(DbStructureUnit.Column.BEGIN_DATE, unit.getBegin_date());
            values.put(DbStructureUnit.Column.END_DATE, unit.getEnd_date());
            values.put(DbStructureUnit.Column.SOFT_DEADLINE, unit.getSoft_deadline());
            values.put(DbStructureUnit.Column.HARD_DEADLINE, unit.getHard_deadline());
            values.put(DbStructureUnit.Column.GRADING_POLICY, unit.getGrading_policy());
            values.put(DbStructureUnit.Column.BEGIN_DATE_SOURCE, unit.getBegin_date_source());
            values.put(DbStructureUnit.Column.END_DATE_SOURCE, unit.getEnd_date_source());
            values.put(DbStructureUnit.Column.SOFT_DEADLINE_SOURCE, unit.getSoft_deadline_source());
            values.put(DbStructureUnit.Column.HARD_DEADLINE_SOURCE, unit.getHard_deadline_source());
            values.put(DbStructureUnit.Column.GRADING_POLICY_SOURCE, unit.getGrading_policy_source());
            values.put(DbStructureUnit.Column.IS_ACTIVE, unit.is_active());
            values.put(DbStructureUnit.Column.CREATE_DATE, unit.getCreate_date());
            values.put(DbStructureUnit.Column.UPDATE_DATE, unit.getUpdate_date());
//            values.put(DbStructureUnit.Column.IS_CACHED, unit.is_cached());
//            values.put(DbStructureUnit.Column.IS_LOADING, unit.is_loading());


            if (isUnitInDb(unit)) {
                database.update(DbStructureUnit.UNITS, values, DbStructureUnit.Column.UNIT_ID + "=" + unit.getId(), null);
            } else {
                database.insert(DbStructureUnit.UNITS, null, values);
            }


        } finally {
            close();
        }

    }

    public void addDownloadEntity(DownloadEntity downloadEntity) {
        try {
            open();
            if (isDownloadEntityInDb(downloadEntity)) return;

            ContentValues values = new ContentValues();
            values.put(DbStructureSharedDownloads.Column.DOWNLOAD_ID, downloadEntity.getDownloadId());
            values.put(DbStructureSharedDownloads.Column.VIDEO_ID, downloadEntity.getVideoId());
            values.put(DbStructureSharedDownloads.Column.STEP_ID, downloadEntity.getStepId());
            values.put(DbStructureSharedDownloads.Column.THUMBNAIL, downloadEntity.getThumbnail());
            database.insert(DbStructureSharedDownloads.SHARED_DOWNLOADS, null, values);

        } finally {
            close();
        }
    }


    public void addLesson(Lesson lesson) {
        try {
            open();

            ContentValues values = new ContentValues();

            values.put(DbStructureLesson.Column.LESSON_ID, lesson.getId());
            values.put(DbStructureLesson.Column.STEPS, DbParseHelper.parseLongArrayToString(lesson.getSteps()));
            values.put(DbStructureLesson.Column.IS_FEATURED, lesson.is_featured());
            values.put(DbStructureLesson.Column.IS_PRIME, lesson.is_prime());
            values.put(DbStructureLesson.Column.PROGRESS, lesson.getProgress());
            values.put(DbStructureLesson.Column.OWNER, lesson.getOwner());
            values.put(DbStructureLesson.Column.SUBSCRIPTIONS, DbParseHelper.parseStringArrayToString(lesson.getSubscriptions()));
            values.put(DbStructureLesson.Column.VIEWED_BY, lesson.getViewed_by());
            values.put(DbStructureLesson.Column.PASSED_BY, lesson.getPassed_by());
            values.put(DbStructureLesson.Column.DEPENDENCIES, DbParseHelper.parseStringArrayToString(lesson.getDependencies()));
            values.put(DbStructureLesson.Column.IS_PUBLIC, lesson.is_public());
            values.put(DbStructureLesson.Column.TITLE, lesson.getTitle());
            values.put(DbStructureLesson.Column.SLUG, lesson.getSlug());
            values.put(DbStructureLesson.Column.CREATE_DATE, lesson.getCreate_date());
            values.put(DbStructureLesson.Column.LEARNERS_GROUP, lesson.getLearners_group());
            values.put(DbStructureLesson.Column.TEACHER_GROUP, lesson.getTeacher_group());
//            values.put(DbStructureLesson.Column.IS_CACHED, lesson.is_cached());
//            values.put(DbStructureLesson.Column.IS_LOADING, lesson.is_loading());

            if (isLessonInDb(lesson)) {
                database.update(DbStructureLesson.LESSONS, values, DbStructureLesson.Column.LESSON_ID + "=" + lesson.getId(), null);

            } else {
                database.insert(DbStructureLesson.LESSONS, null, values);
            }
        } finally {
            close();
        }
    }


    private Cursor getLessonCursor() {
        return database.query(DbStructureLesson.LESSONS, DbStructureLesson.getUsedColumns(),
                null, null, null, null, null);
    }

    private Lesson parseLesson(Cursor cursor) {
        Lesson lesson = new Lesson();
        int columnIndexLessonId = cursor.getColumnIndex(DbStructureLesson.Column.LESSON_ID);
        int columnIndexSteps = cursor.getColumnIndex(DbStructureLesson.Column.STEPS);
        int columnIndexIsFeatured = cursor.getColumnIndex(DbStructureLesson.Column.IS_FEATURED);
        int columnIndexIsPrime = cursor.getColumnIndex(DbStructureLesson.Column.IS_PRIME);
        int columnIndexProgress = cursor.getColumnIndex(DbStructureLesson.Column.PROGRESS);
        int columnIndexOwner = cursor.getColumnIndex(DbStructureLesson.Column.OWNER);
        int columnIndexSubscriptions = cursor.getColumnIndex(DbStructureLesson.Column.SUBSCRIPTIONS);
        int columnIndexViewedBy = cursor.getColumnIndex(DbStructureLesson.Column.VIEWED_BY);
        int columnIndexPassedBy = cursor.getColumnIndex(DbStructureLesson.Column.PASSED_BY);
        int columnIndexDependencies = cursor.getColumnIndex(DbStructureLesson.Column.DEPENDENCIES);
        int columnIndexIsPublic = cursor.getColumnIndex(DbStructureLesson.Column.IS_PUBLIC);
        int columnIndexTitle = cursor.getColumnIndex(DbStructureLesson.Column.TITLE);
        int columnIndexSlug = cursor.getColumnIndex(DbStructureLesson.Column.SLUG);
        int columnIndexCreateDate = cursor.getColumnIndex(DbStructureLesson.Column.CREATE_DATE);
        int columnIndexLearnersGroup = cursor.getColumnIndex(DbStructureLesson.Column.LEARNERS_GROUP);
        int columnIndexTeacherGroup = cursor.getColumnIndex(DbStructureLesson.Column.TEACHER_GROUP);
        int indexIsCached = cursor.getColumnIndex(DbStructureLesson.Column.IS_CACHED);
        int indexIsLoading = cursor.getColumnIndex(DbStructureLesson.Column.IS_LOADING);

        lesson.setId(cursor.getLong(columnIndexLessonId));
        lesson.setSteps(DbParseHelper.parseStringToLongArray(cursor.getString(columnIndexSteps)));
        lesson.setIs_featured(cursor.getInt(columnIndexIsFeatured) > 0);
        lesson.setIs_prime(cursor.getInt(columnIndexIsPrime) > 0);
        lesson.setProgress(cursor.getString(columnIndexProgress));
        lesson.setOwner(cursor.getInt(columnIndexOwner));
        lesson.setSubscriptions(DbParseHelper.parseStringToStringArray(cursor.getString(columnIndexSubscriptions)));
        lesson.setViewed_by(cursor.getInt(columnIndexViewedBy));
        lesson.setPassed_by(cursor.getInt(columnIndexPassedBy));
        lesson.setDependencies(DbParseHelper.parseStringToStringArray(cursor.getString(columnIndexDependencies)));
        lesson.setIs_public(cursor.getInt(columnIndexIsPublic) > 0);
        lesson.setTitle(cursor.getString(columnIndexTitle));
        lesson.setSlug(cursor.getString(columnIndexSlug));
        lesson.setCreate_date(cursor.getString(columnIndexCreateDate));
        lesson.setLearners_group(cursor.getString(columnIndexLearnersGroup));
        lesson.setTeacher_group(cursor.getString(columnIndexTeacherGroup));
        lesson.setIs_cached(cursor.getInt(indexIsCached) > 0);
        lesson.setIs_loading(cursor.getInt(indexIsLoading) > 0);

        return lesson;
    }

    private Unit parseUnit(Cursor cursor) {
        Unit unit = new Unit();

        int columnIndexUnitId = cursor.getColumnIndex(DbStructureUnit.Column.UNIT_ID);
        int columnIndexSection = cursor.getColumnIndex(DbStructureUnit.Column.SECTION);
        int columnIndexLesson = cursor.getColumnIndex(DbStructureUnit.Column.LESSON);
        int columnIndexAssignments = cursor.getColumnIndex(DbStructureUnit.Column.ASSIGNMENTS);
        int columnIndexPosition = cursor.getColumnIndex(DbStructureUnit.Column.POSITION);
        int columnIndexProgress = cursor.getColumnIndex(DbStructureUnit.Column.PROGRESS);
        int columnIndexBeginDate = cursor.getColumnIndex(DbStructureUnit.Column.BEGIN_DATE);
        int columnIndexSoftDeadline = cursor.getColumnIndex(DbStructureUnit.Column.SOFT_DEADLINE);
        int columnIndexHardDeadline = cursor.getColumnIndex(DbStructureUnit.Column.HARD_DEADLINE);
        int columnIndexIsActive = cursor.getColumnIndex(DbStructureUnit.Column.IS_ACTIVE);
        int indexIsCached = cursor.getColumnIndex(DbStructureUnit.Column.IS_CACHED);
        int indexIsLoading = cursor.getColumnIndex(DbStructureUnit.Column.IS_LOADING);


        unit.setId(cursor.getLong(columnIndexUnitId));
        unit.setSection(cursor.getLong(columnIndexSection));
        unit.setLesson(cursor.getLong(columnIndexLesson));
        unit.setProgress(cursor.getString(columnIndexProgress));
        unit.setAssignments(DbParseHelper.parseStringToLongArray(cursor.getString(columnIndexAssignments)));
        unit.setBegin_date(cursor.getString(columnIndexBeginDate));
        unit.setSoft_deadline(cursor.getString(columnIndexSoftDeadline));
        unit.setHard_deadline(cursor.getString(columnIndexHardDeadline));
        unit.setPosition(cursor.getInt(columnIndexPosition));
        unit.setIs_active(cursor.getInt(columnIndexIsActive) > 0);
        unit.setIs_cached(cursor.getInt(indexIsCached) > 0);
        unit.setIs_loading(cursor.getInt(indexIsLoading) > 0);

        boolean is_viewed = progressIsViewed(unit.getProgress());
        unit.setIs_viewed_custom(is_viewed);

        return unit;

    }

    private DownloadEntity parseDownloadEntity(Cursor cursor) {
        DownloadEntity downloadEntity = new DownloadEntity();

        int indexDownloadId = cursor.getColumnIndex(DbStructureSharedDownloads.Column.DOWNLOAD_ID);
        int indexStepId = cursor.getColumnIndex(DbStructureSharedDownloads.Column.STEP_ID);
        int indexVideoId = cursor.getColumnIndex(DbStructureSharedDownloads.Column.VIDEO_ID);
        int indexThumbnail = cursor.getColumnIndex(DbStructureSharedDownloads.Column.THUMBNAIL);

        downloadEntity.setDownloadId(cursor.getLong(indexDownloadId));
        downloadEntity.setStepId(cursor.getLong(indexStepId));
        downloadEntity.setVideoId(cursor.getLong(indexVideoId));
        downloadEntity.setThumbnail(cursor.getString(indexThumbnail));

        return downloadEntity;
    }


    private Cursor getUnitCursor() {
        return database.query(DbStructureUnit.UNITS, DbStructureUnit.getUsedColumns(),
                null, null, null, null, null);
    }

    private CachedVideo parseCachedVideo(Cursor cursor) {
        CachedVideo cachedVideo = new CachedVideo();
        int indexStepId = cursor.getColumnIndex(DbStructureCachedVideo.Column.STEP_ID);
        int indexVideoId = cursor.getColumnIndex(DbStructureCachedVideo.Column.VIDEO_ID);
        int indexUrl = cursor.getColumnIndex(DbStructureCachedVideo.Column.URL);
        int indexThumbnail = cursor.getColumnIndex(DbStructureCachedVideo.Column.THUMBNAIL);

        cachedVideo.setVideoId(cursor.getLong(indexVideoId));
        cachedVideo.setUrl(cursor.getString(indexUrl));
        cachedVideo.setThumbnail(cursor.getString(indexThumbnail));
        cachedVideo.setStepId(cursor.getLong(indexStepId));
        return cachedVideo;
    }


    private boolean isDownloadEntityInDb(DownloadEntity downloadEntity) {
        String Query = "Select * from " + DbStructureSharedDownloads.SHARED_DOWNLOADS + " where " + DbStructureSharedDownloads.Column.DOWNLOAD_ID + " = " + downloadEntity.getDownloadId();
        Cursor cursor = database.rawQuery(Query, null);
        if (cursor.getCount() <= 0) {
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }

    private boolean isVideoInDb(Video video) {
        String Query = "Select * from " + DbStructureCachedVideo.CACHED_VIDEO + " where " + DbStructureCachedVideo.Column.VIDEO_ID + " = " + video.getId();
        Cursor cursor = database.rawQuery(Query, null);
        if (cursor.getCount() <= 0) {
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }

    private boolean isLessonInDb(Lesson lesson) {
        String Query = "Select * from " + DbStructureLesson.LESSONS + " where " + DbStructureLesson.Column.LESSON_ID + " = " + lesson.getId();
        Cursor cursor = database.rawQuery(Query, null);
        if (cursor.getCount() <= 0) {
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }

    private boolean isUnitInDb(Unit unit) {
        String Query = "Select * from " + DbStructureUnit.UNITS + " where " + DbStructureUnit.Column.UNIT_ID + " = " + unit.getId();
        Cursor cursor = database.rawQuery(Query, null);
        if (cursor.getCount() <= 0) {
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }


    private Cursor getCachedVideosCursor() {
        return database.query(DbStructureCachedVideo.CACHED_VIDEO, DbStructureCachedVideo.getUsedColumns(),
                null, null, null, null, null);
    }

    private Cursor getSectionCursor() {
        return database.query(DbStructureSections.SECTIONS, DbStructureSections.getUsedColumns(),
                null, null, null, null, null);
    }

    private Section parseSection(Cursor cursor) {
        Section section = new Section();

        int columnIndexId = cursor.getColumnIndex(DbStructureSections.Column.SECTION_ID);
        int columnIndexTitle = cursor.getColumnIndex(DbStructureSections.Column.TITLE);
        int columnIndexSlug = cursor.getColumnIndex(DbStructureSections.Column.SLUG);
        int columnIndexIsActive = cursor.getColumnIndex(DbStructureSections.Column.IS_ACTIVE);
        int columnIndexBeginDate = cursor.getColumnIndex(DbStructureSections.Column.BEGIN_DATE);
        int columnIndexSoftDeadline = cursor.getColumnIndex(DbStructureSections.Column.SOFT_DEADLINE);
        int columnIndexHardDeadline = cursor.getColumnIndex(DbStructureSections.Column.HARD_DEADLINE);
        int columnIndexCourseId = cursor.getColumnIndex(DbStructureSections.Column.COURSE);
        int columnIndexPosition = cursor.getColumnIndex(DbStructureSections.Column.POSITION);
        int columnIndexUnits = cursor.getColumnIndex(DbStructureSections.Column.UNITS);
        int indexIsCached = cursor.getColumnIndex(DbStructureSections.Column.IS_CACHED);
        int indexIsLoading = cursor.getColumnIndex(DbStructureSections.Column.IS_LOADING);

        section.setId(cursor.getLong(columnIndexId));
        section.setTitle(cursor.getString(columnIndexTitle));
        section.setSlug(cursor.getString(columnIndexSlug));
        section.setIs_active(cursor.getInt(columnIndexIsActive) > 0);
        section.setBegin_date(cursor.getString(columnIndexBeginDate));
        section.setSoft_deadline(cursor.getString(columnIndexSoftDeadline));
        section.setHard_deadline(cursor.getString(columnIndexHardDeadline));
        section.setCourse(cursor.getLong(columnIndexCourseId));
        section.setPosition(cursor.getInt(columnIndexPosition));
        section.setIs_cached(cursor.getInt(indexIsCached) > 0);
        section.setIs_loading(cursor.getInt(indexIsLoading) > 0);
        section.setUnits(DbParseHelper.parseStringToLongArray(cursor.getString(columnIndexUnits)));

        return section;
    }


    private Course parseCourse(Cursor cursor) {
        Course course = new Course();

        int indexId = cursor.getColumnIndex(DBStructureCourses.Column.COURSE_ID);
        int indexSummary = cursor.getColumnIndex(DBStructureCourses.Column.SUMMARY);
        int indexCover = cursor.getColumnIndex(DBStructureCourses.Column.COVER_LINK);
        int indexIntro = cursor.getColumnIndex(DBStructureCourses.Column.INTRO_LINK_VIMEO);
        int indexTitle = cursor.getColumnIndex(DBStructureCourses.Column.TITLE);
        int indexLanguage = cursor.getColumnIndex(DBStructureCourses.Column.LANGUAGE);
        int indexBeginDateSource = cursor.getColumnIndex(DBStructureCourses.Column.BEGIN_DATE_SOURCE);
        int indexLastDeadline = cursor.getColumnIndex(DBStructureCourses.Column.LAST_DEADLINE);
        int indexDescription = cursor.getColumnIndex(DBStructureCourses.Column.DESCRIPTION);
        int indexInstructors = cursor.getColumnIndex(DBStructureCourses.Column.INSTRUCTORS);
        int indexRequirements = cursor.getColumnIndex(DBStructureCourses.Column.REQUIREMENTS);
        int indexEnrollment = cursor.getColumnIndex(DBStructureCourses.Column.ENROLLMENT);
        int indexSection = cursor.getColumnIndex(DBStructureCourses.Column.SECTIONS);
        int indexIsCached = cursor.getColumnIndex(DBStructureCourses.Column.IS_CACHED);
        int indexIsLoading = cursor.getColumnIndex(DBStructureCourses.Column.IS_LOADING);

        course.setId(cursor.getLong(indexId));
        course.setSummary(cursor.getString(indexSummary));
        course.setCover(cursor.getString(indexCover));
        course.setIntro(cursor.getString(indexIntro));
        course.setTitle(cursor.getString(indexTitle));
        course.setLanguage(cursor.getString(indexLanguage));
        course.setBegin_date_source(cursor.getString(indexBeginDateSource));
        course.setLast_deadline(cursor.getString(indexLastDeadline));
        course.setDescription(cursor.getString(indexDescription));
        course.setInstructors(DbParseHelper.parseStringToLongArray(cursor.getString(indexInstructors)));
        course.setRequirements(cursor.getString(indexRequirements));
        course.setEnrollment(cursor.getInt(indexEnrollment));
        course.setIs_cached(cursor.getInt(indexIsCached) > 0);
        course.setIs_loading(cursor.getInt(indexIsLoading) > 0);
        course.setSections(DbParseHelper.parseStringToLongArray(cursor.getString(indexSection)));

        return course;
    }

    private Step parseStep(Cursor cursor) {
        Step step = new Step();

        int columnIndexCreateDate = cursor.getColumnIndex(DbStructureStep.Column.CREATE_DATE);
        int columnIndexStepId = cursor.getColumnIndex(DbStructureStep.Column.STEP_ID);
        int columnIndexLessonId = cursor.getColumnIndex(DbStructureStep.Column.LESSON_ID);
        int columnIndexStatus = cursor.getColumnIndex(DbStructureStep.Column.STATUS);
        int columnIndexProgress = cursor.getColumnIndex(DbStructureStep.Column.PROGRESS);
        int columnIndexViewedBy = cursor.getColumnIndex(DbStructureStep.Column.VIEWED_BY);
        int columnIndexPassedBy = cursor.getColumnIndex(DbStructureStep.Column.PASSED_BY);
        int columnIndexUpdateDate = cursor.getColumnIndex(DbStructureStep.Column.UPDATE_DATE);
        int columnIndexSubscriptions = cursor.getColumnIndex(DbStructureStep.Column.SUBSCRIPTIONS);
        int columnIndexPosition = cursor.getColumnIndex(DbStructureStep.Column.POSITION);
        int columnIndexIsCached = cursor.getColumnIndex(DbStructureStep.Column.IS_CACHED);
        int columnIndexIsLoading = cursor.getColumnIndex(DbStructureStep.Column.IS_LOADING);
//        int columnIndexIsCustomViewed = cursor.getColumnIndex(DbStructureStep.Column.IS_CUSTOM_VIEWED);

        step.setId(cursor.getLong(columnIndexStepId));
        step.setLesson(cursor.getLong(columnIndexLessonId));
        step.setCreate_date(cursor.getString(columnIndexCreateDate));
        step.setCreate_date(cursor.getString(columnIndexStatus));
        step.setProgress(cursor.getString(columnIndexProgress));
        step.setViewed_by(cursor.getLong(columnIndexViewedBy));
        step.setPassed_by(cursor.getLong(columnIndexPassedBy));
        step.setUpdate_date(cursor.getString(columnIndexUpdateDate));
        step.setSubscriptions(DbParseHelper.parseStringToStringArray(cursor.getString(columnIndexSubscriptions)));
        step.setPosition(cursor.getLong(columnIndexPosition));
        step.setIs_cached(cursor.getInt(columnIndexIsCached) > 0);
        step.setIs_loading(cursor.getInt(columnIndexIsLoading) > 0);
//        step.setIs_custom_passed(cursor.getInt(columnIndexIsCustomViewed) > 0);
        step.setIs_custom_passed(isAssignmentByStepViewed(step.getId()));


        String Query = "Select * from " + DbStructureBlock.BLOCKS + " where " + DbStructureBlock.Column.STEP_ID + " = " + step.getId();
        Cursor blockCursor = database.rawQuery(Query, null);
        blockCursor.moveToFirst();

        if (!blockCursor.isAfterLast()) {
            step.setBlock(parseBlock(blockCursor, step));
        }
        blockCursor.close();
        return step;
    }

    private Block parseBlock(Cursor cursor, Step step) {
        Block block = new Block();

        int indexName = cursor.getColumnIndex(DbStructureBlock.Column.NAME);
        int indexText = cursor.getColumnIndex(DbStructureBlock.Column.TEXT);

        block.setName(cursor.getString(indexName));
        block.setText(cursor.getString(indexText));


        String Query = "Select * from " + DbStructureCachedVideo.CACHED_VIDEO + " where " + DbStructureCachedVideo.Column.STEP_ID + " = " + step.getId();
        Cursor videoCursor = database.rawQuery(Query, null);
        videoCursor.moveToFirst();

        if (!videoCursor.isAfterLast()) {
            block.setVideo(parseRealVideo(videoCursor));
        }
        videoCursor.close();


        return block;
    }

    private Video parseRealVideo(Cursor cursor) {
        Video video = new Video();

        int indexVideoId = cursor.getColumnIndex(DbStructureCachedVideo.Column.VIDEO_ID);
        int indexThumbnail = cursor.getColumnIndex(DbStructureCachedVideo.Column.THUMBNAIL);


        video.setThumbnail(cursor.getString(indexThumbnail));
        video.setId(cursor.getLong(indexVideoId));

        return video;
    }


    private Cursor getCourseCursor(DatabaseManager.Table type) {
        return database.query(type.getStoreName(), DBStructureCourses.getUsedColumns(),
                null, null, null, null, null);

    }

    private Cursor getDownloadEntitiesCursor() {
        return database.query(DbStructureSharedDownloads.SHARED_DOWNLOADS,
                DbStructureSharedDownloads.getUsedColumns(), null, null, null, null, null);
    }

    public void addToQueueViewedState(ViewAssignment viewState) {
        try {
            open();
            ContentValues values = new ContentValues();

            values.put(DbStructureViewQueue.Column.ASSIGNMENT_ID, viewState.getAssignment());
            values.put(DbStructureViewQueue.Column.STEP_ID, viewState.getStep());
            if (!isViewStateInDb(viewState.getAssignment())) {
                database.insert(DbStructureViewQueue.VIEW_QUEUE, null, values);
            }
        } finally {
            close();
        }
    }

    private ViewAssignment parseViewAssignmentWrapper(Cursor cursor) {
        int indexStepId = cursor.getColumnIndex(DbStructureViewQueue.Column.STEP_ID);
        int indexAssignmentId = cursor.getColumnIndex(DbStructureViewQueue.Column.ASSIGNMENT_ID);


        long stepId = cursor.getLong(indexStepId);
        long assignmentId = cursor.getLong(indexAssignmentId);

        return new ViewAssignment(assignmentId, stepId);
    }

    public List<ViewAssignment> getAllInQueue() {
        try {
            open();
            List<ViewAssignment> queue = new ArrayList<>();
            Cursor cursor = getQueue();

            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                ViewAssignment viewState = parseViewAssignmentWrapper(cursor);
                queue.add(viewState);
                cursor.moveToNext();
            }
            cursor.close();
            return queue;

        } finally {
            close();
        }
    }

    public void removeFromQueue(ViewAssignment viewAssignmentWrapper) {
        try {
            open();

            long assignmentId = viewAssignmentWrapper.getAssignment();
            database.delete(DbStructureViewQueue.VIEW_QUEUE,
                    DbStructureViewQueue.Column.ASSIGNMENT_ID + " = " + assignmentId,
                    null);

        } finally {
            close();
        }
    }


    private Cursor getQueue() {
        return database.query(DbStructureViewQueue.VIEW_QUEUE,
                DbStructureViewQueue.getUsedColumns(), null, null, null, null, null);
    }


    public void markProgressAsPassed(long assignmentId) {
        try {
            open();

            String Query = "Select * from " + DbStructureAssignment.ASSIGNMENTS + " where " + DbStructureAssignment.Column.ASSIGNMENT_ID + " = " + assignmentId;
            Cursor cursor = database.rawQuery(Query, null);

            cursor.moveToFirst();

            if (!cursor.isAfterLast()) {
                Assignment assignment = parseAssignment(cursor);
                cursor.close();
                String progressId = assignment.getProgress();

                if (isProgressInDb(progressId)) {
                    ContentValues values = new ContentValues();
                    values.put(DbStructureProgress.Column.IS_PASSED, true);
                    database.update(DbStructureProgress.PROGRESS, values, DbStructureProgress.Column.ID + "=?", new String[]{progressId});
                }

            }
            cursor.close();
        } finally {
            close();
        }
    }


    private Assignment parseAssignment(Cursor cursor) {
        Assignment assignment = new Assignment();

        int columnIndexAssignmentId = cursor.getColumnIndex(DbStructureAssignment.Column.ASSIGNMENT_ID);
        int columnIndexCreateDate = cursor.getColumnIndex(DbStructureAssignment.Column.CREATE_DATE);
        int columnIndexProgress = cursor.getColumnIndex(DbStructureAssignment.Column.PROGRESS);
        int columnIndexStepId = cursor.getColumnIndex(DbStructureAssignment.Column.STEP_ID);
        int columnIndexUnitId = cursor.getColumnIndex(DbStructureAssignment.Column.UNIT_ID);
        int columnIndexUpdateDate = cursor.getColumnIndex(DbStructureAssignment.Column.UPDATE_DATE);

        assignment.setCreate_date(cursor.getString(columnIndexCreateDate));
        assignment.setId(cursor.getLong(columnIndexAssignmentId));
        assignment.setProgress(cursor.getString(columnIndexProgress));
        assignment.setStep(cursor.getLong(columnIndexStepId));
        assignment.setUnit(cursor.getLong(columnIndexUnitId));
        assignment.setUpdate_date(cursor.getString(columnIndexUpdateDate));
        return assignment;
    }

    public void addProgress(Progress progress) {
        try {
            open();
            ContentValues values = new ContentValues();

            values.put(DbStructureProgress.Column.ID, progress.getId());
            values.put(DbStructureProgress.Column.COST, progress.getCost());
            values.put(DbStructureProgress.Column.SCORE, progress.getScore());
            values.put(DbStructureProgress.Column.IS_PASSED, progress.is_passed());
            values.put(DbStructureProgress.Column.LAST_VIEWED, progress.getLast_viewed());
            values.put(DbStructureProgress.Column.N_STEPS, progress.getN_steps());
            values.put(DbStructureProgress.Column.N_STEPS_PASSED, progress.getLast_viewed());


            if (isProgressInDb(progress.getId())) {
                database.update(DbStructureProgress.PROGRESS, values, DbStructureProgress.Column.ID + "=?", new String[]{progress.getId()});
            } else {
                database.insert(DbStructureProgress.PROGRESS, null, values);
            }

        } finally {
            close();
        }
    }

    private boolean isProgressInDb(String progressId) {
        String Query = "Select * from " + DbStructureProgress.PROGRESS + " where " + DbStructureProgress.Column.ID + " =?";
        Cursor cursor = database.rawQuery(Query, new String[]{progressId});
        if (cursor.getCount() <= 0) {
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }

    private boolean isAssignmentInDb(long assignmentId) {
        String Query = "Select * from " + DbStructureAssignment.ASSIGNMENTS + " where " + DbStructureAssignment.Column.ASSIGNMENT_ID + " =?";
        Cursor cursor = database.rawQuery(Query, new String[]{assignmentId + ""});
        if (cursor.getCount() <= 0) {
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }

    private boolean isViewStateInDb(long assignmentId) {
        String Query = "Select * from " + DbStructureViewQueue.VIEW_QUEUE + " where " + DbStructureViewQueue.Column.ASSIGNMENT_ID + " =?";
        Cursor cursor = database.rawQuery(Query, new String[]{assignmentId + ""});
        if (cursor.getCount() <= 0) {
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }

    public boolean isViewedPublicWrapper(String progressId) {
        try {
            open();
            return progressIsViewed(progressId);
        } finally {
            close();
        }
    }

    private boolean progressIsViewed(String progressId) {
        String Query = "Select * from " + DbStructureProgress.PROGRESS + " where " + DbStructureProgress.Column.ID + " =?";
        Cursor cursor = database.rawQuery(Query, new String[]{progressId});

        cursor.moveToFirst();

        if (!cursor.isAfterLast()) {
            boolean progress = cursor.getInt(cursor.getColumnIndex(DbStructureProgress.Column.IS_PASSED)) > 0;
            cursor.close();
            return progress;
        }
        cursor.close();
        return false;
    }

    private boolean isAssignmentByStepViewed(long stepId) {
        String Query = "Select * from " + DbStructureAssignment.ASSIGNMENTS + " where " + DbStructureAssignment.Column.STEP_ID + " =?";
        Cursor cursor = database.rawQuery(Query, new String[]{stepId + ""});

        cursor.moveToFirst();

        if (!cursor.isAfterLast()) {
            String progressId = cursor.getString(cursor.getColumnIndex(DbStructureAssignment.Column.PROGRESS));
            cursor.close();
            return progressIsViewed(progressId);
        }
        cursor.close();
        return false;
    }

    public boolean isStepPassed(long stepId) {
        try {
            open();
            return isAssignmentByStepViewed(stepId);
        } finally {
            close();
        }
    }
}