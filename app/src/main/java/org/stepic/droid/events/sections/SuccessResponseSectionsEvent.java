package org.stepic.droid.events.sections;

import org.stepic.droid.model.Course;
import org.stepic.droid.web.SectionsStepicResponse;

import retrofit.Response;
import retrofit.Retrofit;

public class SuccessResponseSectionsEvent {
    private final Course courseOfSection;
    private final Response<SectionsStepicResponse> response;
    private final Retrofit retrofit;


    public SuccessResponseSectionsEvent(Course courseOfSection, Response<SectionsStepicResponse> response, Retrofit retrofit) {
        this.courseOfSection = courseOfSection;
        this.response = response;
        this.retrofit = retrofit;
    }

    public Course getCourseOfSection() {
        return courseOfSection;
    }

    public Response<SectionsStepicResponse> getResponse() {
        return response;
    }

    public Retrofit getRetrofit() {
        return retrofit;
    }
}
