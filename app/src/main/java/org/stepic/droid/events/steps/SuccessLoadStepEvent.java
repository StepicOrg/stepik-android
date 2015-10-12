package org.stepic.droid.events.steps;

import org.stepic.droid.web.StepResponse;

import retrofit.Response;

public class SuccessLoadStepEvent {
    private Response<StepResponse> response;

    public SuccessLoadStepEvent(Response<StepResponse> response) {

        this.response = response;
    }

    public Response<StepResponse> getResponse() {
        return response;
    }
}
