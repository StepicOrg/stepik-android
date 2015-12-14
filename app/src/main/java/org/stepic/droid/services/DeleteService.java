package org.stepic.droid.services;

import android.app.DownloadManager;
import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.squareup.otto.Bus;
import com.yandex.metrica.YandexMetrica;

import org.stepic.droid.base.MainApplication;
import org.stepic.droid.model.Course;
import org.stepic.droid.model.Lesson;
import org.stepic.droid.model.Section;
import org.stepic.droid.model.Step;
import org.stepic.droid.model.Unit;
import org.stepic.droid.preferences.UserPreferences;
import org.stepic.droid.store.IStoreStateManager;
import org.stepic.droid.store.operations.DatabaseManager;
import org.stepic.droid.util.AppConstants;
import org.stepic.droid.util.resolvers.IVideoResolver;
import org.stepic.droid.web.IApi;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class DeleteService extends IntentService {
    @Inject
    DownloadManager mSystemDownloadManager;
    @Inject
    UserPreferences mUserPrefs;
    @Inject
    Bus mBus;
    @Inject
    IVideoResolver mResolver;
    @Inject
    IApi mApi;
    @Inject
    DatabaseManager mDb;
    @Inject
    IStoreStateManager mStoreStateManager;

    public DeleteService() {
        super("delete_service");

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        MainApplication.component().inject(this);
        super.onStartCommand(intent, flags, startId);
        return START_REDELIVER_INTENT;
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        LoadService.LoadTypeKey type = (LoadService.LoadTypeKey) intent.getSerializableExtra(AppConstants.KEY_LOAD_TYPE);
        Log.i("downloading", "Service: " + Thread.currentThread().getName());
        try {
            switch (type) {
                case Course:
                    Course course = (Course) intent.getSerializableExtra(AppConstants.KEY_COURSE_BUNDLE);
                    DatabaseManager.Table tableType = (DatabaseManager.Table) intent.getSerializableExtra(AppConstants.KEY_TABLE_TYPE);
                    removeFromDisk(course, tableType);
                    break;
                case Section:
                    Section section = (Section) intent.getSerializableExtra(AppConstants.KEY_SECTION_BUNDLE);
                    removeFromDisk(section);
                    break;
                case UnitLesson:
                    Unit unit = (Unit) intent.getSerializableExtra(AppConstants.KEY_UNIT_BUNDLE);
                    Lesson lesson = (Lesson) intent.getSerializableExtra(AppConstants.KEY_LESSON_BUNDLE);
                    removeFromDisk(unit, lesson);
                    break;
                case Step:
                    Step step = (Step) intent.getSerializableExtra(AppConstants.KEY_STEP_BUNDLE);
                    removeFromDisk(step);
                    break;
            }
        } catch (NullPointerException ex) {
            //possibly user click clear cache;
//            throw ex;
            YandexMetrica.reportError("DeleteService nullptr", ex);

            mDb.dropDatabase();
        }
    }

    private void removeFromDisk(Step step) {
        if (step.getBlock().getVideo() != null) {
            String path = mDb.getPathToVideoIfExist(step.getBlock().getVideo());
            File file = new File(path);
            if (file.exists()) {
                file.delete();
            }

            mDb.deleteVideo(step.getBlock().getVideo());
        }

        mDb.deleteStep(step); // remove steps
        mStoreStateManager.updateStepAfterDeleting(step);
    }

    private void removeFromDisk(Unit unit, Lesson lesson) {
        List<Step> steps = mDb.getStepsOfLesson(lesson.getId());
        for (Step step : steps) {
            removeFromDisk(step);
        }

//        unit.setIs_cached(false);
//        unit.setIs_loading(false);
//        lesson.setIs_cached(false);
//        lesson.setIs_loading(false);
//        mDb.updateOnlyCachedLoadingLesson(lesson);
//        mDb.updateOnlyCachedLoadingUnit(unit);
//        mStoreStateManager.updateUnitLessonAfterDeleting(unit);

    }

    private void removeFromDisk(Section section) {
        List<Unit> units = mDb.getAllUnitsOfSection(section.getId());
        List<Lesson> lessons = new ArrayList<>();
        for (Unit unit : units) {
            lessons.add(mDb.getLessonOfUnit(unit));
        }

        for (int i = 0; i < units.size(); i++) {
            removeFromDisk(units.get(i), lessons.get(i));
        }


//        mDb.deleteSection(section);
    }

    private void removeFromDisk(Course course, DatabaseManager.Table tableType) {
        List<Section> sections = mDb.getAllSectionsOfCourse(course);
        for (Section section : sections) {
            removeFromDisk(section);
        }
//        mDb.deleteCourse(course, tableType);
    }
}
