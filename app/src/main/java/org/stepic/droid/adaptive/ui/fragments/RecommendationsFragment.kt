package org.stepic.droid.adaptive.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_recommendations.*
import org.stepic.droid.R
import org.stepic.droid.adaptive.ui.adapters.QuizCardsAdapter
import org.stepic.droid.base.App
import org.stepic.droid.base.FragmentBase
import org.stepic.droid.core.presenters.RecommendationsPresenter
import org.stepic.droid.core.presenters.contracts.RecommendationsView
import org.stepic.droid.model.Course
import org.stepic.droid.ui.util.initCenteredToolbar
import org.stepic.droid.util.AppConstants
import org.stepic.droid.util.MathUtli
import javax.inject.Inject

class RecommendationsFragment : FragmentBase(), RecommendationsView {
    companion object {
        fun newInstance(course: Course): RecommendationsFragment {
            val args = Bundle().apply { putParcelable(AppConstants.KEY_COURSE_BUNDLE, course) }
            return RecommendationsFragment().apply { arguments = args }
        }
    }

    @Inject
    lateinit var recommendationsPresenter: RecommendationsPresenter

    private var course: Course? = null

    private val loadingPlaceholders by lazy { resources.getStringArray(R.array.recommendation_loading_placeholders) }

    override fun onCreate(savedInstanceState: Bundle?) {
        course = arguments.getParcelable(AppConstants.KEY_COURSE_BUNDLE)
        super.onCreate(savedInstanceState)
        recommendationsPresenter.initCourse(course?.courseId ?: 0)
    }

    override fun injectComponent() {
        App.componentManager()
                .adaptiveCourseComponent(course?.courseId ?: 0)
                .inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater?.inflate(R.layout.fragment_recommendations, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tryAgain.setOnClickListener {
            recommendationsPresenter.retry()
        }

//        (activity as? AppCompatActivity)?.let {
//            it.setSupportActionBar(toolbar)
//            it.supportActionBar?.setDisplayHomeAsUpEnabled(true)
//        }

        initCenteredToolbar(title = course?.title ?: "", showHomeButton = true)
    }

    override fun onAdapter(cardsAdapter: QuizCardsAdapter) {
        cardsContainer.setAdapter(cardsAdapter)
    }

    override fun onLoading() {
        progress.visibility = View.VISIBLE
        error.visibility = View.GONE
        loadingPlaceholder.text = loadingPlaceholders[MathUtli.randomBetween(0, loadingPlaceholders.size - 1)]
    }

    override fun onCardLoaded() {
        progress.visibility = View.GONE
        cardsContainer.visibility = View.VISIBLE
    }

    private fun onError() {
        cardsContainer.visibility = View.GONE
        error.visibility = View.VISIBLE
        progress.visibility = View.GONE
    }

    override fun onConnectivityError() {
        errorMessage.setText(R.string.no_connection)
        onError()
    }

    override fun onRequestError() {
        errorMessage.setText(R.string.request_error)
        onError()
    }

    override fun onCourseCompleted() {
        cardsContainer.visibility = View.GONE
        progress.visibility = View.GONE
        courseCompleted.visibility = View.VISIBLE
    }

    override fun onCourseNotSupported() {
        // show error if course is not adaptive or not in list of accepted courses
    }

    override fun onStart() {
        super.onStart()
        recommendationsPresenter.attachView(this)
    }

    override fun onStop() {
        recommendationsPresenter.detachView(this)
        super.onStop()
    }

    override fun onReleaseComponent() {
        App.componentManager()
                .releaseAdaptiveCourseComponent(course?.courseId ?: 0)
    }

    override fun onDestroy() {
        recommendationsPresenter.destroy()
        super.onDestroy()
    }
}