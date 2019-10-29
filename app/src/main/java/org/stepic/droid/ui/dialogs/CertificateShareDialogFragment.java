package org.stepic.droid.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import org.jetbrains.annotations.NotNull;
import org.stepic.droid.model.CertificateViewItem;

public class CertificateShareDialogFragment extends DialogFragment {

    public static final String TAG = "certificate_share_dialog";
    private static final String CERTIFICATE_VIEW_ITEM_KEY = "certificateViewItemKey";

    public static DialogFragment newInstance(@NotNull CertificateViewItem viewItem) {
        Bundle args = new Bundle();
        args.putParcelable(CERTIFICATE_VIEW_ITEM_KEY, viewItem);
        DialogFragment fragment = new CertificateShareDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        CertificateViewItem viewItem = getArguments().getParcelable(CERTIFICATE_VIEW_ITEM_KEY);
        return new CertificateShareDialog(getContext(), viewItem);
    }

}
