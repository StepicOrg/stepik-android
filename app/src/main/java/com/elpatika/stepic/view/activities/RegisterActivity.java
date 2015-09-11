package com.elpatika.stepic.view.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.elpatika.stepic.R;
import com.elpatika.stepic.base.StepicBaseFragmentActivity;
import com.elpatika.stepic.concurrency.RegistrationTask;
import com.elpatika.stepic.web.SignUpResponse;

import roboguice.inject.InjectView;


public class RegisterActivity extends StepicBaseFragmentActivity {

    @InjectView(R.id.createAccount_button_layout)
    RelativeLayout mCreateAccountButton;

    @InjectView (R.id.actionbar_close_btn)
    View mCloseButton;

    @InjectView (R.id.first_name_reg)
    TextView mFirstNameView;
    @InjectView (R.id.second_name_reg)
    TextView mSecondNameView;
    @InjectView (R.id.email_reg)
    TextView mEmailView;
    @InjectView (R.id.password_reg)
    TextView mPassword;

    @InjectView (R.id.progress)
    ProgressBar mProgressBar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_register);
        overridePendingTransition(R.anim.slide_in_from_bottom, R.anim.no_transition);

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
        overridePendingTransition(R.anim.no_transition, R.anim.slide_out_to_bottom);
    }

    private void createAccount() {
        //todo: create account
        String firstName = mFirstNameView.getText().toString();
        String lastName = mSecondNameView.getText().toString();
        String email = mEmailView.getText().toString().trim();
        String password = mPassword.getText().toString().trim(); //todo: substitute to more safe way
        RegistrationTask registrationTask = new RegistrationTask(this, firstName, lastName, email, password)
        {
            @Override
            public void onSuccess(SignUpResponse result) {

                Toast toast =  Toast.makeText(RegisterActivity.this, "onSuccess", Toast.LENGTH_SHORT);
                toast.show();
            }

            @Override
            public void onException(Exception ex) {

                Toast toast =  Toast.makeText(RegisterActivity.this, "onException", Toast.LENGTH_SHORT);
                toast.show();
            }
        };

        registrationTask.setProgressBar(mProgressBar);
        registrationTask.execute();

    }


}
