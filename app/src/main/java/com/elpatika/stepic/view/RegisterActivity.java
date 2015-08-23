package com.elpatika.stepic.view;

import android.os.Bundle;

import com.elpatika.stepic.R;
import com.elpatika.stepic.base.BaseFragmentActivity;

/**
 * Created by kirillmakarov on 24.08.15.
 */
public class RegisterActivity extends BaseFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_register);
        overridePendingTransition(R.anim.slide_in_from_bottom, R.anim.no_transition);
    }
}
