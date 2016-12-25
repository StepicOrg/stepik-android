package org.stepic.droid.core;

import org.jetbrains.annotations.Nullable;
import org.stepic.droid.model.Course;
import org.stepic.droid.social.SocialManager;
import org.stepic.droid.ui.util.FailLoginSupplementaryHandler;

public interface LoginManager {

    void login(String rawLogin, String rawPassword, ProgressHandler progressHandler, ActivityFinisher finisher, @Nullable Course course);

    void loginWithCode(String code, ProgressHandler progressHandler, ActivityFinisher finisher, @Nullable Course course);

    void loginWithNativeProviderCode (String nativeCode, SocialManager.SocialType type, ProgressHandler progressHandler, ActivityFinisher finisher, FailLoginSupplementaryHandler failLoginSupplementaryHandler, @Nullable Course course);
}
