package org.stepic.droid.store;

import android.os.Handler;

import com.squareup.otto.Bus;
import com.yandex.metrica.YandexMetrica;

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
import org.stepic.droid.util.AppConstants;

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
        if (unit.is_loading() || !unit.is_cached()) {
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
        }


        updateSectionState(unit.getSection());
    }

    @Override
    public void updateUnitLessonAfterDeleting(long lessonId) {
        //now unit lesson and all steps are deleting
        //cached = false, loading false
        //just make for parents
        //// FIXME: 14.12.15 it is not true, see related commit. Now we can delete one step.

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

        updateSectionAfterDeleting(unit.getSection());
    }

    @Override
    public void updateStepAfterDeleting(Step step) {
        //// TODO: 17.12.15 transfer to this method all update state? it is not good for UI

        long lessonId = step.getLesson();
        updateUnitLessonAfterDeleting(lessonId);
    }

    @Override
    public void updateSectionAfterDeleting(long sectionId) {
        final Section section = mDatabaseManager.getSectionById(sectionId);
        if (section == null) {
            YandexMetrica.reportError(AppConstants.NULL_SECTION, new Exception("update Section after deleting"));
            return;
        }
        if (section.is_cached() || section.is_loading()) {
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
        }

        updateCourseAfterDeleting(section.getCourse());
    }

    private void updateCourseAfterDeleting(long courseId) {

        Course course = mDatabaseManager.getCourseById(courseId, DatabaseManager.Table.enrolled);
        if (course == null) {
            YandexMetrica.reportError(AppConstants.NULL_COURSE, new Exception("update Course after deleting"));
            return;
        }
        if (course.is_cached() || course.is_loading()) {
            course.setIs_cached(false);
            course.setIs_loading(false);
            mDatabaseManager.updateOnlyCachedLoadingCourse(course, DatabaseManager.Table.enrolled);
        }
    }

    @Override
    public void updateSectionState(long sectionId) {
        List<Unit> units = mDatabaseManager.getAllUnitsOfSection(sectionId);
        for (Unit unit : units) {
            if (!unit.is_cached()) return;
        }

        //all units, lessons, steps of sections are cached
        final Section section = mDatabaseManager.getSectionById(sectionId);
        if (section == null) {
            YandexMetrica.reportError(AppConstants.NULL_SECTION, new Exception("update section state"));
            return;
        }
        if (!section.is_cached() || section.is_loading()) {
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
        }

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

        if (course == null) {
            YandexMetrica.reportError(AppConstants.NULL_COURSE, new Exception("null course in update course state"));
            return;
        }

        course.setIs_loading(false);
        course.setIs_cached(true);
        mDatabaseManager.updateOnlyCachedLoadingCourse(course, DatabaseManager.Table.enrolled);
    }
}
