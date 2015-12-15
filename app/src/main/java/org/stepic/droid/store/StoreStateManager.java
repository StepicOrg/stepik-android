package org.stepic.droid.store;

import android.os.Handler;

import com.squareup.otto.Bus;

import org.stepic.droid.base.MainApplication;
import org.stepic.droid.events.sections.NotCachedSectionEvent;
import org.stepic.droid.events.sections.SectionCachedEvent;
import org.stepic.droid.events.units.NotCachedUnitEvent;
import org.stepic.droid.events.units.UnitCachedEvent;
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
    private Bus bus;

    @Inject
    public StoreStateManager(DatabaseManager dbManager, Bus bus) {
        mDatabaseManager = dbManager;
        this.bus = bus;
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

        final Unit unit = mDatabaseManager.getUnitByLessonId(lessonId);
        unit.setIs_loading(false);
        unit.setIs_cached(true);
        mDatabaseManager.updateOnlyCachedLoadingUnit(unit);


        Handler mainHandler = new Handler(MainApplication.getAppContext().getMainLooper());
        //Say to ui that ui is cached now
        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                bus.post(new UnitCachedEvent(unit.getId()));
            }
        };
        mainHandler.post(myRunnable);

        updateSectionState(unit.getSection());
    }

    private void updateUnitLessonAfterDeleting(Unit unit) {
        //now unit lesson and all steps are deleting
        //cached = false, loading false
        //just make for parents
        //// FIXME: 14.12.15 it is not true, see related commit. Now we can delete one step.


        Section section = mDatabaseManager.getSectionById(unit.getSection());
        updateSectionAfterDeleting(section);

    }

    @Override
    public void updateStepAfterDeleting(Step step) {
        long lessonId = step.getLesson();

        Lesson lesson = mDatabaseManager.getLessonById(lessonId);
        final Unit unit = mDatabaseManager.getUnitByLessonId(lessonId);

        if (lesson.is_cached() || lesson.is_loading()) {
            lesson.setIs_loading(false);
            lesson.setIs_cached(false);
            mDatabaseManager.updateOnlyCachedLoadingLesson(lesson);
        }


        if (unit.is_cached() || unit.is_loading()) {
            unit.setIs_loading(false);
            unit.setIs_cached(false);
            mDatabaseManager.updateOnlyCachedLoadingUnit(unit);

            Handler mainHandler = new Handler(MainApplication.getAppContext().getMainLooper());
            //Say to ui that ui is cached now
            Runnable myRunnable = new Runnable() {
                @Override
                public void run() {
                    bus.post(new NotCachedUnitEvent(unit.getId()));
                }
            };
            mainHandler.post(myRunnable);
        }


        updateUnitLessonAfterDeleting(unit);
    }

    private void updateSectionAfterDeleting(final Section section) {
        section.setIs_cached(false);
        section.setIs_loading(false);
        mDatabaseManager.updateOnlyCachedLoadingSection(section);
        Handler mainHandler = new Handler(MainApplication.getAppContext().getMainLooper());
        //Say to ui that ui is cached now
        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                bus.post(new NotCachedSectionEvent(section.getId()));
            }
        };
        mainHandler.post(myRunnable);


        Course course = mDatabaseManager.getCourseById(section.getCourse(), DatabaseManager.Table.enrolled);
        updateCourseAfterDeleting(course);

    }

    private void updateCourseAfterDeleting(Course course) {
        course.setIs_cached(false);
        course.setIs_loading(false);
        mDatabaseManager.updateOnlyCachedLoadingCourse(course, DatabaseManager.Table.enrolled);
    }

    @Override
    public void updateSectionState(long sectionId) {
        List<Unit> units = mDatabaseManager.getAllUnitsOfSection(sectionId);
        for (Unit unit : units) {
            if (!unit.is_cached()) return;
        }

        //all units, lessons, steps of sections are cached
        final Section section = mDatabaseManager.getSectionById(sectionId);
        section.setIs_cached(true);
        section.setIs_loading(false);
        mDatabaseManager.updateOnlyCachedLoadingSection(section);


        Handler mainHandler = new Handler(MainApplication.getAppContext().getMainLooper());
        //Say to ui that ui is cached now
        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                bus.post(new SectionCachedEvent(section.getId()));
            }
        };
        mainHandler.post(myRunnable);

        updateCourseState(section.getCourse());
    }

    private void updateCourseState(long courseId) {
        Course course = mDatabaseManager.getCourseById(courseId, DatabaseManager.Table.enrolled);
        if (course == null) {
            course = mDatabaseManager.getCourseById(courseId, DatabaseManager.Table.featured);
            mDatabaseManager.addCourse(course, DatabaseManager.Table.enrolled);
        }
        List<Section> sections = mDatabaseManager.getAllSectionsOfCourse(course);
        for (Section section : sections) {
            if (!section.is_cached()) return;
        }

        course.setIs_loading(false);
        course.setIs_cached(true);
        mDatabaseManager.updateOnlyCachedLoadingCourse(course, DatabaseManager.Table.enrolled);
    }
}
