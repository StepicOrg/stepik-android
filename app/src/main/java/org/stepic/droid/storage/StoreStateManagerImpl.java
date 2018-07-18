package org.stepic.droid.storage;

import android.support.annotation.WorkerThread;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.base.ListenerContainer;
import org.stepic.droid.concurrency.MainHandler;
import org.stepic.droid.di.AppSingleton;
import org.stepik.android.model.structure.Lesson;
import org.stepik.android.model.structure.Section;
import org.stepik.android.model.structure.Step;
import org.stepik.android.model.structure.Unit;
import org.stepic.droid.storage.operations.DatabaseFacade;

import java.util.List;

import javax.inject.Inject;

import kotlin.jvm.functions.Function0;

@AppSingleton
public class StoreStateManagerImpl implements StoreStateManager {

    private DatabaseFacade databaseFacade;
    private Analytic analytic;
    private MainHandler mainHandler;
    private ListenerContainer<StoreStateManager.LessonCallback> lessonCallbackContainer;
    private ListenerContainer<StoreStateManager.SectionCallback> sectionCallbackContainer;

    @Inject
    public StoreStateManagerImpl(DatabaseFacade databaseFacade,
                                 Analytic analytic,
                                 MainHandler mainHandler,
                                 ListenerContainer<LessonCallback> lessonCallbackContainer,
                                 ListenerContainer<SectionCallback> sectionCallbackContainer) {
        this.databaseFacade = databaseFacade;
        this.analytic = analytic;
        this.mainHandler = mainHandler;
        this.lessonCallbackContainer = lessonCallbackContainer;
        this.sectionCallbackContainer = sectionCallbackContainer;
    }

    @Override
    public void updateUnitLessonState(final long lessonId) {
        List<Step> steps = databaseFacade.getStepsOfLesson(lessonId);
        boolean cached = true;
        for (Step step : steps) {
            if (!step.isCached()) {
                cached = false;
                break;
            }
        }

        if (cached) {
            makeLessonCached(lessonId);

            Unit unit = databaseFacade.getUnitByLessonId(lessonId);
            if (unit != null) {
                updateSectionState(unit.getSection());
            }
        }
    }

    @WorkerThread
    private void makeLessonCached(final long lessonId) {
        final Lesson lesson = databaseFacade.getLessonById(lessonId);
        if (lesson == null) {
            analytic.reportError(Analytic.Error.LESSON_IN_STORE_STATE_NULL, new NullPointerException("lesson was null"));
            return;
        }

        lesson.setLoading(false);
        lesson.setCached(true);

        databaseFacade.updateOnlyCachedLoadingLesson(lesson);
        mainHandler.post(new Function0<kotlin.Unit>() {
            @Override
            public kotlin.Unit invoke() {
                for (LessonCallback callback : lessonCallbackContainer.asIterable()) {
                    callback.onLessonCached(lessonId);
                }
                return kotlin.Unit.INSTANCE;
            }
        });
    }

    @Override
    public void updateUnitLessonAfterDeleting(long lessonId) {
        //now unit lesson and all steps are deleting
        //cached = false, loading false
        //just make for parents
        //// FIXME: 14.12.15 it is not true, see related commit. Now we can delete one step.

        final Lesson lesson = databaseFacade.getLessonById(lessonId);
        final Unit unit = databaseFacade.getUnitByLessonId(lessonId);

        if (lesson != null && (lesson.isCached() || lesson.isLoading())) {
            lesson.setLoading(false);
            lesson.setCached(false);
            databaseFacade.updateOnlyCachedLoadingLesson(lesson);
            mainHandler.post(new Function0<kotlin.Unit>() {
                @Override
                public kotlin.Unit invoke() {
                    for (LessonCallback callback : lessonCallbackContainer.asIterable()) {
                        callback.onLessonNotCached(lesson.getId());
                    }
                    return kotlin.Unit.INSTANCE;
                }
            });
        }
        if (unit != null) {
            updateSectionAfterDeleting(unit.getSection());
        }
    }

    @Override
    public void updateStepAfterDeleting(@Nullable Step step) {
        if (step == null) {
            return;
        }
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
        if (section.isCached() || section.isLoading()) {
            section.setCached(false);
            section.setLoading(false);
            databaseFacade.updateOnlyCachedLoadingSection(section);
            mainHandler.post(
                    new Function0<kotlin.Unit>() {
                        @Override
                        public kotlin.Unit invoke() {
                            for (SectionCallback callback : sectionCallbackContainer.asIterable()) {
                                callback.onSectionNotCached(section.getId());
                            }
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

        boolean cached = true;

        for (Lesson lesson : lessonList) {
            if (!lesson.isCached()) {
                cached = false;
                break;
            }
        }

        if (cached) {
            makeSectionCached(sectionId);
        }
    }

    @WorkerThread
    private void makeSectionCached(long sectionId) {
        //all units, lessons, steps of section are cached
        final Section section = databaseFacade.getSectionById(sectionId);
        if (section == null) {
            analytic.reportError(Analytic.Error.NULL_SECTION, new Exception("update section state"));
            return;
        }

        section.setCached(true);
        section.setLoading(false);
        databaseFacade.updateOnlyCachedLoadingSection(section);

        mainHandler.post(new Function0<kotlin.Unit>() {
            @Override
            public kotlin.Unit invoke() {
                for (SectionCallback callback : sectionCallbackContainer.asIterable()) {
                    callback.onSectionCached(section.getId());
                }
                return kotlin.Unit.INSTANCE;
            }
        });
    }

    @Override
    public void addLessonCallback(@NotNull LessonCallback callback) {
        lessonCallbackContainer.add(callback);
    }

    @Override
    public void removeLessonCallback(@NotNull LessonCallback callback) {
        lessonCallbackContainer.remove(callback);
    }

    @Override
    public void addSectionCallback(@NotNull SectionCallback callback) {
        sectionCallbackContainer.add(callback);
    }

    @Override
    public void removeSectionCallback(@NotNull SectionCallback callback) {
        sectionCallbackContainer.remove(callback);
    }
}
