package org.stepic.droid.view.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.DialogFragment;
import android.view.ViewGroup;

import org.stepic.droid.R;
import org.stepic.droid.util.DisplayUtils;

public class CertificateShareDialog extends BottomSheetDialogFragment {

    public static DialogFragment newInstance() {

        Bundle args = new Bundle();

        CertificateShareDialog fragment = new CertificateShareDialog();
        fragment.setArguments(args);
        return fragment;
    }

//    @Override
//    public void setupDialog(Dialog dialog, int style) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && dialog.getWindow() != null) {
//            dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//        }
//        super.setupDialog(dialog, style);
//        View contentView = View.inflate(getContext(), R.layout.certificate_share_view, null);
//        dialog.setContentView(contentView);
//    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        dialog.setContentView(R.layout.certificate_share_view);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && dialog.getWindow() != null) {
//            dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//        }
        int screenHeight = DisplayUtils.getScreenHeight();
        int statusBarHeight = DisplayUtils.getStatusBarHeight(getContext());
        int dialogHeight = 300;// screenHeight - (500);
        getActivity().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, dialogHeight == 0 ? ViewGroup.LayoutParams.MATCH_PARENT : dialogHeight);

        return dialog;

    }
}
