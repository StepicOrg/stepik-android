package org.stepic.droid.events.sections;

import org.stepic.droid.model.Course;
import org.stepic.droid.web.SectionsStepicResponse;

import retrofit.Response;
import retrofit.Retrofit;

public class SuccessResponseSectionsEvent extends SectionBaseEvent {
    private final Response<SectionsStepicResponse> response;
    private final Retrofit retrofit;


    public SuccessResponseSectionsEvent(Course courseOfSection, Response<SectionsStepicResponse> response, Retrofit retrofit) {
        super(courseOfSection);
        this.response = response;
        this.retrofit = retrofit;
    }


    public Response<SectionsStepicResponse> getResponse() {
        return response;
    }

    public Retrofit getRetrofit() {
        return retrofit;
    }
}
