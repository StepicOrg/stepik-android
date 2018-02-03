package org.stepic.droid.core.presenters

import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineDataSet
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import org.stepic.droid.adaptive.ui.adapters.AdaptiveWeeksAdapter
import org.stepic.droid.adaptive.util.ExpHelper
import org.stepic.droid.core.presenters.contracts.AdaptiveProgressView
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.CourseId
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepic.droid.storage.operations.DatabaseFacade
import javax.inject.Inject

class AdaptiveProgressPresenter
@Inject
constructor (
        @CourseId
        private val courseId: Long,
        @BackgroundScheduler
        private val backgroundScheduler: Scheduler,
        @MainScheduler
        private val mainScheduler: Scheduler,
        private val databaseFacade: DatabaseFacade
) : PresenterBase<AdaptiveProgressView>() {
    private val adapter = AdaptiveWeeksAdapter()

    private val compositeDisposable = CompositeDisposable()

    init {
        compositeDisposable.add(
                Single.fromCallable { databaseFacade.getExpForCourse(courseId) }
                        .subscribeOn(backgroundScheduler)
                        .observeOn(mainScheduler)
                        .subscribe({
                            adapter.setHeaderLevelAndTotal(ExpHelper.getCurrentLevel(it), it)
                        }, {})
        )

        compositeDisposable.add(
                Single.fromCallable { databaseFacade.getExpForWeeks(courseId) }
                        .subscribeOn(backgroundScheduler)
                        .observeOn(mainScheduler)
                        .subscribe(adapter::addAll, {})
        )

        compositeDisposable.add(
                Single.fromCallable { databaseFacade.getExpForLast7Days(courseId) }
                        .map {
                            Pair(LineDataSet(it.mapIndexed { index, l -> Entry(index.toFloat(), l.toFloat()) }, ""), it.sum())
                        }
                        .subscribeOn(backgroundScheduler)
                        .observeOn(mainScheduler)
                        .subscribe({
                            adapter.setHeaderChart(it.first, it.second)
                        }, {})
        )
    }

    override fun attachView(view: AdaptiveProgressView) {
        super.attachView(view)
        view.onWeeksAdapter(adapter)
    }

    fun destroy() {
        compositeDisposable.dispose()
    }
}