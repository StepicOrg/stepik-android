package org.stepik.android.presentation.font_size_settings

import io.reactivex.Scheduler
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.presentation.base.PresenterBase
import javax.inject.Inject

class FontSizePresenter
@Inject
constructor(
    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : PresenterBase<FontSizeView>() {
}