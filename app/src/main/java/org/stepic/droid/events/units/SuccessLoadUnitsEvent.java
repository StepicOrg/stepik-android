package org.stepic.droid.events.units;

import org.stepic.droid.model.Section;
import org.stepic.droid.web.UnitStepicResponse;

import retrofit.Response;
import retrofit.Retrofit;

public class SuccessLoadUnitsEvent {
    private final Section mSection;
    private final Response<UnitStepicResponse> response;
    private final Retrofit retrofit;

    public SuccessLoadUnitsEvent(Section mSection, Response<UnitStepicResponse> response, Retrofit retrofit) {

        this.mSection = mSection;
        this.response = response;
        this.retrofit = retrofit;
    }

    public Section getmSection() {
        return mSection;
    }

    public Response<UnitStepicResponse> getResponse() {
        return response;
    }

    public Retrofit getRetrofit() {
        return retrofit;
    }
}
