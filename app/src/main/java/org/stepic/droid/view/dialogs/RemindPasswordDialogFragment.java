package org.stepic.droid.view.dialogs;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.stepic.droid.R;
import org.stepic.droid.base.MainApplication;
import org.stepic.droid.util.ProgressHelper;
import org.stepic.droid.web.IApi;

import javax.inject.Inject;

import butterknife.BindString;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

public class RemindPasswordDialogFragment extends DialogFragment {

    private static final String ERROR_TEXT_KEY = "Error_Text_Key";

    @Inject
    IApi mApi;

    public static RemindPasswordDialogFragment newInstance() {
        return new RemindPasswordDialogFragment();
    }

    private TextInputLayout mEmailTextWrapper;
    private ProgressDialog mProgressLogin;
    private View mRootView;

    @BindString(R.string.email_wrong)
    String mEmailWrong;


    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MainApplication.component().inject(this);


        View v = ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.view_remind_password, null, false);
        mEmailTextWrapper = ButterKnife.findById(v, R.id.email_reg_wrapper);
        mRootView = ButterKnife.findById(v, R.id.root_view_dialog);
        mRootView.requestFocus();

        mProgressLogin = new ProgressDialog(getContext());
        mProgressLogin.setTitle(getString(R.string.loading));
        mProgressLogin.setMessage(getString(R.string.loading_message));
        mProgressLogin.setCancelable(false);


        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.MyAlertDialogStyle);
        builder.setTitle(R.string.remind_password)
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

        if (mEmailTextWrapper.getEditText() != null) {
            mEmailTextWrapper.getEditText().setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    boolean handled = false;
                    if (actionId == EditorInfo.IME_ACTION_SEND) {
                        sendEmail(alertDialog);
                        handled = true;
                    }
                    return handled;
                }
            });

            mEmailTextWrapper.getEditText().setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        hideError(mEmailTextWrapper);
                    }
                }
            });
        }

        ButterKnife.bind(this, alertDialog);

        if (savedInstanceState != null) {
            String errorText = savedInstanceState.getString(ERROR_TEXT_KEY);
            if (errorText != null) {
                showError(mEmailTextWrapper, errorText);
            }
        }

        return alertDialog;
    }

    private void sendEmail(final AlertDialog alertDialog) {
        ProgressHelper.activate(mProgressLogin);
        safetyHideKeypad(mEmailTextWrapper.getEditText());
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
                        Toast.makeText(getContext(), R.string.email_sent, Toast.LENGTH_SHORT).show();
                    } else if (response.code() == 200) {
                        if (mRootView != null)
                            mRootView.requestFocus();
                        showError(mEmailTextWrapper, mEmailWrong);
                    } else {
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

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mEmailTextWrapper != null && mEmailTextWrapper.getError() != null)
            outState.putString(ERROR_TEXT_KEY, mEmailTextWrapper.getError().toString());

    }

    @Override
    public void onDestroyView() {
        if (mEmailTextWrapper != null && mEmailTextWrapper.getEditText() != null) {
            mEmailTextWrapper.getEditText().setOnFocusChangeListener(null);
            mEmailTextWrapper.getEditText().setOnEditorActionListener(null);
        }
        super.onDestroyView();
    }

    private void hideError(TextInputLayout textInputLayout) {
        if (textInputLayout != null) {
            textInputLayout.setError("");
            textInputLayout.setErrorEnabled(false);
        }
    }

    private void showError(TextInputLayout textInputLayout, String errorText) {
        if (textInputLayout != null) {
            textInputLayout.setErrorEnabled(true);
            textInputLayout.setError(errorText);
        }
    }

    private void safetyHideKeypad(TextView view) {
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) MainApplication.getAppContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

}
