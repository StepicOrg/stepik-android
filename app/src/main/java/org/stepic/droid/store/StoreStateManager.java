package org.stepic.droid.store;

import android.os.Handler;

import com.squareup.otto.Bus;

import org.stepic.droid.analytic.Analytic;
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
import org.stepic.droid.store.operations.Table;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class StoreStateManager implements IStoreStateManager {

    private DatabaseFacade databaseFacade;
    private Bus bus;
    private Analytic analytic;

    @Inject
    public StoreStateManager(DatabaseFacade dbManager, Bus bus, Analytic analytic) {
        databaseFacade = dbManager;
        this.bus = bus;
        this.analytic = analytic;
    }

    @Override
    public void updateUnitLessonState(long lessonId) {
        List<Step> steps = databaseFacade.getStepsOfLesson(lessonId);
        for (Step step : steps) {
            if (!step.is_cached()) return;
        }

        //all steps of lesson is cached
        Lesson lesson = databaseFacade.getLessonById(lessonId);
        if (lesson == null) {
            analytic.reportError(Analytic.Error.LESSON_IN_STORE_STATE_NULL, new NullPointerException("lesson was null"));
            return;
        }
        lesson.set_loading(false);
        lesson.set_cached(true);
        databaseFacade.updateOnlyCachedLoadingLesson(lesson);

        final Unit unit = databaseFacade.getUnitByLessonId(lessonId);
        if (unit == null) {
            analytic.reportError(Analytic.Error.UNIT_IN_STORE_STATE_NULL, new NullPointerException("unit is null"));
            return;
        }
        if (unit.is_loading() || !unit.is_cached()) {
            unit.set_loading(false);
            unit.set_cached(true);
            databaseFacade.updateOnlyCachedLoadingUnit(unit);

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

        Lesson lesson = databaseFacade.getLessonById(lessonId);
        final Unit unit = databaseFacade.getUnitByLessonId(lessonId);

        if (lesson.is_cached() || lesson.is_loading()) {
            lesson.set_loading(false);
            lesson.set_cached(false);
            databaseFacade.updateOnlyCachedLoadingLesson(lesson);
        }


        if (unit.is_cached() || unit.is_loading()) {
            unit.set_loading(false);
            unit.set_cached(false);
            databaseFacade.updateOnlyCachedLoadingUnit(unit);

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
        final Section section = databaseFacade.getSectionById(sectionId);
        if (section == null) {
            analytic.reportError(Analytic.Error.NULL_SECTION, new Exception("update Section after deleting"));
            return;
        }
        if (section.is_cached() || section.is_loading()) {
            section.set_cached(false);
            section.set_loading(false);
            databaseFacade.updateOnlyCachedLoadingSection(section);
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
//Don't need support course state
//        updateCourseAfterDeleting(section.getCourse());
    }

    @Deprecated
    private void updateCourseAfterDeleting(long courseId) {

        Course course = databaseFacade.getCourseById(courseId, Table.enrolled);
        if (course == null) {
            analytic.reportError(Analytic.Error.NULL_COURSE, new Exception("update Course after deleting"));
            return;
        }
        if (course.is_cached() || course.is_loading()) {
            course.set_cached(false);
            course.set_loading(false);
            databaseFacade.updateOnlyCachedLoadingCourse(course, Table.enrolled);
        }
    }

    @Override
    public void updateSectionState(long sectionId) {
        List<Unit> units = databaseFacade.getAllUnitsOfSection(sectionId);
        for (Unit unit : units) {
            if (!unit.is_cached()) return;
        }

        //all units, lessons, steps of sections are cached
        final Section section = databaseFacade.getSectionById(sectionId);
        if (section == null) {
            analytic.reportError(Analytic.Error.NULL_SECTION, new Exception("update section state"));
            return;
        }
        if (!section.is_cached() || section.is_loading()) {
            section.set_cached(true);
            section.set_loading(false);
            databaseFacade.updateOnlyCachedLoadingSection(section);
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
        Course course = databaseFacade.getCourseById(courseId, Table.enrolled);
        if (course == null) {
            course = databaseFacade.getCourseById(courseId, Table.featured);
            databaseFacade.addCourse(course, Table.enrolled);
        }
        List<Section> sections = databaseFacade.getAllSectionsOfCourse(course);
        for (Section section : sections) {
            if (!section.is_cached()) return;
        }

        if (course == null) {
            analytic.reportError(Analytic.Error.NULL_COURSE, new Exception("null course in update course state"));
            return;
        }

        course.set_loading(false);
        course.set_cached(true);
        databaseFacade.updateOnlyCachedLoadingCourse(course, Table.enrolled);
    }
}
