package org.stepic.droid.store;

import android.content.Intent;
import android.os.Parcelable;

import org.stepic.droid.base.App;
import org.stepic.droid.model.Lesson;
import org.stepic.droid.model.Section;
import org.stepic.droid.services.CancelLoadingService;
import org.stepic.droid.services.LoadService;
import org.stepic.droid.util.AppConstants;

import java.io.Serializable;

import javax.inject.Singleton;

@Singleton
public class DownloadManagerImpl implements IDownloadManager {

    @Override
    public void addSection(Section section) {
        Intent loadIntent = new Intent(App.getAppContext(), LoadService.class);

        loadIntent.putExtra(AppConstants.KEY_LOAD_TYPE, LoadService.LoadTypeKey.Section);
        loadIntent.putExtra(AppConstants.KEY_SECTION_BUNDLE, (Serializable) section);

        App.getAppContext().startService(loadIntent);
    }

    @Override
    public void addLesson(final Lesson lesson) {
        Intent loadIntent = new Intent(App.getAppContext(), LoadService.class);

        loadIntent.putExtra(AppConstants.KEY_LOAD_TYPE, LoadService.LoadTypeKey.Lesson);
        loadIntent.putExtra(AppConstants.KEY_LESSON_BUNDLE, (Parcelable) lesson);

        App.getAppContext().startService(loadIntent);

    }

    @Override
    public void cancelStep(long stepId) {
        Intent loadIntent = new Intent(App.getAppContext(), CancelLoadingService.class);

        loadIntent.putExtra(AppConstants.KEY_LOAD_TYPE, LoadService.LoadTypeKey.Step);
        loadIntent.putExtra(AppConstants.KEY_STEP_BUNDLE, stepId);

        App.getAppContext().startService(loadIntent);

    }

}
