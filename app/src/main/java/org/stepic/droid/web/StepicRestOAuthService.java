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
                                                             @Query(value = "username", encoded = true) String username,
                                                             @Query(value = "password", encoded = true) String password);


    @POST("/oauth2/token/")
    Call<AuthenticationStepicResponse> getTokenByCode(@Query("grant_type") String grant_type,
                                                      @Query("code") String code,
                                                      @Query("redirect_uri") String redirect_uri);

}