package com.elpatika.stepic.core;

import com.elpatika.stepic.web.AuthenticationStepicResponse;
import com.google.inject.Singleton;

@Singleton
public class TemporaryResponse {
    private static AuthenticationStepicResponse mAuth;
    public static AuthenticationStepicResponse get(){
        return mAuth;
    }

    public static void set (AuthenticationStepicResponse resp){
        mAuth = resp;
    }
}
