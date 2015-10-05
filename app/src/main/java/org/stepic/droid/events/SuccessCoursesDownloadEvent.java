package org.stepic.droid.events;

import org.stepic.droid.web.CoursesStepicResponse;

import retrofit.Response;
import retrofit.Retrofit;

public class SuccessCoursesDownloadEvent {


    private final Response<CoursesStepicResponse> response;
    private final Retrofit retrofit;

    public SuccessCoursesDownloadEvent(Response<CoursesStepicResponse> response, Retrofit retrofit) {
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
