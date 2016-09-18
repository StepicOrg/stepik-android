package org.stepic.droid.core.components

import dagger.Subcomponent
import org.stepic.droid.core.PerFragment
import org.stepic.droid.core.modules.CertificateModule
import org.stepic.droid.ui.fragments.CertificateFragment

@PerFragment
@Subcomponent(modules = arrayOf(CertificateModule::class))
interface CertificateComponent {
    fun inject(certificateFragment: CertificateFragment)
}
