package org.stepic.droid.web;

import org.stepic.droid.util.AppConstants;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface EmptyAuthService {

    @GET("/")
    Call<Void> getStepicForFun(@Header("Accept-Language") String lang);

    @FormUrlEncoded
    @POST("accounts/password/reset/")
    Call<Void> remindPassword(@Field(value = "email", encoded = true) String email);

    @GET("api/stepics/1")
    Call<StepicProfileResponse> getUserProfileWithCookie(@Header(AppConstants.refererHeaderName) String referer,
                                                         @Header(AppConstants.cookieHeaderName) String cookies,
                                                         @Header(AppConstants.csrfTokenHeaderName) String csrfToken);


}
