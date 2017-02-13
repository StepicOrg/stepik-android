package org.stepic.droid.web;

import okhttp3.ResponseBody;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Streaming;

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
                                                            @Query("redirect_uri") String redirect_uri,
                                                            @Query("code_type") String accessToken);


    @POST("/api/users")
    Call<RegistrationResponse> createAccount(@Body UserRegistrationRequest user);

    @Streaming
    @GET("/{updatingPath}")
    Call<ResponseBody> updatingInfo(@Path("updatingPath") String updatingPathRelateBaseUrl);
}