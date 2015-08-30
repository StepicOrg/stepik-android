package com.elpatika.stepic.view;

import android.os.Bundle;
import android.widget.TextView;

import com.elpatika.stepic.R;
import com.elpatika.stepic.base.BaseFragmentActivity;
import com.elpatika.stepic.core.Tempresponse;

import roboguice.inject.InjectView;

public class MainFeedActivity extends BaseFragmentActivity {

    @InjectView (R.id.tempResp)
    TextView mTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main_feed);
        mTextView.setText(Tempresponse.get().toString());
    }
}
