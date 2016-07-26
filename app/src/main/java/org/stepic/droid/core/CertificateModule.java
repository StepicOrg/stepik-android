package org.stepic.droid.core;

import org.stepic.droid.presenters.certificate.CertificatePresenter;
import org.stepic.droid.presenters.certificate.CertificatePresenterImpl;

import dagger.Module;
import dagger.Provides;

@Module
public class CertificateModule {
    @Provides
    public CertificatePresenter providePresenter() {
        return new CertificatePresenterImpl();
    }
}
