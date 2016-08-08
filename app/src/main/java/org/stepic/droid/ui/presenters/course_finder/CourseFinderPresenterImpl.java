package org.stepic.droid.ui.presenters.course_finder;

import org.stepic.droid.base.MainApplication;
import org.stepic.droid.concurrency.IMainHandler;
import org.stepic.droid.events.courses.CourseCantLoadEvent;
import org.stepic.droid.events.courses.CourseFoundEvent;
import org.stepic.droid.events.courses.CourseUnavailableForUserEvent;
import org.stepic.droid.model.Course;
import org.stepic.droid.store.operations.DatabaseFacade;
import org.stepic.droid.ui.abstraction.LoadCourseView;
import org.stepic.droid.web.CoursesStepicResponse;
import org.stepic.droid.web.IApi;

import java.util.concurrent.ThreadPoolExecutor;

import javax.inject.Inject;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

public class CourseFinderPresenterImpl implements CourseFinderPresenter {

    LoadCourseView view;

    @Inject
    ThreadPoolExecutor mThreadPoolExecutor;

    @Inject
    DatabaseFacade mDatabaseFacade;

    @Inject
    IApi api;

    @Inject
    IMainHandler mainHandler;

    @Inject
    public CourseFinderPresenterImpl() {
        MainApplication.component().inject(this);
    }

    @Override
    public void onStart(LoadCourseView view) {
        this.view = view;
    }

    @Override
    public void onDestroy() {
        view = null;
    }

    @Override
    public void findCourseById(final long courseId) {
        mThreadPoolExecutor.execute(new Runnable() {
            @Override
            public void run() {
                Course course = mDatabaseFacade.getCourseById(courseId, DatabaseFacade.Table.featured);
                if (course == null) {
                    course = mDatabaseFacade.getCourseById(courseId, DatabaseFacade.Table.enrolled);
                }

                final Course finalCourse = course;
                if (finalCourse != null) {
                    if (isViewAttached()) {
                        mainHandler.post(new Function0<Unit>() {
                            @Override
                            public Unit invoke() {
                                view.onCourseFound(new CourseFoundEvent(finalCourse));
                                return Unit.INSTANCE;
                            }
                        });
                    }
                } else {
                    api.getCourse(courseId).enqueue(new Callback<CoursesStepicResponse>() {
                        @Override
                        public void onResponse(Response<CoursesStepicResponse> response, Retrofit retrofit) {
                            if (response.isSuccess() && !response.body().getCourses().isEmpty()) {
                                if (isViewAttached()) {
                                    view.onCourseFound(new CourseFoundEvent(response.body().getCourses().get(0)));
                                }
                            } else {
                                if (isViewAttached()) {
                                    view.onCourseUnavailable(new CourseUnavailableForUserEvent(courseId));
                                }
                            }
                        }

                        @Override
                        public void onFailure(Throwable t) {
                            if (isViewAttached()) {
                                view.onInternetFailWhenCourseIsTriedToLoad(new CourseCantLoadEvent());
                            }
                        }
                    });
                }
            }
        });
    }

    private boolean isViewAttached() {
        return view != null;
    }

}
