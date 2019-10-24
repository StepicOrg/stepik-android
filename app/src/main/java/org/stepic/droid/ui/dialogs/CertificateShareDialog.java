package org.stepic.droid.ui.dialogs;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.jetbrains.annotations.NotNull;
import org.stepic.droid.R;
import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.base.App;
import org.stepic.droid.core.ScreenManager;
import org.stepic.droid.core.ShareHelper;
import org.stepic.droid.model.CertificateViewItem;
import org.stepic.droid.util.ContextExtensionsKt;
import org.stepic.droid.util.DisplayUtils;

import javax.inject.Inject;

public class CertificateShareDialog extends BottomSheetDialog {

    @NotNull
    private final CertificateViewItem certificateViewItem;

    @Inject
    ScreenManager screenManager;

    @Inject
    ShareHelper shareHelper;

    @Inject
    Analytic analytic;

    public CertificateShareDialog(@NonNull Context context, @NotNull CertificateViewItem certificateViewItem) {
        super(context);
        this.certificateViewItem = certificateViewItem;
        App.Companion.component().inject(this);
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

        View addLinkedIn = view.findViewById(R.id.share_certificate_add_linkedin);
        View copyLink = view.findViewById(R.id.share_certificate_copy_link);
        View shareAll = view.findViewById(R.id.share_certificate_all);

        addLinkedIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                analytic.reportEvent(Analytic.Certificate.ADD_LINKEDIN);
                screenManager.addCertificateToLinkedIn(certificateViewItem);
            }
        });

        copyLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                analytic.reportEvent(Analytic.Certificate.COPY_LINK_CERTIFICATE);
                ContextExtensionsKt.copyTextToClipboard(
                        getContext(),
                        App.Companion.getAppContext().getString(R.string.copy_link_title),
                        certificateViewItem.getCertificate().getUrl(),
                        getContext().getResources().getString(R.string.link_copied_title)
                );
            }
        });

        shareAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                analytic.reportEvent(Analytic.Certificate.SHARE_LINK_CERTIFICATE);
                Intent intent = shareHelper.getIntentForShareCertificate(certificateViewItem);
                getContext().startActivity(intent);
            }
        });
    }

}
