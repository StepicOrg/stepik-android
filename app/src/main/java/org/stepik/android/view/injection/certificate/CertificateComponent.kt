package org.stepik.android.view.injection.certificate

import dagger.Subcomponent
import org.stepik.android.view.certificate.ui.activity.CertificatesActivity
import org.stepik.android.view.injection.course.CourseDataModule

@Subcomponent(modules = [
    CertificateModule::class,
    CertificateDataModule::class,
    CourseDataModule::class
])
interface CertificateComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): CertificateComponent
    }

    fun inject(certificatesActivity: CertificatesActivity)
}