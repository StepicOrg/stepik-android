package org.stepic.droid.web;

import androidx.annotation.Nullable;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface StepicRestOAuthService {
    @FormUrlEncoded
    @POST("/oauth2/token/")
    Call<AuthenticationStepikResponse> updateToken(@Field("grant_type") String grantType,
                                                   @Field("refresh_token") String refreshToken);

    @FormUrlEncoded
    @POST("/oauth2/token/")
    Call<AuthenticationStepikResponse> authWithLoginPassword(@Field("grant_type") String grant_type,
                                                             @Field(value = "username", encoded = true) String username,
                                                             @Field(value = "password", encoded = true) String password);


    @FormUrlEncoded
    @POST("/oauth2/token/")
    Call<AuthenticationStepikResponse> getTokenByCode(@Field("grant_type") String grant_type,
                                                      @Field("code") String code,
                                                      @Field("redirect_uri") String redirectUri);

    @FormUrlEncoded
    @POST("/oauth2/social-token/")
    Call<AuthenticationStepikResponse> getTokenByNativeCode(@Field("provider") String providerName,
                                                            @Field("code") String providerCode,
                                                            @Field("grant_type") String grantType,
                                                            @Field("redirect_uri") String redirectUri,
                                                            @Field("code_type") String accessToken,
                                                            @Nullable
                                                            @Field("email") String email);


    @POST("/api/users")
    Call<RegistrationResponse> createAccount(@Body UserRegistrationRequest user);
}