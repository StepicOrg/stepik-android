package org.stepic.droid.events.profile;

import org.stepic.droid.model.Profile;

public class ProfileCanBeShownEvent {
    Profile profile;

    public ProfileCanBeShownEvent(Profile profile) {
        this.profile = profile;
    }

    public Profile getProfile() {
        return profile;
    }
}

