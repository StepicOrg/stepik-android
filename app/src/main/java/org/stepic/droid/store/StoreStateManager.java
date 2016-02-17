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
import org.stepic.droid.store.operations.DatabaseFacade;
import org.stepic.droid.util.AppConstants;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class StoreStateManager implements IStoreStateManager {

    private DatabaseFacade mDatabaseFacade;
    private Bus bus;

    @Inject
    public StoreStateManager(DatabaseFacade dbManager, Bus bus) {
        mDatabaseFacade = dbManager;
        this.bus = bus;
    }

    @Override
    public void updateUnitLessonState(long lessonId) {
        List<Step> steps = mDatabaseFacade.getStepsOfLesson(lessonId);
        for (Step step : steps) {
            if (!step.is_cached()) return;
        }

        //all steps of lesson is cached
        Lesson lesson = mDatabaseFacade.getLessonById(lessonId);
        if (lesson == null) {
            YandexMetrica.reportEvent(AppConstants.METRICA_LESSON_IN_STORE_STATE_NULL);
            return;
        }
        lesson.setIs_loading(false);
        lesson.setIs_cached(true);
        mDatabaseFacade.updateOnlyCachedLoadingLesson(lesson);

        final Unit unit = mDatabaseFacade.getUnitByLessonId(lessonId);
        if (unit == null) {
            YandexMetrica.reportEvent(AppConstants.METRICA_UNIT_IN_STORE_STATE_NULL);
            return;
        }
        if (unit.is_loading() || !unit.is_cached()) {
            unit.setIs_loading(false);
            unit.setIs_cached(true);
            mDatabaseFacade.updateOnlyCachedLoadingUnit(unit);

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

        Lesson lesson = mDatabaseFacade.getLessonById(lessonId);
        final Unit unit = mDatabaseFacade.getUnitByLessonId(lessonId);

        if (lesson.is_cached() || lesson.is_loading()) {
            lesson.setIs_loading(false);
            lesson.setIs_cached(false);
            mDatabaseFacade.updateOnlyCachedLoadingLesson(lesson);
        }


        if (unit.is_cached() || unit.is_loading()) {
            unit.setIs_loading(false);
            unit.setIs_cached(false);
            mDatabaseFacade.updateOnlyCachedLoadingUnit(unit);

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
        final Section section = mDatabaseFacade.getSectionById(sectionId);
        if (section == null) {
            YandexMetrica.reportError(AppConstants.NULL_SECTION, new Exception("update Section after deleting"));
            return;
        }
        if (section.is_cached() || section.is_loading()) {
            section.setIs_cached(false);
            section.setIs_loading(false);
            mDatabaseFacade.updateOnlyCachedLoadingSection(section);
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
//Don't need suppot course state
//        updateCourseAfterDeleting(section.getCourse());
    }

    @Deprecated
    private void updateCourseAfterDeleting(long courseId) {

        Course course = mDatabaseFacade.getCourseById(courseId, DatabaseFacade.Table.enrolled);
        if (course == null) {
            YandexMetrica.reportError(AppConstants.NULL_COURSE, new Exception("update Course after deleting"));
            return;
        }
        if (course.is_cached() || course.is_loading()) {
            course.setIs_cached(false);
            course.setIs_loading(false);
            mDatabaseFacade.updateOnlyCachedLoadingCourse(course, DatabaseFacade.Table.enrolled);
        }
    }

    @Override
    public void updateSectionState(long sectionId) {
        List<Unit> units = mDatabaseFacade.getAllUnitsOfSection(sectionId);
        for (Unit unit : units) {
            if (!unit.is_cached()) return;
        }

        //all units, lessons, steps of sections are cached
        final Section section = mDatabaseFacade.getSectionById(sectionId);
        if (section == null) {
            YandexMetrica.reportError(AppConstants.NULL_SECTION, new Exception("update section state"));
            return;
        }
        if (!section.is_cached() || section.is_loading()) {
            section.setIs_cached(true);
            section.setIs_loading(false);
            mDatabaseFacade.updateOnlyCachedLoadingSection(section);
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
//do not update course state
//        updateCourseState(section.getCourse());
    }

    @Deprecated
    private void updateCourseState(long courseId) {
        Course course = mDatabaseFacade.getCourseById(courseId, DatabaseFacade.Table.enrolled);
        if (course == null) {
            course = mDatabaseFacade.getCourseById(courseId, DatabaseFacade.Table.featured);
            mDatabaseFacade.addCourse(course, DatabaseFacade.Table.enrolled);
        }
        List<Section> sections = mDatabaseFacade.getAllSectionsOfCourse(course);
        for (Section section : sections) {
            if (!section.is_cached()) return;
        }

        if (course == null) {
            YandexMetrica.reportError(AppConstants.NULL_COURSE, new Exception("null course in update course state"));
            return;
        }

        course.setIs_loading(false);
        course.setIs_cached(true);
        mDatabaseFacade.updateOnlyCachedLoadingCourse(course, DatabaseFacade.Table.enrolled);
    }
}
