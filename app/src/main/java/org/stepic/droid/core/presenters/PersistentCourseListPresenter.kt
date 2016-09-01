package org.stepic.droid.core.presenters

import org.stepic.droid.core.presenters.contracts.PersistentCourseListView
import org.stepic.droid.store.operations.DatabaseFacade

class PersistentCourseListPresenter : PresenterBase<PersistentCourseListView>() {

    var currentPage = 1;

    fun downloadData(courseType: DatabaseFacade.Table?) {

    }

    fun downloadNextPageIfExist(courseType: DatabaseFacade.Table?) {

    }


}
