package org.stepic.droid.presenters.course_joiner;

import com.squareup.otto.Bus;

import org.jetbrains.annotations.NotNull;
import org.stepic.droid.base.MainApplication;
import org.stepic.droid.concurrency.tasks.UpdateCourseTask;
import org.stepic.droid.events.joining_course.FailJoinEvent;
import org.stepic.droid.events.joining_course.SuccessJoinEvent;
import org.stepic.droid.model.Course;
import org.stepic.droid.preferences.SharedPreferenceHelper;
import org.stepic.droid.store.operations.DatabaseFacade;
import org.stepic.droid.view.abstraction.CourseJoinView;
import org.stepic.droid.web.AuthenticationStepicResponse;
import org.stepic.droid.web.IApi;

import java.net.HttpURLConnection;
import java.util.concurrent.ThreadPoolExecutor;

import javax.inject.Inject;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

public class CourseJoinerPresenterImpl implements CourseJoinerPresenter {

    CourseJoinView view;

    @Inject
    SharedPreferenceHelper mSharedPreferenceHelper;

    @Inject
    IApi api;

    @Inject
    ThreadPoolExecutor mThreadPoolExecutor;

    @Inject
    Bus bus;

    public CourseJoinerPresenterImpl() {
        MainApplication.component().inject(this);
    }

    @Override
    public void onStart(CourseJoinView view) {
        this.view = view;
    }

    @Override
    public void onStop() {
        view = null;
    }

    @Override
    public void joinCourse(@NotNull final Course mCourse) {
        if (isViewAttached()) {
            view.setEnabledJoinButton(false);
        }
        AuthenticationStepicResponse response = mSharedPreferenceHelper.getAuthResponseFromStore();
        if (response != null) {
            if (isViewAttached()) {
                view.showProgress();
            }
            api.tryJoinCourse(mCourse).enqueue(new Callback<Void>() {
                private final Course localCopy = mCourse;

                @Override
                public void onResponse(Response<Void> response, Retrofit retrofit) {
                    if (response.isSuccess()) {

                        localCopy.setEnrollment((int) localCopy.getCourseId());

                        UpdateCourseTask updateCourseTask = new UpdateCourseTask(DatabaseFacade.Table.enrolled, localCopy);
                        updateCourseTask.executeOnExecutor(mThreadPoolExecutor);

                        UpdateCourseTask updateCourseFeaturedTask = new UpdateCourseTask(DatabaseFacade.Table.featured, localCopy);
                        updateCourseFeaturedTask.executeOnExecutor(mThreadPoolExecutor);

                        bus.post(new SuccessJoinEvent(localCopy));

                    } else {
                        if (isViewAttached()) {
                            view.onFailJoin(new FailJoinEvent(response.code()));
                        }
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    if (isViewAttached()) {
                        view.onFailJoin(new FailJoinEvent());
                    }
                }
            });
        } else {
            if (isViewAttached()) {
                view.onFailJoin(new FailJoinEvent(HttpURLConnection.HTTP_UNAUTHORIZED));
            }
        }
    }

    private boolean isViewAttached() {
        return view != null;
    }
}
