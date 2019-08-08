package org.stepik.android.view.injection.certificates

import dagger.Subcomponent
import org.stepik.android.view.certificates.ui.activity.CertificatesActivity

@Subcomponent(modules = [
    CertificatesModule::class
])
interface CertificatesComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): CertificatesComponent
    }

    fun inject(certificatesActivity: CertificatesActivity)
}