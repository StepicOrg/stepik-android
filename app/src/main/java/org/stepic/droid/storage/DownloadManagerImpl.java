package org.stepic.droid.storage;

import android.content.Intent;
import android.os.Parcelable;

import org.stepic.droid.base.App;
import org.stepic.droid.di.AppSingleton;
import org.stepik.android.model.Lesson;
import org.stepik.android.model.Section;
import org.stepic.droid.services.CancelLoadingService;
import org.stepic.droid.services.LoadService;
import org.stepic.droid.util.AppConstants;

import javax.inject.Inject;

@AppSingleton
public class DownloadManagerImpl implements IDownloadManager {

    @Inject
    DownloadManagerImpl() {
    }

    @Override
    public void addSection(Section section) {
        Intent loadIntent = new Intent(App.Companion.getAppContext(), LoadService.class);

        loadIntent.putExtra(AppConstants.KEY_LOAD_TYPE, LoadService.LoadTypeKey.Section);
        loadIntent.putExtra(AppConstants.KEY_SECTION_BUNDLE, section);

        App.Companion.getAppContext().startService(loadIntent);
    }

    @Override
    public void addLesson(final Lesson lesson) {
        Intent loadIntent = new Intent(App.Companion.getAppContext(), LoadService.class);

        loadIntent.putExtra(AppConstants.KEY_LOAD_TYPE, LoadService.LoadTypeKey.Lesson);
        loadIntent.putExtra(AppConstants.KEY_LESSON_BUNDLE, (Parcelable) lesson);

        App.Companion.getAppContext().startService(loadIntent);

    }

    @Override
    public void cancelStep(long stepId) {
        Intent loadIntent = new Intent(App.Companion.getAppContext(), CancelLoadingService.class);

        loadIntent.putExtra(AppConstants.KEY_LOAD_TYPE, LoadService.LoadTypeKey.Step);
        loadIntent.putExtra(AppConstants.KEY_STEP_BUNDLE, stepId);

        App.Companion.getAppContext().startService(loadIntent);

    }

}
