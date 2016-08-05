package org.stepic.droid.web;

import com.squareup.okhttp.ResponseBody;

import retrofit.Call;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;
import retrofit.http.Streaming;

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

    @POST("/oauth2/social-token/")
    Call<AuthenticationStepicResponse> getTokenByNativeCode(@Query("provider") String providerName,
                                                            @Query("code") String providerCode,
                                                            @Query("grant_type") String grant_type,
                                                            @Query("redirect_uri") String redirect_uri);


    @POST("/api/users")
    Call<RegistrationResponse> createAccount(@Body UserRegistrationRequest user);

    @Streaming
    @GET("/{updatingPath}")
    Call<ResponseBody> updatingInfo(@Path("updatingPath") String updatingPathRelateBaseUrl);
}