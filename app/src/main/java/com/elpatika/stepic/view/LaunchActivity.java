package com.elpatika.stepic.view;

import android.os.Bundle;

import com.elpatika.stepic.R;
import com.elpatika.stepic.base.BaseFragmentActivity;

/**
 * Created by kirillmakarov on 23.08.15.
 */
public class LaunchActivity extends BaseFragmentActivity {

    public static final String OVERRIDE_ANIMATION_FLAG = "override_animation_flag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_launch);
    }
}
