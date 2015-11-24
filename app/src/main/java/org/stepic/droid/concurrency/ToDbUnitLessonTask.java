package org.stepic.droid.concurrency;

import com.squareup.otto.Bus;

import org.stepic.droid.base.MainApplication;
import org.stepic.droid.events.units.UnitLessonSavedEvent;
import org.stepic.droid.model.Assignment;
import org.stepic.droid.model.IProgressable;
import org.stepic.droid.model.Lesson;
import org.stepic.droid.model.Progress;
import org.stepic.droid.model.Section;
import org.stepic.droid.model.Unit;
import org.stepic.droid.store.operations.DatabaseManager;
import org.stepic.droid.util.ProgressUtil;
import org.stepic.droid.web.IApi;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class ToDbUnitLessonTask extends StepicTask<Void, Void, Void> {

    private final List<Unit> unitList;
    private final List<Lesson> lessonList;
    @Inject
    DatabaseManager mDatabaseManager;

    @Inject
    Bus mBus;

    @Inject
    IApi mApi;

    Section mSection;

    public ToDbUnitLessonTask(Section section, List<Unit> unitList, List<Lesson> lessonList) {
        super(MainApplication.getAppContext());
        this.unitList = unitList;
        this.lessonList = lessonList;
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
        List<Progress> progresses = mApi.getProgresses(ProgressUtil.getAllProgresses(unitList)).execute().body().getProgresses();
        for (Progress item : progresses) {
            mDatabaseManager.addProgress(item);
        }


        for (Unit unitItem : unitList) {

            List<Assignment> assignments = mApi.getAssignments(unitItem.getAssignments()).execute().body().getAssignments();
            for (Assignment item : assignments) {
                mDatabaseManager.addAssignment(item);
            }
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
