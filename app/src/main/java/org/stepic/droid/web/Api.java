package org.stepic.droid.web;

import androidx.fragment.app.FragmentActivity;

import org.stepic.droid.social.ISocialType;

import retrofit2.Call;


public interface Api {

    enum TokenType {
        social, loginPassword
    }

    /**
     * Max number of  units defined in AppConstants
     */

    void loginWithSocial(FragmentActivity activity, ISocialType type);

    Call<Void> remindPassword(String email);
}
