package org.stepic.droid.events.instructors;

import org.stepic.droid.model.Course;
import org.stepic.droid.web.UserStepicResponse;

import retrofit.Response;
import retrofit.Retrofit;

public class OnResponseLoadingInstructorsEvent extends InstructorsBaseEvent {
    private final Response<UserStepicResponse> response;
    private final Retrofit retrofit;

    public OnResponseLoadingInstructorsEvent(Course mCourse, Response<UserStepicResponse> response, Retrofit retrofit) {
        super(mCourse);
        this.response = response;
        this.retrofit = retrofit;
    }

    public Response<UserStepicResponse> getResponse() {
        return response;
    }

    public Retrofit getRetrofit() {
        return retrofit;
    }
}
