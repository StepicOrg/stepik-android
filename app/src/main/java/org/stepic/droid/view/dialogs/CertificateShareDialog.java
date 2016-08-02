package org.stepic.droid.view.dialogs;

import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.WindowManager;

import org.stepic.droid.R;

public class CertificateShareDialog extends BottomSheetDialogFragment {

    public static DialogFragment newInstance() {

        Bundle args = new Bundle();

        CertificateShareDialog fragment = new CertificateShareDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void setupDialog(Dialog dialog, int style) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && dialog.getWindow() != null) {
            dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        }
        super.setupDialog(dialog, style);
        View contentView = View.inflate(getContext(), R.layout.certificate_share_view, null);
        dialog.setContentView(contentView);
    }
}
