package org.stepic.droid.web;


import retrofit.Call;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;

public interface StepicZendeskEmptyAuthService {

    @GET("/hc/ru/requests/new")
    Call<Void> getZendeskForFun();

    @FormUrlEncoded
    @POST("/hc/ru/requests/")
    Call<Void> sendFeedback(@Field(value = "request[subject]", encoded = true) String requestSubject,
                            @Field(value = "request[anonymous_requester_email]", encoded = true) String email,
                            @Field(value = "request[custom_fields][24562019]", encoded = true) String systemInfo,
                            @Field(value = "request[description]", encoded = true) String description);
}
