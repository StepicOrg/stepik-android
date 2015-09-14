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

import butterknife.Bind;
import butterknife.ButterKnife;


public class RegisterActivity extends StepicBaseFragmentActivity {

    @Bind(R.id.createAccount_button_layout)
    RelativeLayout mCreateAccountButton;

    @Bind (R.id.actionbar_close_btn)
    View mCloseButton;

    @Bind (R.id.first_name_reg)
    TextView mFirstNameView;
    @Bind (R.id.second_name_reg)
    TextView mSecondNameView;
    @Bind (R.id.email_reg)
    TextView mEmailView;
    @Bind (R.id.password_reg)
    TextView mPassword;

    @Bind (R.id.progress)
    ProgressBar mProgressBar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);
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
            protected void onPostExecute(SignUpResponse signUpResponse) {
                Toast toast =  Toast.makeText(RegisterActivity.this, "onSuccess", Toast.LENGTH_SHORT);
                toast.show();
            }
        };

        registrationTask.setProgressBar(mProgressBar);
        registrationTask.execute();

    }


}
