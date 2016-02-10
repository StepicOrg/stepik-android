package org.stepic.droid.core;

import android.os.Handler;

import com.squareup.otto.Bus;

import org.stepic.droid.base.MainApplication;
import org.stepic.droid.events.units.UnitProgressUpdateEvent;
import org.stepic.droid.events.units.UnitScoreUpdateEvent;
import org.stepic.droid.model.Assignment;
import org.stepic.droid.model.Progress;
import org.stepic.droid.model.Step;
import org.stepic.droid.model.Unit;
import org.stepic.droid.store.operations.DatabaseManager;
import org.stepic.droid.util.StringUtil;

import java.util.List;

import javax.inject.Inject;

public class LocalProgressOfUnitManager implements ILocalProgressManager {
    private DatabaseManager mDatabaseManager;
    private Bus mBus;

    @Inject
    public LocalProgressOfUnitManager(DatabaseManager databaseManager, Bus bus) {
        mDatabaseManager = databaseManager;
        mBus = bus;
    }


    @Override
    public void checkUnitAsPassed(final long stepId) {
        Step step = mDatabaseManager.getStepById(stepId);
        if (step == null) return;
        List<Step> stepList = mDatabaseManager.getStepsOfLesson(step.getLesson());
        for (Step stepItem : stepList) {
            if (!stepItem.is_custom_passed()) return;
        }

        Unit unit = mDatabaseManager.getUnitByLessonId(step.getLesson());
        if (unit == null) return;

//        unit.setIs_viewed_custom(true);
//        mDatabaseManager.addUnit(unit); //// TODO: 26.01.16 progress is not saved
        mDatabaseManager.markProgressAsPassedIfInDb(unit.getProgress());

        final long unitId = unit.getId();
        Handler mainHandler = new Handler(MainApplication.getAppContext().getMainLooper());
        //Say to ui that ui is cached now
        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                mBus.post(new UnitProgressUpdateEvent(unitId));
            }
        };
        mainHandler.post(myRunnable);

    }

    @Override
    public void updateStepProgress(long stepId, String scoreStr) {
        long assignmentId = mDatabaseManager.getAssignmentIdByStepId(stepId);
        Assignment assignment = mDatabaseManager.getAssignmentById(assignmentId);
        if (assignment == null) {
            return;
        }

        String progressId = assignment.getProgress();
        Progress progress= mDatabaseManager.getProgressById(progressId);
        Double oldDoubleScore = getScoreOfProgressFromDb(progress);
        if (oldDoubleScore == null) return;

        Double score = StringUtil.safetyParseString(scoreStr);
        if (score == null) return;
        if (oldDoubleScore < score) {
            progress.setScore(score + "");
            mDatabaseManager.addProgress(progress);
            Step step = mDatabaseManager.getStepById(stepId);
            if (step == null) return;
            final Unit unit =  mDatabaseManager.getUnitByLessonId(step.getLesson());
            if (unit == null) return;

            Progress unitProgress = mDatabaseManager.getProgressById(unit.getProgress());
            Double scoreInUnit = getScoreOfProgressFromDb(unitProgress);
            if (scoreInUnit == null) return;
            scoreInUnit = scoreInUnit + score - oldDoubleScore;
            unitProgress.setScore(scoreInUnit + "");
            mDatabaseManager.addProgress(unitProgress);

            Handler mainHandler = new Handler(MainApplication.getAppContext().getMainLooper());
            final Double finalScoreInUnit = scoreInUnit;
            Runnable myRunnable = new Runnable() {
                @Override
                public void run() {
                    mBus.post(new UnitScoreUpdateEvent(unit.getId(), finalScoreInUnit));
                }
            };
            mainHandler.post(myRunnable);

        }
    }

    private Double getScoreOfProgressFromDb(Progress progress) {

        if (progress == null) return null;
        String oldScore = progress.getScore();
        return StringUtil.safetyParseString(oldScore);
    }
}
