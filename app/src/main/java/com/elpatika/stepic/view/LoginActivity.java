package com.elpatika.stepic.view;

import android.os.Bundle;
import android.view.View;

import com.elpatika.stepic.R;
import com.elpatika.stepic.base.BaseFragmentActivity;

import roboguice.inject.InjectView;

public class LoginActivity extends BaseFragmentActivity {

    @InjectView (R.id.actionbar_close_btn)
    View mCloseButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);
        overridePendingTransition(R.anim.slide_in_from_bottom, R.anim.no_transition);

        hideSoftKeypad();

        mCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.no_transition, R.anim.slide_out_to_bottom);
    }
}
