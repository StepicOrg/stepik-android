package org.stepic.droid.core

import dagger.Subcomponent
import org.stepic.droid.ui.fragments.CertificateFragment

@PerFragment
@Subcomponent(modules = arrayOf(CertificateModule::class))
interface CertificateComponent {
    fun inject(certificateFragment: CertificateFragment)
}
