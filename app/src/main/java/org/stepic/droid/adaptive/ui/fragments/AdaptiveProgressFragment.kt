package org.stepic.droid.adaptive.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.stepic.droid.R
import org.stepic.droid.adaptive.ui.adapters.AdaptiveWeeksAdapter
import org.stepic.droid.base.App
import org.stepic.droid.base.FragmentBase
import org.stepic.droid.core.presenters.AdaptiveProgressPresenter
import org.stepic.droid.core.presenters.contracts.AdaptiveProgressView
import ru.nobird.android.view.base.ui.extension.argument
import javax.inject.Inject

class AdaptiveProgressFragment : FragmentBase(), AdaptiveProgressView {
    companion object {
        fun newInstance(courseId: Long) = AdaptiveProgressFragment()
            .apply {
                this.courseId = courseId
            }
    }

    private var courseId by argument<Long>()

    @Inject
    lateinit var adaptiveProgressPresenter: AdaptiveProgressPresenter

    private lateinit var recycler: RecyclerView

    override fun injectComponent() {
        App.componentManager()
            .adaptiveCourseComponent(courseId)
            .inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val context = requireContext()

        recycler = RecyclerView(context)
        recycler.layoutManager = LinearLayoutManager(context)

        val divider = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        divider.setDrawable(ContextCompat.getDrawable(context, R.drawable.bg_divider_vertical)!!)
        recycler.addItemDecoration(divider)

        return recycler
    }

    override fun onWeeksAdapter(adapter: AdaptiveWeeksAdapter) {
        recycler.adapter = adapter
    }

    override fun onStart() {
        super.onStart()
        adaptiveProgressPresenter.attachView(this)
    }

    override fun onStop() {
        adaptiveProgressPresenter.detachView(this)
        super.onStop()
    }

    override fun onReleaseComponent() {
        App.componentManager()
            .releaseAdaptiveCourseComponent(courseId)
    }

    override fun onDestroy() {
        adaptiveProgressPresenter.destroy()
        super.onDestroy()
    }
}