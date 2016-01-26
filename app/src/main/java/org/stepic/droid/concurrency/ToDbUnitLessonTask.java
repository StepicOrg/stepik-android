package org.stepic.droid.concurrency;

import com.squareup.otto.Bus;

import org.stepic.droid.base.MainApplication;
import org.stepic.droid.events.units.UnitLessonSavedEvent;
import org.stepic.droid.model.Lesson;
import org.stepic.droid.model.Progress;
import org.stepic.droid.model.Section;
import org.stepic.droid.model.Unit;
import org.stepic.droid.store.operations.DatabaseManager;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class ToDbUnitLessonTask extends StepicTask<Void, Void, Void> {

    private final List<Unit> unitList;
    private final List<Lesson> lessonList;
    private List<Progress> progresses;
    @Inject
    DatabaseManager mDatabaseManager;

    @Inject
    Bus mBus;

    Section mSection;

    public ToDbUnitLessonTask(Section section, List<Unit> unitList, List<Lesson> lessonList, List<Progress> progresses) {
        super(MainApplication.getAppContext());
        this.unitList = unitList;
        this.lessonList = lessonList;
        this.progresses = progresses;
        MainApplication.component().inject(this);
        mSection = section;

    }

    public ToDbUnitLessonTask(Unit unit, Lesson lesson) {
        super(MainApplication.getAppContext());

        unitList = new ArrayList<>();
        unitList.add(unit);
        lessonList = new ArrayList<>();
        lessonList.add(lesson);
        MainApplication.component().inject(this);
        mSection = null;

    }

    @Override
    protected Void doInBackgroundBody(Void... params) throws Exception {
        for (Progress item : progresses) {
            mDatabaseManager.addProgress(item);
        }
        
        for (Unit unitItem : unitList) {
            mDatabaseManager.addUnit(unitItem);
        }
        for (Lesson lessonItem : lessonList) {
            mDatabaseManager.addLesson(lessonItem);
        }

        return null;
    }

    @Override
    protected void onSuccess(Void aVoid) {
        super.onSuccess(aVoid);
        mBus.post(new UnitLessonSavedEvent(mSection, unitList, lessonList));
    }
}
