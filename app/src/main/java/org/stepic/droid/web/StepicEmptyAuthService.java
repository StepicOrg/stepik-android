package org.stepic.droid.web;

import retrofit.Call;
import retrofit.http.GET;

public interface StepicEmptyAuthService {

    @GET("/")
    Call<Void> getStepicForFun ();
}
