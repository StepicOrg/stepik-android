package org.stepic.droid.concurrency;

import android.support.v4.util.Pair;

import com.squareup.otto.Bus;

import org.stepic.droid.base.MainApplication;
import org.stepic.droid.events.units.LoadedFromDbUnitsLessonsEvent;
import org.stepic.droid.exceptions.UnitStoredButLessonNotException;
import org.stepic.droid.model.Lesson;
import org.stepic.droid.model.Section;
import org.stepic.droid.model.Unit;
import org.stepic.droid.store.operations.DatabaseManager;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class FromDbUnitLessonTask extends StepicTask<Void, Void, Pair<List<Unit>, List<Lesson>>> {
    @Inject
    DatabaseManager mDataBaseManager;

    @Inject
    Bus mBus;


    private Section mSection;

    public FromDbUnitLessonTask(Section section) {
        super(MainApplication.getAppContext());
        MainApplication.component().inject(this);
        mSection = section;
    }

    @Override
    protected Pair<List<Unit>, List<Lesson>> doInBackgroundBody(Void... params) throws Exception {
        List<Unit> fromCacheUnits = null;
        List<Lesson> fromCacheLessons = new ArrayList<>();

        fromCacheUnits = mDataBaseManager.getAllUnitsOfSection(mSection.getId());
        for (Unit unit :fromCacheUnits) {
            String progressId = unit.getProgress();
            unit.setIs_viewed_custom(mDataBaseManager.isViewedPublicWrapper(progressId));
        }


        for (Unit unitItem : fromCacheUnits) {
            Lesson lesson = mDataBaseManager.getLessonOfUnit(unitItem);
            if (lesson == null) {
                throw new UnitStoredButLessonNotException();
            }

            fromCacheLessons.add(lesson);
        }
        return new Pair<>(fromCacheUnits, fromCacheLessons);
    }

    @Override
    protected void onSuccess(Pair<List<Unit>, List<Lesson>> pair) {
        super.onSuccess(pair);
        mBus.post(new LoadedFromDbUnitsLessonsEvent(pair.first, pair.second, mSection));
    }
}
