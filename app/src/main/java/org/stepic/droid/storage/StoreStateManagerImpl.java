package org.stepic.droid.storage;

import com.squareup.otto.Bus;

import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.concurrency.MainHandler;
import org.stepic.droid.di.AppSingleton;
import org.stepic.droid.events.sections.NotCachedSectionEvent;
import org.stepic.droid.events.sections.SectionCachedEvent;
import org.stepic.droid.events.units.LessonCachedEvent;
import org.stepic.droid.events.units.NotCachedLessonEvent;
import org.stepic.droid.model.Lesson;
import org.stepic.droid.model.Section;
import org.stepic.droid.model.Step;
import org.stepic.droid.model.Unit;
import org.stepic.droid.storage.operations.DatabaseFacade;

import java.util.List;

import javax.inject.Inject;

import kotlin.jvm.functions.Function0;

@AppSingleton
public class StoreStateManagerImpl implements StoreStateManager {

    private DatabaseFacade databaseFacade;
    private Bus bus;
    private Analytic analytic;
    private MainHandler mainHandler;

    @Inject
    public StoreStateManagerImpl(DatabaseFacade databaseFacade, Bus bus, Analytic analytic, MainHandler mainHandler) {
        this.databaseFacade = databaseFacade;
        this.bus = bus;
        this.analytic = analytic;
        this.mainHandler = mainHandler;
    }

    @Override
    public void updateUnitLessonState(long lessonId) {
        List<Step> steps = databaseFacade.getStepsOfLesson(lessonId);
        for (Step step : steps) {
            if (!step.is_cached()) return;
        }

        //all steps of lesson is cached
        final Lesson lesson = databaseFacade.getLessonById(lessonId);
        if (lesson == null) {
            analytic.reportError(Analytic.Error.LESSON_IN_STORE_STATE_NULL, new NullPointerException("lesson was null"));
            return;
        }
        lesson.set_loading(false);
        lesson.set_cached(true);
        databaseFacade.updateOnlyCachedLoadingLesson(lesson);
        mainHandler.post(new Function0<kotlin.Unit>() {
            @Override
            public kotlin.Unit invoke() {
                bus.post(new LessonCachedEvent(lesson.getId()));
                return kotlin.Unit.INSTANCE;
            }
        });
        Unit unit = databaseFacade.getUnitByLessonId(lessonId);
        if (unit != null) {
            updateSectionState(unit.getSection());
        }
    }

    @Override
    public void updateUnitLessonAfterDeleting(long lessonId) {
        //now unit lesson and all steps are deleting
        //cached = false, loading false
        //just make for parents
        //// FIXME: 14.12.15 it is not true, see related commit. Now we can delete one step.

        final Lesson lesson = databaseFacade.getLessonById(lessonId);
        final Unit unit = databaseFacade.getUnitByLessonId(lessonId);

        if (lesson != null && (lesson.is_cached() || lesson.is_loading())) {
            lesson.set_loading(false);
            lesson.set_cached(false);
            databaseFacade.updateOnlyCachedLoadingLesson(lesson);
            mainHandler.post(new Function0<kotlin.Unit>() {
                @Override
                public kotlin.Unit invoke() {
                    bus.post(new NotCachedLessonEvent(lesson.getId()));
                    return kotlin.Unit.INSTANCE;
                }
            });
        }
        if (unit != null) {
            updateSectionAfterDeleting(unit.getSection());
        }
    }

    @Override
    public void updateStepAfterDeleting(Step step) {
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
            mainHandler.post(
                    new Function0<kotlin.Unit>() {
                        @Override
                        public kotlin.Unit invoke() {
                            bus.post(new NotCachedSectionEvent(section.getId()));
                            return kotlin.Unit.INSTANCE;
                        }
                    }
            );
        }
    }

    @Override
    public void updateSectionState(long sectionId) {
        List<Unit> units = databaseFacade.getAllUnitsOfSection(sectionId);
        long[] lessonIds = new long[units.size()];
        for (int i = 0; i < units.size(); i++) {
            lessonIds[i] = units.get(i).getLesson();
        }
        List<Lesson> lessonList = databaseFacade.getLessonsByIds(lessonIds);
        for (Lesson lesson : lessonList) {
            if (!lesson.is_cached()) {
                return;
            }
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
            mainHandler.post(new Function0<kotlin.Unit>() {
                @Override
                public kotlin.Unit invoke() {
                    bus.post(new SectionCachedEvent(section.getId()));
                    return kotlin.Unit.INSTANCE;
                }
            });
        }
    }

}
