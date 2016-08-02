package org.stepic.droid.presenters.certificate;

import android.app.Activity;

import org.jetbrains.annotations.Nullable;
import org.stepic.droid.core.CertificateView;
import org.stepic.droid.model.CertificateViewItem;

public interface CertificatePresenter {
    void onCreate(CertificateView view);

    void onDestroy();

    void showCertificates();

    @Nullable
    CertificateViewItem get(int position);

    int size();

    void showCertificateAsPdf(Activity activity, String fullPath);

    void showShareDialogForCertificate(CertificateViewItem certificateViewItem);
}
