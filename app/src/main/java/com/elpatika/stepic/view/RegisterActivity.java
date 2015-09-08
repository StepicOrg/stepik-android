package com.elpatika.stepic.view;

import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.elpatika.stepic.R;
import com.elpatika.stepic.base.BaseFragmentActivity;

import roboguice.inject.InjectView;


public class RegisterActivity extends BaseFragmentActivity {

    @InjectView(R.id.createAccount_button_layout)
    RelativeLayout mCreateAccountButton;

    @InjectView (R.id.actionbar_close_btn)
    View mCloseButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_register);
        overridePendingTransition(R.anim.slide_in_from_bottom, R.anim.no_transition);

        mCreateAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAccount();
            }
        });
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

    private void createAccount() {
        //todo: create account
        Toast toast =  Toast.makeText(this, "Sorry, this function is unimplemented", Toast.LENGTH_SHORT);
        toast.show();
    }



}
