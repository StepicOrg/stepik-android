package org.stepic.droid.web;

import org.stepic.droid.web.model.desk.DeskRequestWrapper;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface StepikDeskEmptyAuthService {

    @POST("/api/v2/requests.json")
    Call<Void> sendFeedback(@Body DeskRequestWrapper wrapper);
}
