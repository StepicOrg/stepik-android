package org.stepic.droid.ui.presenters.certificate;

import android.app.Activity;

import org.jetbrains.annotations.Nullable;
import org.stepic.droid.core.CertificateView;
import org.stepic.droid.model.CertificateViewItem;
import org.stepic.droid.ui.presenters.Presenter;

public interface CertificatePresenter extends Presenter<CertificateView> {

    void showCertificates(boolean isRefreshing);

    @Nullable
    CertificateViewItem get(int position);

    int size();

    void showCertificateAsPdf(Activity activity, String fullPath);

    void showShareDialogForCertificate(CertificateViewItem certificateViewItem);
}
