package org.stepic.droid.events.courses;

import org.stepic.droid.store.operations.DatabaseManager;
import org.stepic.droid.web.CoursesStepicResponse;

import retrofit.Response;
import retrofit.Retrofit;

public class SuccessCoursesDownloadEvent extends CourseEventBase {


    private final Response<CoursesStepicResponse> response;
    private final Retrofit retrofit;

    public SuccessCoursesDownloadEvent(DatabaseManager.Table type, Response<CoursesStepicResponse> response, Retrofit retrofit) {
        super(type);
        this.response = response;
        this.retrofit = retrofit;

    }

    public Response<CoursesStepicResponse> getResponse() {
        return response;
    }

    public Retrofit getRetrofit() {
        return retrofit;
    }
}
