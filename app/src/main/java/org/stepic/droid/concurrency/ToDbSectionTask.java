package org.stepic.droid.concurrency;

import com.squareup.otto.Bus;

import org.stepic.droid.base.MainApplication;
import org.stepic.droid.events.sections.FinishingSaveSectionToDbEvent;
import org.stepic.droid.events.sections.StartingSaveSectionToDbEvent;
import org.stepic.droid.model.Section;
import org.stepic.droid.store.operations.DatabaseFacade;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class ToDbSectionTask extends StepicTask<Void, Void, Void> {

    @Inject
    Bus mBus;

    @Inject
    DatabaseFacade mDatabaseFacade;
    private final List<Section> mSectionList;


    public ToDbSectionTask(List<Section> sectionList) {
        super(MainApplication.getAppContext());
        MainApplication.component().inject(this);

        mSectionList = sectionList;
    }

    public ToDbSectionTask(Section section)
    {
        super(MainApplication.getAppContext());
        MainApplication.component().inject(this);

        mSectionList = new ArrayList<>();
        mSectionList.add(section);
    }

    @Override
    protected Void doInBackgroundBody(Void... params) throws Exception {
        for (Section sectionItem : mSectionList) {
                mDatabaseFacade.addSection(sectionItem);
        }
        return null;
    }

    @Override
    protected void onPreExecute() {
        mBus.post(new StartingSaveSectionToDbEvent());
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(AsyncResultWrapper<Void> voidAsyncResultWrapper) {
        super.onPostExecute(voidAsyncResultWrapper);
        mBus.post(new FinishingSaveSectionToDbEvent());
    }
}
