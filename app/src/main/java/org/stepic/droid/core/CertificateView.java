package org.stepic.droid.core;

import org.jetbrains.annotations.Nullable;
import org.stepic.droid.model.CertificateViewItem;

import java.util.List;

public interface CertificateView {
    void onLoading();

    void showEmptyState();

    void onInternetProblem();

    void onDataLoaded(List<CertificateViewItem> certificateViewItems);

    void onNeedShowShareDialog(@Nullable CertificateViewItem certificateViewItem);
}
