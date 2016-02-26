package org.stepic.droid.concurrency.tasks;

import com.squareup.otto.Bus;

import org.stepic.droid.base.MainApplication;
import org.stepic.droid.events.steps.SuccessToDbStepEvent;
import org.stepic.droid.model.Lesson;
import org.stepic.droid.model.Step;
import org.stepic.droid.store.operations.DatabaseFacade;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class ToDbStepTask extends StepicTask<Void, Void, Void> {
    private final List<Step> mStepList;
    private final Lesson mLesson;
    @Inject
    DatabaseFacade mDatabaseFacade;

    @Inject
    Bus mBus;

    public ToDbStepTask(Lesson parentLesson, List<Step> steps) {
        super(MainApplication.getAppContext());

        MainApplication.component().inject(this);
        mStepList = steps;
        mLesson = parentLesson;

    }

    public ToDbStepTask(Step oneStep) {
        super(MainApplication.getAppContext());
        MainApplication.component().inject(this);
        mStepList = new ArrayList<>();
        mStepList.add(oneStep);
        mLesson = null;

    }

    @Override
    protected Void doInBackgroundBody(Void... params) throws Exception {
        for (Step step : mStepList) {
            mDatabaseFacade.addStep(step);
        }
        return null;
    }

    @Override
    protected void onPostExecute(AsyncResultWrapper<Void> voidAsyncResultWrapper) {
        super.onPostExecute(voidAsyncResultWrapper);
        mBus.post(new SuccessToDbStepEvent(mLesson));
    }
}
