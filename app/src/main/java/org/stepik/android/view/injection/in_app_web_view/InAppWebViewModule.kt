package org.stepik.android.view.injection.in_app_web_view

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import org.stepik.android.presentation.base.injection.ViewModelKey
import org.stepik.android.presentation.in_app_web_view.InAppWebViewPresenter

@Module
abstract class InAppWebViewModule {
    /**
     * Presentation
     */
    @Binds
    @IntoMap
    @ViewModelKey(InAppWebViewPresenter::class)
    internal abstract fun bindInAppWebViewPresenter(inAppWebViewPresenter: InAppWebViewPresenter): ViewModel
}