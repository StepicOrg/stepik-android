package org.stepic.droid.ui.dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.stepic.droid.R;
import org.stepic.droid.base.App;
import org.stepic.droid.util.ProgressHelper;
import org.stepic.droid.web.Api;

import javax.inject.Inject;

import butterknife.BindString;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RemindPasswordDialogFragment extends DialogFragment {

    private static final String ERROR_TEXT_KEY = "Error_Text_Key";

    @Inject
    Api api;

    public static RemindPasswordDialogFragment newInstance() {
        return new RemindPasswordDialogFragment();
    }

    private TextInputLayout emailTextWrapper;
    private LoadingProgressDialog progressLogin;
    private View rootView;

    @BindString(R.string.email_wrong)
    String emailWrong;


    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        App.Companion.component().inject(this);

        @SuppressLint("InflateParams") //it is dialog and it shoud not have any parent
        View v = ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.view_remind_password, null, false);
        emailTextWrapper = ButterKnife.findById(v, R.id.emailViewWrapper);
        rootView = ButterKnife.findById(v, R.id.root_view_dialog);
        rootView.requestFocus();

        progressLogin = new LoadingProgressDialog(getContext());


        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.StepikTheme_15_LoginDialog);
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
                setButtonState(b);
                b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        sendEmail(alertDialog);
                    }
                });
            }
        });

        if (emailTextWrapper.getEditText() != null) {
            emailTextWrapper.getEditText().setOnEditorActionListener(new TextView.OnEditorActionListener() {
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

            emailTextWrapper.getEditText().setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        hideError(emailTextWrapper);
                    }
                }
            });
        }

        ButterKnife.bind(this, alertDialog);

        if (savedInstanceState != null) {
            String errorText = savedInstanceState.getString(ERROR_TEXT_KEY);
            if (errorText != null) {
                showError(emailTextWrapper, errorText);
            }
        }

        return alertDialog;
    }

    private void sendEmail(final AlertDialog alertDialog) {
        ProgressHelper.activate(progressLogin);
        safetyHideKeypad(emailTextWrapper.getEditText());
        String email;
        try {
            email = emailTextWrapper.getEditText().getText().toString().trim();
        } catch (NullPointerException e) {
            ProgressHelper.dismiss(progressLogin);
            return;
        }
        if (!email.isEmpty()) {
            api.remindPassword(email).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    ProgressHelper.dismiss(progressLogin);
                    okhttp3.Response rawResponse = response.raw();
                    if (rawResponse.priorResponse() != null && rawResponse.priorResponse().code() == 302) {
                        alertDialog.dismiss();
                        Toast.makeText(getContext(), R.string.email_sent, Toast.LENGTH_SHORT).show();
                    } else if (response.code() == 200) {
                        if (rootView != null)
                            rootView.requestFocus();
                        showError(emailTextWrapper, emailWrong);
                    } else {
                        final Context context = getContext();
                        if (context != null) {
                            Toast.makeText(context, R.string.connectionProblems, Toast.LENGTH_SHORT).show();
                        }
                    }

                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    ProgressHelper.dismiss(progressLogin);

                    final Context context = getContext();
                    if (context != null) {
                        Toast.makeText(context, R.string.connectionProblems, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
    private void setButtonState(final Button button) {
        EditText email = emailTextWrapper.getEditText();
        if (email != null) {
            setSubmitButtonState(button, email.getText().toString());
            email.addTextChangedListener(new TextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    setSubmitButtonState(button, s.toString());
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void setSubmitButtonState(final Button button, String text) {
        button.setEnabled(!(text.trim().length() == 0));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (emailTextWrapper != null && emailTextWrapper.getError() != null)
            outState.putString(ERROR_TEXT_KEY, emailTextWrapper.getError().toString());

    }

    @Override
    public void onDestroyView() {
        if (emailTextWrapper != null && emailTextWrapper.getEditText() != null) {
            emailTextWrapper.getEditText().setOnFocusChangeListener(null);
            emailTextWrapper.getEditText().setOnEditorActionListener(null);
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
            InputMethodManager imm = (InputMethodManager) App.Companion.getAppContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

}
