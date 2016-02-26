package org.stepic.droid.concurrency.tasks;

import com.squareup.otto.Bus;

import org.stepic.droid.base.MainApplication;
import org.stepic.droid.events.sections.FinishingGetSectionFromDbEvent;
import org.stepic.droid.events.sections.StartingGetSectionFromDbEvent;
import org.stepic.droid.model.Course;
import org.stepic.droid.model.Section;
import org.stepic.droid.store.operations.DatabaseFacade;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

public class FromDbSectionTask extends StepicTask<Void, Void, List<Section>> {

    @Inject
    DatabaseFacade mDatabaseFacade;

    @Inject
    Bus mBus;

    private Course mCourse;

    public FromDbSectionTask(Course course) {
        super(MainApplication.getAppContext());

        MainApplication.component().inject(this);

        mCourse = course;

    }

    @Override
    protected List<Section> doInBackgroundBody(Void... params) throws Exception {
        List<Section> fromCache = null;
        fromCache = mDatabaseFacade.getAllSectionsOfCourse(mCourse);
        Collections.sort(fromCache, new Comparator<Section>() {
            @Override
            public int compare(Section lhs, Section rhs) {
                if (lhs == null || rhs == null) return 0;

                int lhsPos = lhs.getPosition();
                int rhsPos = rhs.getPosition();
                return lhsPos - rhsPos;
            }
        });
        return fromCache;
    }

    @Override
    protected void onPreExecute() {
        mBus.post(new StartingGetSectionFromDbEvent(mCourse));
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(AsyncResultWrapper<List<Section>> listAsyncResultWrapper) {
        super.onPostExecute(listAsyncResultWrapper);
        mBus.post(new FinishingGetSectionFromDbEvent(mCourse, listAsyncResultWrapper.getResult()));
    }
}
