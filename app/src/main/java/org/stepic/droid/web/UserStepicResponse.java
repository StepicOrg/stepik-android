package org.stepic.droid.web;

import org.stepic.droid.model.Meta;
import org.stepic.droid.model.User;

import java.util.List;

public class UserStepicResponse implements IStepicResponse{
    Meta meta;
    List<User> users;

    public Meta getMeta() {
        return meta;
    }

    public List<User> getUsers() {
        return users;
    }
}
