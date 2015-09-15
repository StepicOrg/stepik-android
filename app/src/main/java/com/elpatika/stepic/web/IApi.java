package com.elpatika.stepic.web;

public interface IApi {
    IStepicResponse authWithLoginPassword (String login, String password);

    IStepicResponse signUp (String firstName, String secondName, String email, String password);

}
