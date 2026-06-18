package org.stepik.android.view.injection.certificate

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import org.stepik.android.view.injection.base.ViewModelFactoryModule
import org.stepik.android.presentation.base.injection.ViewModelKey
import org.stepik.android.presentation.certificate.CertificatesPresenter

@Module(includes = [ViewModelFactoryModule::class])
abstract class CertificateModule {
    /**
     * PRESENTATION LAYER
     */

    @Binds
    @IntoMap
    @ViewModelKey(CertificatesPresenter::class)
    internal abstract fun bindCertificatesPresenter(certificatesPresenter: CertificatesPresenter): ViewModel
}