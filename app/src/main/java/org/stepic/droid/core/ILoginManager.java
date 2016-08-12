package org.stepic.droid.core;

import org.stepic.droid.social.SocialManager;
import org.stepic.droid.ui.util.FailLoginSupplementaryHandler;

public interface ILoginManager {
    void login(String rawLogin, String rawPassword, ProgressHandler progressHandler, ActivityFinisher finisher);

    void loginWithCode(String code, ProgressHandler progressHandler, ActivityFinisher finisher);

    void loginWithNativeProviderCode (String nativeCode, SocialManager.SocialType type, ProgressHandler progressHandler, ActivityFinisher finisher, FailLoginSupplementaryHandler failLoginSupplementaryHandler);
}
