package org.stepic.droid.adaptive.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.error_no_connection_with_button.*
import kotlinx.android.synthetic.main.fragment_adaptive_rating.*
import org.stepic.droid.R
import org.stepic.droid.adaptive.ui.adapters.AdaptiveRatingAdapter
import org.stepic.droid.base.App
import org.stepic.droid.base.FragmentBase
import org.stepic.droid.core.presenters.AdaptiveRatingPresenter
import org.stepic.droid.core.presenters.contracts.AdaptiveRatingView
import org.stepik.android.view.base.ui.extension.ColorExtensions
import ru.nobird.android.view.base.ui.extension.argument
import javax.inject.Inject

class AdaptiveRatingFragment: FragmentBase(), AdaptiveRatingView {
    companion object {
        fun newInstance(courseId: Long) = AdaptiveRatingFragment().also {
            it.courseId = courseId
        }
    }

    @Inject
    lateinit var adaptiveRatingPresenter: AdaptiveRatingPresenter

    private var courseId by argument<Long>()

    override fun injectComponent() {
        App.componentManager()
            .adaptiveCourseComponent(courseId)
            .inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_adaptive_rating, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val context = requireContext()

        super.onViewCreated(view, savedInstanceState)
        recycler.layoutManager = LinearLayoutManager(context)

        val divider = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        divider.setDrawable(ContextCompat.getDrawable(context, R.drawable.bg_divider_vertical)!!)
        recycler.addItemDecoration(divider)

        spinnerContainer.setBackgroundColor(ColorExtensions.colorSurfaceWithElevationOverlay(context, resources.getInteger(R.integer.highlighted_element_elevation), overrideLightTheme = true))
        val spinnerAdapter = ArrayAdapter<CharSequence>(context, R.layout.adaptive_item_rating_period, context.resources.getStringArray(R.array.adaptive_rating_periods))
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = spinnerAdapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {}

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, p3: Long) {
                adaptiveRatingPresenter.changeRatingPeriod(pos)
            }
        }

        tryAgain.setOnClickListener { adaptiveRatingPresenter.retry() }
    }

    override fun onLoading() {
        error.visibility = View.GONE
        progress.visibility = View.VISIBLE
        container.visibility = View.GONE
    }

    private fun onError() {
        error.visibility = View.VISIBLE
        progress.visibility = View.GONE
        container.visibility = View.GONE
    }

    override fun onConnectivityError() {
        errorMessage.setText(R.string.no_connection)
        onError()
    }

    override fun onRequestError() {
        errorMessage.setText(R.string.request_error)
        onError()
    }

    override fun onComplete() {
        error.visibility = View.GONE
        progress.visibility = View.GONE
        container.visibility = View.VISIBLE
    }

    override fun onRatingAdapter(adapter: AdaptiveRatingAdapter) {
        recycler.adapter = adapter
    }

    override fun onStart() {
        super.onStart()
        adaptiveRatingPresenter.attachView(this)
    }

    override fun onStop() {
        adaptiveRatingPresenter.detachView(this)
        super.onStop()
    }

    override fun onReleaseComponent() {
        App.componentManager()
                .releaseAdaptiveCourseComponent(courseId)
    }

    override fun onDestroy() {
        adaptiveRatingPresenter.destroy()
        super.onDestroy()
    }
}