package org.stepic.droid.view.activities;

import android.content.Intent;
import android.os.Bundle;

import com.yandex.metrica.YandexMetrica;

import org.stepic.droid.R;
import org.stepic.droid.base.FragmentActivityBase;
import org.stepic.droid.preferences.SharedPreferenceHelper;
import org.stepic.droid.util.AppConstants;
import org.stepic.droid.web.AuthenticationStepicResponse;

import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

public class LoginSocialRedirectActivity extends FragmentActivityBase {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_social_login);
        overridePendingTransition(org.stepic.droid.R.anim.slide_in_from_end, org.stepic.droid.R.anim.slide_out_to_start);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        String code = intent.getData().getQueryParameter("code");

        //TODO: CHANGE THIS DUPLICATE CODE
        //FIXME: show big progress on other screen (for example LoginActivity.java)
        mShell.getApi().authWithCode(code).enqueue(new Callback<AuthenticationStepicResponse>() {
            @Override
            public void onResponse(Response<AuthenticationStepicResponse> response, Retrofit retrofit) {
                SharedPreferenceHelper preferenceHelper = mShell.getSharedPreferenceHelper();
                AuthenticationStepicResponse authStepic = response.body();
                preferenceHelper.storeAuthInfo(authStepic);


                if (authStepic != null) {
                    YandexMetrica.reportEvent(AppConstants.METRICA_SUCCESS_LOGIN);
                    onUserLoginSuccess();
                } else {
                    YandexMetrica.reportEvent(AppConstants.METRICA_FAIL_LOGIN);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                finish();
            }
        });
    }


    private void onUserLoginSuccess() {
        mShell.getScreenProvider().showMainFeed(this);
        finish();
    }


}
