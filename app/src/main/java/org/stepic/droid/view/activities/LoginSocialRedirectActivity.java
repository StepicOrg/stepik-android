package org.stepic.droid.view.activities;

import android.content.Intent;
import android.os.Bundle;

import org.stepic.droid.R;
import org.stepic.droid.base.FragmentActivityBase;

import butterknife.ButterKnife;

public class LoginSocialRedirectActivity extends FragmentActivityBase {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_social_login);
        overridePendingTransition(org.stepic.droid.R.anim.slide_in_from_end, org.stepic.droid.R.anim.slide_out_to_start);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        int i = 0;
    }
}
