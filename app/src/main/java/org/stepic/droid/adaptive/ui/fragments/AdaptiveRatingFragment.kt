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
import by.kirich1409.viewbindingdelegate.viewBinding
import org.stepic.droid.R
import org.stepic.droid.adaptive.ui.adapters.AdaptiveRatingAdapter
import org.stepic.droid.base.App
import org.stepic.droid.base.FragmentBase
import org.stepic.droid.core.presenters.AdaptiveRatingPresenter
import org.stepic.droid.core.presenters.contracts.AdaptiveRatingView
import org.stepic.droid.databinding.FragmentAdaptiveRatingBinding
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

    private val adaptiveRatingBinding: FragmentAdaptiveRatingBinding by viewBinding(FragmentAdaptiveRatingBinding::bind)
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
        adaptiveRatingBinding.recycler.layoutManager = LinearLayoutManager(context)

        val divider = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        divider.setDrawable(ContextCompat.getDrawable(context, R.drawable.bg_divider_vertical)!!)
        adaptiveRatingBinding.recycler.addItemDecoration(divider)

        val spinnerAdapter = ArrayAdapter<CharSequence>(context, R.layout.adaptive_item_rating_period, context.resources.getStringArray(R.array.adaptive_rating_periods))
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        adaptiveRatingBinding.spinner.adapter = spinnerAdapter

        adaptiveRatingBinding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {}

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, p3: Long) {
                adaptiveRatingPresenter.changeRatingPeriod(pos)
            }
        }

        adaptiveRatingBinding.errorNoConnectionWithButton.tryAgain.setOnClickListener { adaptiveRatingPresenter.retry() }
    }

    override fun onLoading() {
        adaptiveRatingBinding.errorNoConnectionWithButton.error.visibility = View.GONE
        adaptiveRatingBinding.progress.visibility = View.VISIBLE
        adaptiveRatingBinding.container.visibility = View.GONE
    }

    private fun onError() {
        adaptiveRatingBinding.errorNoConnectionWithButton.error.visibility = View.VISIBLE
        adaptiveRatingBinding.progress.visibility = View.GONE
        adaptiveRatingBinding.container.visibility = View.GONE
    }

    override fun onConnectivityError() {
        adaptiveRatingBinding.errorNoConnectionWithButton.errorMessage.setText(R.string.no_connection)
        onError()
    }

    override fun onRequestError() {
        adaptiveRatingBinding.errorNoConnectionWithButton.errorMessage.setText(R.string.request_error)
        onError()
    }

    override fun onComplete() {
        adaptiveRatingBinding.errorNoConnectionWithButton.error.visibility = View.GONE
        adaptiveRatingBinding.progress.visibility = View.GONE
        adaptiveRatingBinding.container.visibility = View.VISIBLE
    }

    override fun onRatingAdapter(adapter: AdaptiveRatingAdapter) {
        adaptiveRatingBinding.recycler.adapter = adapter
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
