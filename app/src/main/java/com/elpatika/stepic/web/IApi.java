package com.elpatika.stepic.web;

public interface IApi {
    IResponse authWithLoginPassword (String login, String password);
    IResponse signUp (String firstName, String secondName, String email, String password);

}
