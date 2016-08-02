package org.stepic.droid.view.dialogs;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.stepic.droid.R;
import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.base.MainApplication;
import org.stepic.droid.core.IScreenManager;
import org.stepic.droid.core.ShareHelper;
import org.stepic.droid.model.CertificateViewItem;
import org.stepic.droid.util.DisplayUtils;

import javax.inject.Inject;

import butterknife.ButterKnife;

public class CertificateShareDialog extends BottomSheetDialog {

    @NotNull
    private final CertificateViewItem certificateViewItem;

    @Inject
    IScreenManager screenManager;

    @Inject
    ShareHelper shareHelper;

    @Inject
    Analytic analytic;

    public CertificateShareDialog(@NonNull Context context, @NotNull CertificateViewItem certificateViewItem) {
        super(context);
        this.certificateViewItem = certificateViewItem;
        MainApplication.component().inject(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = View.inflate(getContext(), R.layout.certificate_share_view, null);
        setContentView(view);

        int screenHeight = DisplayUtils.getScreenHeight();
        int statusBarHeight = DisplayUtils.getStatusBarHeight(getContext());
        int dialogHeight = screenHeight - statusBarHeight;
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, dialogHeight == 0 ? ViewGroup.LayoutParams.MATCH_PARENT : dialogHeight);

        View addLinkedIn = ButterKnife.findById(view, R.id.share_certificate_add_linkedin);
        View copyLink = ButterKnife.findById(view, R.id.share_certificate_copy_link);
        View shareAll = ButterKnife.findById(view, R.id.share_certificate_all);

        addLinkedIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(), "ADD LINKEDIN " + certificateViewItem.getTitle(), Toast.LENGTH_SHORT).show();
                dismiss();
            }
        });

        copyLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                analytic.reportEvent(Analytic.Certificate.COPY_LINK_CERITIFICATE);
                ClipData clipData = ClipData.newPlainText(MainApplication.getAppContext().getString(R.string.certificate_share_copy_link), certificateViewItem.getFullPath());
                ClipboardManager clipboardManager = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                clipboardManager.setPrimaryClip(clipData);
                Toast.makeText(getContext(), R.string.certificate_share_copy_link_success, Toast.LENGTH_SHORT).show();
            }
        });

        shareAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(), "SHARE ALL " + certificateViewItem.getTitle(), Toast.LENGTH_SHORT).show();
                dismiss();
            }
        });
    }

}
