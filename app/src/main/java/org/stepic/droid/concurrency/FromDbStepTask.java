package org.stepic.droid.concurrency;

import com.squareup.otto.Bus;

import org.stepic.droid.base.MainApplication;
import org.stepic.droid.events.steps.FromDbStepEvent;
import org.stepic.droid.model.Lesson;
import org.stepic.droid.model.Step;
import org.stepic.droid.store.operations.DatabaseManager;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

public class FromDbStepTask extends StepicTask<Void, Void, List<Step>> {

    @Inject
    DatabaseManager mDatabaseManager;

    @Inject
    Bus mBus;
    private Lesson mLesson;


    public FromDbStepTask(Lesson mLesson) {
        super(MainApplication.getAppContext());
        this.mLesson = mLesson;
        MainApplication.component().inject(this);
    }

    @Override
    protected List<Step> doInBackgroundBody(Void... params) throws Exception {
        List<Step> stepList = mDatabaseManager.getStepsOfLesson(mLesson.getId());
        Collections.sort(stepList, new Comparator<Step>() {
            @Override
            public int compare(Step lhs, Step rhs) {
                if (lhs == null || rhs == null) return 0;

                long lhsPos = lhs.getPosition();
                long rhsPos = rhs.getPosition();
                return (int)(lhsPos - rhsPos);
            }
        });
        return stepList;
    }

    @Override
    protected void onPostExecute(AsyncResultWrapper<List<Step>> listAsyncResultWrapper) {
        super.onPostExecute(listAsyncResultWrapper);
        mBus.post(new FromDbStepEvent(listAsyncResultWrapper.getResult(), mLesson));
    }
}
