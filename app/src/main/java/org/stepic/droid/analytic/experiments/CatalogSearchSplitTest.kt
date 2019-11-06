package org.stepic.droid.analytic.experiments

import org.stepic.droid.analytic.Analytic
import org.stepic.droid.preferences.SharedPreferenceHelper
import javax.inject.Inject

class CatalogSearchSplitTest
@Inject
constructor(
    analytic: Analytic,
    sharedPreferenceHelper: SharedPreferenceHelper
) : SplitTest<CatalogSearchSplitTest.Group>(
    analytic,
    sharedPreferenceHelper,

    name = "catalog_search",
    groups = Group.values()
) {
    enum class Group(
        val isUpdatedSearchVisible: Boolean
    ) : SplitTest.Group {
        Control(isUpdatedSearchVisible = false),
        NewSearch(isUpdatedSearchVisible = true)
    }
}