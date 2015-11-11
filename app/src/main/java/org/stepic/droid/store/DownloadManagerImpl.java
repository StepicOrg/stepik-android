package org.stepic.droid.store;

import android.content.Intent;

import org.stepic.droid.base.MainApplication;
import org.stepic.droid.model.Course;
import org.stepic.droid.model.Lesson;
import org.stepic.droid.model.Section;
import org.stepic.droid.model.Step;
import org.stepic.droid.model.Unit;
import org.stepic.droid.services.LoadService;
import org.stepic.droid.store.operations.DatabaseManager;
import org.stepic.droid.util.AppConstants;

import java.io.Serializable;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class DownloadManagerImpl implements IDownloadManager {


    @Inject
    public DownloadManagerImpl() {
    }



    private void addStep(Step step, Lesson lesson) {
        Intent loadIntent = new Intent(MainApplication.getAppContext(), LoadService.class);

        loadIntent.putExtra(AppConstants.KEY_LOAD_TYPE, LoadService.LoadTypeKey.Step);
        loadIntent.putExtra(AppConstants.KEY_LESSON_BUNDLE, (Serializable) lesson);
        loadIntent.putExtra(AppConstants.KEY_STEP_BUNDLE, (Serializable) step);

        MainApplication.getAppContext().startService(loadIntent);
    }

    @Override
    public void addSection(Section section) {
        Intent loadIntent = new Intent(MainApplication.getAppContext(), LoadService.class);

        loadIntent.putExtra(AppConstants.KEY_LOAD_TYPE, LoadService.LoadTypeKey.Section);
        loadIntent.putExtra(AppConstants.KEY_SECTION_BUNDLE, (Serializable) section);

        MainApplication.getAppContext().startService(loadIntent);
    }

    @Override
    public void addCourse(final Course course, DatabaseManager.Table type) {
        Intent loadIntent = new Intent(MainApplication.getAppContext(), LoadService.class);

        loadIntent.putExtra(AppConstants.KEY_LOAD_TYPE, LoadService.LoadTypeKey.Course);
        loadIntent.putExtra(AppConstants.KEY_COURSE_BUNDLE, (Serializable) course);
        loadIntent.putExtra(AppConstants.KEY_TABLE_TYPE, type);

        MainApplication.getAppContext().startService(loadIntent);
    }


    @Override
    public void addUnitLesson(final Unit unit, final Lesson lesson) {
        Intent loadIntent = new Intent(MainApplication.getAppContext(), LoadService.class);

        loadIntent.putExtra(AppConstants.KEY_LOAD_TYPE, LoadService.LoadTypeKey.UnitLesson);
        loadIntent.putExtra(AppConstants.KEY_UNIT_BUNDLE, (Serializable) unit);
        loadIntent.putExtra(AppConstants.KEY_LESSON_BUNDLE, (Serializable) lesson);

        MainApplication.getAppContext().startService(loadIntent);

    }

}
