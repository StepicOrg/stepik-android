package org.stepik.android.view.injection.certificates

import dagger.Subcomponent
import org.stepik.android.view.certificates.ui.activity.CertificatesActivity
import org.stepik.android.view.injection.course.CourseDataModule

@Subcomponent(modules = [
    CertificatesModule::class,
    CertificatesDataModule::class,
    CourseDataModule::class
])
interface CertificatesComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): CertificatesComponent
    }

    fun inject(certificatesActivity: CertificatesActivity)
}