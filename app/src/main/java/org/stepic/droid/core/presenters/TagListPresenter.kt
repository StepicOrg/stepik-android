package org.stepic.droid.core.presenters

import org.stepic.droid.core.presenters.contracts.CoursesView
import org.stepic.droid.di.tags.TagScope
import org.stepic.droid.model.Tag
import org.stepic.droid.web.Api
import javax.inject.Inject

@TagScope
class TagListPresenter
@Inject
constructor(
        private val tag: Tag,
        private val api : Api
) : PresenterBase<CoursesView>() {

    fun downloadData() {

    }

    fun refreshData() {

    }
}
