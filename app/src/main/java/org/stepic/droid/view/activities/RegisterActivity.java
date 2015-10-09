package org.stepic.droid.view.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.stepic.droid.base.FragmentActivityBase;

import butterknife.Bind;
import butterknife.ButterKnife;


public class RegisterActivity extends FragmentActivityBase {
    private static final String TAG = "register_activity";


    @Bind(org.stepic.droid.R.id.createAccount_button_layout)
    RelativeLayout mCreateAccountButton;

    @Bind (org.stepic.droid.R.id.actionbar_close_btn)
    View mCloseButton;

    @Bind (org.stepic.droid.R.id.first_name_reg)
    TextView mFirstNameView;
    @Bind (org.stepic.droid.R.id.second_name_reg)
    TextView mSecondNameView;
    @Bind (org.stepic.droid.R.id.email_reg)
    TextView mEmailView;
    @Bind (org.stepic.droid.R.id.password_reg)
    TextView mPassword;

    @Bind (org.stepic.droid.R.id.progress)
    ProgressBar mProgressBar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");

        setContentView(org.stepic.droid.R.layout.activity_register);
        ButterKnife.bind(this);
        overridePendingTransition(org.stepic.droid.R.anim.slide_in_from_bottom, org.stepic.droid.R.anim.no_transition);

        hideSoftKeypad();

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
        overridePendingTransition(org.stepic.droid.R.anim.no_transition, org.stepic.droid.R.anim.slide_out_to_bottom);
    }

    private void createAccount() {
        //todo: create account
        String firstName = mFirstNameView.getText().toString();
        String lastName = mSecondNameView.getText().toString();
        String email = mEmailView.getText().toString().trim();
        String password = mPassword.getText().toString().trim(); //todo: substitute to more safe way

        // FIXME: 04.10.15 Make registration request

    }


}
