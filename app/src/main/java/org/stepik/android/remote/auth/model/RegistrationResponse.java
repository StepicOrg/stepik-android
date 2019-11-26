package org.stepik.android.remote.auth.model;

import org.jetbrains.annotations.Nullable;

public class RegistrationResponse {
    String[] password;
    String[] last_name;
    String[] email;
    String[] first_name;

    @Nullable
    public String[] getPassword() {
        return password;
    }

    @Nullable
    public String[] getLast_name() {
        return last_name;
    }

    @Nullable
    public String[] getEmail() {
        return email;
    }

    @Nullable
    public String[] getFirst_name() {
        return first_name;
    }
}
