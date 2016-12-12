package org.stepic.droid.core.presenters

import org.stepic.droid.concurrency.IMainHandler
import org.stepic.droid.core.presenters.contracts.ContinueCourseView
import org.stepic.droid.store.operations.DatabaseFacade
import org.stepic.droid.web.IApi
import java.util.concurrent.ThreadPoolExecutor

class ContinueCoursePresenter(val databaseFacade: DatabaseFacade,
                              val api: IApi,
                              val threadPoolExecutor: ThreadPoolExecutor,
                              val mainHandler: IMainHandler) : PresenterBase<ContinueCourseView>() {

    fun continueCourse (courseId : Long) {

    }

}
