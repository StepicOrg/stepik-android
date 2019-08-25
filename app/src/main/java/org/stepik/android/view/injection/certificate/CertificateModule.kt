package org.stepik.android.view.injection.certificate

import android.arch.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import org.stepik.android.presentation.base.injection.ViewModelKey
import org.stepik.android.presentation.certificate.CertificatesPresenter

@Module
abstract class CertificateModule {
    /**
     * PRESENTATION LAYER
     */

    @Binds
    @IntoMap
    @ViewModelKey(CertificatesPresenter::class)
    internal abstract fun bindCertificatesPresenter(certificatesPresenter: CertificatesPresenter): ViewModel
}