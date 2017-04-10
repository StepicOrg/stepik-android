package org.stepic.droid.di.certificates

import dagger.Subcomponent
import org.stepic.droid.ui.fragments.CertificateFragment

@CertificateScope
@Subcomponent
interface CertificateComponent {

    @Subcomponent.Builder
    interface Builder {
        fun build(): CertificateComponent
    }

    fun inject(certificateFragment: CertificateFragment)
}
