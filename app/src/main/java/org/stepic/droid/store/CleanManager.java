package org.stepic.droid.store;

import android.content.Intent;
import android.os.Parcelable;

import org.stepic.droid.base.MainApplication;
import org.stepic.droid.model.Lesson;
import org.stepic.droid.model.Section;
import org.stepic.droid.model.Step;
import org.stepic.droid.services.DeleteService;
import org.stepic.droid.services.LoadService;
import org.stepic.droid.util.AppConstants;

import java.io.Serializable;

import javax.inject.Inject;

public class CleanManager {
    @Inject
    public CleanManager() {

    }

    public void removeStep(Step step) {
        Intent loadIntent = new Intent(MainApplication.getAppContext(), DeleteService.class);

        loadIntent.putExtra(AppConstants.KEY_LOAD_TYPE, LoadService.LoadTypeKey.Step);
        loadIntent.putExtra(AppConstants.KEY_STEP_BUNDLE, (Serializable) step);

        MainApplication.getAppContext().startService(loadIntent);
    }

    public void removeSection(Section section) {
        Intent loadIntent = new Intent(MainApplication.getAppContext(), DeleteService.class);

        loadIntent.putExtra(AppConstants.KEY_LOAD_TYPE, LoadService.LoadTypeKey.Section);
        loadIntent.putExtra(AppConstants.KEY_SECTION_BUNDLE, (Serializable) section);

        MainApplication.getAppContext().startService(loadIntent);
    }


    public void removeLesson(final Lesson lesson) {
        Intent loadIntent = new Intent(MainApplication.getAppContext(), DeleteService.class);

        loadIntent.putExtra(AppConstants.KEY_LOAD_TYPE, LoadService.LoadTypeKey.Lesson);
        loadIntent.putExtra(AppConstants.KEY_LESSON_BUNDLE, (Parcelable) lesson);

        MainApplication.getAppContext().startService(loadIntent);

    }
}
