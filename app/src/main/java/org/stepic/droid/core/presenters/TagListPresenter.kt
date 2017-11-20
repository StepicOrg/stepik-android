package org.stepic.droid.core.presenters

import org.stepic.droid.core.presenters.contracts.CoursesView
import org.stepic.droid.di.tags.TagScope
import org.stepic.droid.model.Tag
import javax.inject.Inject

@TagScope
class TagListPresenter
@Inject
constructor(
        private val tag: Tag
) : PresenterBase<CoursesView>() {

    fun downloadData() {

    }

    fun refreshData() {

    }
}
