package org.stepik.android.presentation.catalog

import io.reactivex.subjects.PublishSubject
import org.stepic.droid.model.StepikFilter
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepik.android.view.injection.catalog.FiltersBus
import ru.nobird.android.presentation.base.PresenterBase
import java.util.EnumSet
import javax.inject.Inject

class FiltersPresenter
@Inject
constructor(
    private val sharedPreferenceHelper: SharedPreferenceHelper,
    @FiltersBus
    private val filtersPublisher: PublishSubject<EnumSet<StepikFilter>>

) : PresenterBase<FiltersView>() {
    fun onNeedFilters() {
        view?.onFiltersPrepared(sharedPreferenceHelper.filterForFeatured)
    }

    fun onFilterChanged(newAppliedFilters: EnumSet<StepikFilter>) {
        sharedPreferenceHelper.saveFilterForFeatured(newAppliedFilters)
        filtersPublisher.onNext(newAppliedFilters)
    }
}