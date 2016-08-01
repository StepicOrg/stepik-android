package org.stepic.droid.core;

import org.stepic.droid.configuration.IConfig;
import org.stepic.droid.presenters.certificate.CertificatePresenter;
import org.stepic.droid.presenters.certificate.CertificatePresenterImpl;
import org.stepic.droid.web.IApi;

import dagger.Module;
import dagger.Provides;

@Module
public class CertificateModule {
    @Provides
    public CertificatePresenter providePresenter(IApi api, IConfig config, IScreenManager screenManager) {
        return new CertificatePresenterImpl(api, config, screenManager);
    }
}
