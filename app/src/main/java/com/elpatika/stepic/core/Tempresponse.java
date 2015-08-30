package com.elpatika.stepic.core;

import com.elpatika.stepic.web.AuthenticationResponse;
import com.google.inject.Singleton;

@Singleton
public class Tempresponse {
    private static AuthenticationResponse mAuth;
    public static AuthenticationResponse get(){
        return mAuth;
    }

    public static void set (AuthenticationResponse resp){
        mAuth = resp;
    }
}
