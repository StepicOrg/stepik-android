package org.stepic.droid.web;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Streaming;

public interface StepicRestOAuthService {
    @FormUrlEncoded
    @POST("/oauth2/token/")
    Call<AuthenticationStepicResponse> updateToken(@Field("grant_type") String grant_type,
                                                   @Field("refresh_token") String refresh_token);

    @FormUrlEncoded
    @POST("/oauth2/token/")
    Call<AuthenticationStepicResponse> authWithLoginPassword(@Field("grant_type") String grant_type,
                                                             @Field(value = "username", encoded = true) String username,
                                                             @Field(value = "password", encoded = true) String password);


    @FormUrlEncoded
    @POST("/oauth2/token/")
    Call<AuthenticationStepicResponse> getTokenByCode(@Field("grant_type") String grant_type,
                                                      @Field("code") String code,
                                                      @Field("redirect_uri") String redirect_uri);

    @FormUrlEncoded
    @POST("/oauth2/social-token/")
    Call<AuthenticationStepicResponse> getTokenByNativeCode(@Field("provider") String providerName,
                                                            @Field("code") String providerCode,
                                                            @Field("grant_type") String grant_type,
                                                            @Field("redirect_uri") String redirect_uri,
                                                            @Field("code_type") String accessToken);


    @POST("/api/users")
    Call<RegistrationResponse> createAccount(@Body UserRegistrationRequest user);

    @Streaming
    @GET("/{updatingPath}")
    Call<ResponseBody> updatingInfo(@Path("updatingPath") String updatingPathRelateBaseUrl);
}