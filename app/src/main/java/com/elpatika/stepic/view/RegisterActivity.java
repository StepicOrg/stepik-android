package com.elpatika.stepic.view;

import android.os.Bundle;

import com.elpatika.stepic.R;
import com.elpatika.stepic.base.BaseFragmentActivity;


public class RegisterActivity extends BaseFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_register);
        overridePendingTransition(R.anim.slide_in_from_bottom, R.anim.no_transition);
    }
}
