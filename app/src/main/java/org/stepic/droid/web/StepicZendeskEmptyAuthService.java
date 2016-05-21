package org.stepic.droid.web;
import retrofit.Call;
import retrofit.http.POST;
import retrofit.http.Query;

public interface StepicZendeskEmptyAuthService {

    @POST("/hc/en-us/requests/")
    Call<Void> sendFeedback(@Query(value = "request[subject]", encoded = true) String requestSubject,
                            @Query(value = "request[anonymous_requester_email]", encoded = true) String email,
                            @Query(value = "request[custom_fields][24562019]", encoded = true) String systemInfo,
                            @Query(value = "request[description]", encoded = true) String description,
                            @Query(value = "request[custom_fields][24562009]", encoded = true) String link);
}
