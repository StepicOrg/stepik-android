package org.stepic.droid.core;

public interface ILoginManager {
    void login(String rawLogin, String rawPassword, ProgressHandler progressHandler, ActivityFinisher finisher);

    void loginWithCode(String code, ProgressHandler progressHandler, ActivityFinisher finisher);
}
