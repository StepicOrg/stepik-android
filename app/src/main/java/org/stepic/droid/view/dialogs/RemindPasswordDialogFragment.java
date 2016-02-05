package org.stepic.droid.view.dialogs;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.stepic.droid.R;
import org.stepic.droid.base.MainApplication;
import org.stepic.droid.util.ProgressHelper;
import org.stepic.droid.web.IApi;

import javax.inject.Inject;

import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

public class RemindPasswordDialogFragment extends DialogFragment {

    @Inject
    IApi mApi;

    public static RemindPasswordDialogFragment newInstance() {
        return new RemindPasswordDialogFragment();
    }

    private TextInputLayout mEmailTextWrapper;
    private ProgressDialog mProgressLogin;
    private View mRootView;

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MainApplication.component().inject(this);

        View v = ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.view_remind_password, null, false);
        mEmailTextWrapper = ButterKnife.findById(v, R.id.email_reg_wrapper);
//        mProgressLogin = ButterKnife.findById(v, R.id.progress_bar);
        mRootView = ButterKnife.findById(v, R.id.root_view);
        mRootView.requestFocus();

        mProgressLogin = new ProgressDialog(getContext());
        mProgressLogin.setTitle(getString(R.string.loading));
        mProgressLogin.setMessage(getString(R.string.loading_message));
        mProgressLogin.setCancelable(false);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.MyAlertDialogStyle);
        builder.setTitle(R.string.reset_password)
                .setView(v)
                .setPositiveButton(R.string.send, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setNegativeButton(R.string.cancel, null);
        final AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button b = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        sendEmail(alertDialog);
                    }
                });
            }
        });

        return alertDialog;
    }

    private void sendEmail(final AlertDialog alertDialog) {
        ProgressHelper.activate(mProgressLogin);
        String email;
        try {
            email = mEmailTextWrapper.getEditText().getText().toString().trim();
        } catch (NullPointerException e) {
            ProgressHelper.dismiss(mProgressLogin);
            return;
        }
        if (!email.isEmpty()) {
            mApi.remindPassword(email).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Response<Void> response, Retrofit retrofit) {
                    ProgressHelper.dismiss(mProgressLogin);

                    com.squareup.okhttp.Response rawResponse = response.raw();
                    if (rawResponse.priorResponse() != null && rawResponse.priorResponse().code() == 302) {
                        alertDialog.dismiss();
                        Toast.makeText(getContext(), "SUCCESS", Toast.LENGTH_SHORT).show();
                    } else if (response.code() == 200) {
                        Toast.makeText(getContext(), "USER IS NOT FOUND", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(getContext(), R.string.failLoginConnectionProblems, Toast.LENGTH_SHORT).show();
                    }

                }

                @Override
                public void onFailure(Throwable t) {
                    ProgressHelper.dismiss(mProgressLogin);
                    Toast.makeText(getContext(), R.string.failLoginConnectionProblems, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
