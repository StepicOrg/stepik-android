package org.stepic.droid.core;

import org.stepic.droid.concurrency.IMainHandler;
import org.stepic.droid.presenters.certificate.CertificatePresenter;
import org.stepic.droid.presenters.certificate.CertificatePresenterImpl;
import org.stepic.droid.store.operations.DatabaseFacade;
import org.stepic.droid.web.IApi;

import java.util.concurrent.ThreadPoolExecutor;

import dagger.Module;
import dagger.Provides;

@Module
public class CertificateModule {
    @Provides
    public CertificatePresenter providePresenter(IApi api) {
        return new CertificatePresenterImpl(api);
    }
}
