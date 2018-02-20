package org.stepic.droid.web;

import org.stepic.droid.web.model.desk.DeskRequestWrapper;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface StepikDeskEmptyAuthService {

    @POST("/api/v2/requests.json")
    Call<Void> sendFeedback(@Body DeskRequestWrapper wrapper);
}
