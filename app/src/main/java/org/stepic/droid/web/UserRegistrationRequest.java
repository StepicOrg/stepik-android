package org.stepic.droid.web;

import org.stepic.droid.model.RegistrationUser;

public class UserRegistrationRequest {
    RegistrationUser user;

    public UserRegistrationRequest(RegistrationUser user) {
        this.user = user;
    }
}
