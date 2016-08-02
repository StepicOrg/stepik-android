package org.stepic.droid.view.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

public class CertificateShareDialogFragment extends DialogFragment {
    public static DialogFragment newInstance() {
        
        Bundle args = new Bundle();
        
        DialogFragment fragment = new CertificateShareDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new CertificateShareDialog(getContext());
    }

}
