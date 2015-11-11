package org.stepic.droid.store;

import org.stepic.droid.model.Course;
import org.stepic.droid.model.Lesson;
import org.stepic.droid.model.Section;
import org.stepic.droid.model.Step;
import org.stepic.droid.model.Unit;
import org.stepic.droid.store.operations.DatabaseManager;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class StoreStateManager implements IStoreStateManager {

    private DatabaseManager mDatabaseManager;

    @Inject
    public StoreStateManager(DatabaseManager dbManager) {
        mDatabaseManager = dbManager;
    }

    @Override
    public void updateUnitLessonState(long lessonId) {
        List<Step> steps = mDatabaseManager.getStepsOfLesson(lessonId);
        for (Step step : steps) {
            if (!step.is_cached()) return;
        }

        //all steps of lesson is cached
        Lesson lesson = mDatabaseManager.getLessonById(lessonId);
        lesson.setIs_loading(false);
        lesson.setIs_cached(true);
        mDatabaseManager.updateOnlyCachedLoadingLesson(lesson);

        Unit unit = mDatabaseManager.getUnitByLessonId(lessonId);
        unit.setIs_loading(false);
        unit.setIs_cached(true);
        mDatabaseManager.updateOnlyCachedLoadingUnit(unit);

        updateSectionState(unit.getSection());
    }

    private void updateSectionState(long sectionId) {
        List<Unit> units = mDatabaseManager.getAllUnitsOfSection(sectionId);
        for (Unit unit : units) {
            if (!unit.is_cached()) return;
        }

        //all units, lessons, steps of sections are cached
        Section section = mDatabaseManager.getSectionById(sectionId);
        section.setIs_cached(true);
        section.setIs_loading(false);
        mDatabaseManager.updateOnlyCachedLoadingSection(section);

        updateCourseState(section.getCourse());
    }

    private void updateCourseState(long courseId) {
        Course course = mDatabaseManager.getCourseById(courseId, DatabaseManager.Table.enrolled);
        List<Section> sections = mDatabaseManager.getAllSectionsOfCourse(course);
        for (Section section : sections) {
            if (!section.is_cached()) return;
        }

        course.setIs_loading(false);
        course.setIs_cached(true);
        mDatabaseManager.updateOnlyCachedLoadingCourse(course, DatabaseManager.Table.enrolled);
    }
}
