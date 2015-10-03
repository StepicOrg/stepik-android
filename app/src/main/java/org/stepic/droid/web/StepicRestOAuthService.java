package org.stepic.droid.web;

import retrofit.Call;
import retrofit.http.POST;
import retrofit.http.Query;

public interface StepicRestOAuthService {
    @POST("/oauth2/token/")
    Call<AuthenticationStepicResponse> updateToken(@Query("grant_type") String grant_type,
                                                   @Query("refresh_token") String refresh_token);

    @POST("/oauth2/token/")
    Call<AuthenticationStepicResponse> authWithLoginPassword(@Query("grant_type") String grant_type,
                                                             @Query("username") String username,
                                                             @Query("password") String password);

}
