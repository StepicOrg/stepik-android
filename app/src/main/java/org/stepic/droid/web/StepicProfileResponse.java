package org.stepic.droid.web;

import org.stepic.droid.model.Profile;
import org.stepic.droid.model.User;

import java.util.List;

public class StepicProfileResponse {
    private List<User> users;
    private List<Profile> profiles;

    public Profile getProfile() {
        if (profiles == null || profiles.size() < 1)
            return null;
        else
            return profiles.get(0);
    }
}
