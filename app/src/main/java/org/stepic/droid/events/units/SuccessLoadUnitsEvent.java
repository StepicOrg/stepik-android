package org.stepic.droid.events.units;

import org.stepic.droid.model.Section;
import org.stepic.droid.web.UnitStepicResponse;

import retrofit.Response;
import retrofit.Retrofit;

public class SuccessLoadUnitsEvent {
    private final Section section;
    private final Response<UnitStepicResponse> response;
    private final Retrofit retrofit;

    public SuccessLoadUnitsEvent(Section section, Response<UnitStepicResponse> response, Retrofit retrofit) {

        this.section = section;
        this.response = response;
        this.retrofit = retrofit;
    }

    public Section getSection() {
        return section;
    }

    public Response<UnitStepicResponse> getResponse() {
        return response;
    }

    public Retrofit getRetrofit() {
        return retrofit;
    }
}
