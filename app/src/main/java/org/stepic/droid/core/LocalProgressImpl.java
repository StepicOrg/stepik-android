package org.stepic.droid.core;

import com.squareup.otto.Bus;

import org.stepic.droid.concurrency.MainHandler;
import org.stepic.droid.events.UpdateSectionProgressEvent;
import org.stepic.droid.model.Progress;
import org.stepic.droid.model.Section;
import org.stepic.droid.model.Step;
import org.stepic.droid.model.Unit;
import org.stepic.droid.storage.operations.DatabaseFacade;
import org.stepic.droid.util.StringUtil;
import org.stepic.droid.web.Api;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import kotlin.jvm.functions.Function0;
import timber.log.Timber;

public class LocalProgressImpl implements LocalProgressManager {
    private DatabaseFacade databaseFacade;
    private Bus bus;
    private Api api;
    private MainHandler mainHandler;

    private final Set<UnitProgressListener> unitProgressListeners = new HashSet<>();

    @Inject
    public LocalProgressImpl(DatabaseFacade databaseFacade, Bus bus, Api api, MainHandler mainHandler) {
        this.databaseFacade = databaseFacade;
        this.bus = bus;
        this.api = api;
        this.mainHandler = mainHandler;
    }


    @Override
    public void checkUnitAsPassed(final long stepId) {
        Step step = databaseFacade.getStepById(stepId);
        if (step == null) return;
        List<Step> stepList = databaseFacade.getStepsOfLesson(step.getLesson());
        for (Step stepItem : stepList) {
            if (!stepItem.is_custom_passed()) return;
        }

        Unit unit = databaseFacade.getUnitByLessonId(step.getLesson());
        if (unit == null) return;

//        unit.set_viewed_custom(true);
//        mDatabaseFacade.addUnit(unit); //// TODO: 26.01.16 progress is not saved
        if (unit.getProgressId() != null) {
            databaseFacade.markProgressAsPassedIfInDb(unit.getProgressId());
        }

        final long unitId = unit.getId();
        //Say to ui that ui is cached now
        mainHandler.post(new Function0<kotlin.Unit>() {
            @Override
            public kotlin.Unit invoke() {
                for (UnitProgressListener unitProgressListener : unitProgressListeners) {
                    unitProgressListener.onUnitPassed(unitId);
                }
                return kotlin.Unit.INSTANCE;
            }
        });
    }

    @Override
    public void updateUnitProgress(final long unitId) {

        Unit unit = databaseFacade.getUnitById(unitId);
        if (unit == null) return;
        Progress updatedUnitProgress;
        try {
            updatedUnitProgress = api.getProgresses(new String[]{unit.getProgressId()}).execute().body().getProgresses().get(0);
        } catch (Exception e) {
            //if we have no progress of unit or progress is null -> do nothing
            return;
        }
        if (updatedUnitProgress == null)
            return;
        databaseFacade.addProgress(updatedUnitProgress);

        final Double finalScoreInUnit = getScoreOfProgress(updatedUnitProgress);
        if (finalScoreInUnit == null) {
            return;
        }
        mainHandler.post(new Function0<kotlin.Unit>() {
            @Override
            public kotlin.Unit invoke() {
                for (UnitProgressListener unitProgressListener : unitProgressListeners) {
                    unitProgressListener.onScoreUpdated(unitId, finalScoreInUnit);
                }
                return kotlin.Unit.INSTANCE;
            }
        });

        //after that update section progress
        final long sectionId = unit.getSection();
        try {
            final Section persistentSection = databaseFacade.getSectionById(sectionId);
            if (persistentSection == null) {
                return;
            }

            String progressId = persistentSection.getProgress();
            if (progressId == null) {
                return;
            }

            final Progress progress = api.getProgresses(new String[]{progressId}).execute().body().getProgresses().get(0);
            databaseFacade.addProgress(progress);
            mainHandler.post(new Function0<kotlin.Unit>() {
                @Override
                public kotlin.Unit invoke() {
                    bus.post(new UpdateSectionProgressEvent(progress, persistentSection.getCourse()));
                    return kotlin.Unit.INSTANCE;
                }
            });
        } catch (Exception exception) {
            Timber.e(exception);
        }
    }

    @Override
    public synchronized void subscribe(UnitProgressListener unitProgressListener) {
        unitProgressListeners.add(unitProgressListener);
    }

    @Override
    public synchronized void unsubscribe(UnitProgressListener unitProgressListener) {
        unitProgressListeners.remove(unitProgressListener);
    }

    private Double getScoreOfProgress(Progress progress) {
        if (progress == null) return null;
        String oldScore = progress.getScore();
        return StringUtil.safetyParseString(oldScore);
    }
}
