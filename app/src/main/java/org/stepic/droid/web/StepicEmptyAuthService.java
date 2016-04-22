package org.stepic.droid.web;

import retrofit.Call;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.POST;

public interface StepicEmptyAuthService {

    @GET("/")
    Call<Void> getStepicForFun (@Header("Accept-Language") String lang);

    @FormUrlEncoded
    @POST("accounts/password/reset/")
    Call<Void> remindPassword(@Field(value = "email", encoded = true) String email);

}
