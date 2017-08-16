package org.stepic.droid.ui.fragments

import android.app.Activity
import android.os.Bundle
import android.view.*
import android.widget.Checkable
import kotlinx.android.synthetic.main.fragment_filter.*
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.base.FragmentBase
import org.stepic.droid.core.presenters.FilterPresenter
import org.stepic.droid.core.presenters.contracts.FilterView
import org.stepic.droid.model.StepikFilter
import org.stepic.droid.storage.operations.Table
import org.stepic.droid.ui.util.initCenteredToolbar
import org.stepic.droid.util.AppConstants
import java.util.*
import javax.inject.Inject

class FilterFragment : FragmentBase(),
        FilterView {

    companion object {

        private val filterCourseTypeKey = "filterCourseType"

        fun newInstance(filterCourseTypeCode: Int): FilterFragment {
            val args = Bundle()
            args.putInt(filterCourseTypeKey, filterCourseTypeCode)
            val fragment = FilterFragment()
            fragment.arguments = args
            return fragment
        }
    }

    @Inject
    lateinit var filterPresenter: FilterPresenter

    private lateinit var courseType: Table

    override fun injectComponent() {
        App
                .component()
                .filterComponentBuilder()
                .build()
                .inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        setHasOptionsMenu(true)

        val filterCode = arguments.getInt(filterCourseTypeKey)

        courseType = when (filterCode) {
            AppConstants.ENROLLED_FILTER -> Table.enrolled
            AppConstants.FEATURED_FILTER -> Table.featured
            else -> throw IllegalStateException("course type for filters should be set")
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_filter, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initToolbar()

        acceptButton.setOnClickListener {
            analytic.reportEvent(Analytic.Interaction.CLICK_ACCEPT_FILTER_BUTTON)
            filterPresenter.acceptFilter(getCurrentFilterFromUI(), courseType)
        }

        cancelButton.setOnClickListener { activity.finish() }
        filterPresenter.attachView(this)
        filterPresenter.initFiltersIfNeed(courseType)
    }

    private fun initToolbar() {
        initCenteredToolbar(R.string.filter_title, true, closeIconDrawableRes)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater?.inflate(R.menu.filter_accept_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            android.R.id.home -> {
                // Respond to the action bar's Up/Home button
                activity.finish()
                return true
            }
            R.id.accept_action -> {
                filterPresenter.acceptFilter(getCurrentFilterFromUI(), courseType)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onDestroyView() {
        acceptButton.setOnClickListener(null)
        cancelButton.setOnClickListener(null)
        filterPresenter.detachView(this)
        super.onDestroyView()
    }

    private fun applyFilterToView(filters: EnumSet<StepikFilter>, stepikFilterValue: StepikFilter, checkable: Checkable) {
        checkable.isChecked = filters.contains(stepikFilterValue)
    }

    private fun getCurrentFilterFromUI(): EnumSet<StepikFilter> {
        val filter = EnumSet.noneOf(StepikFilter::class.java)
        appendToFilter(filter, StepikFilter.RUSSIAN, languageRuCheckBox.isChecked)
        appendToFilter(filter, StepikFilter.ENGLISH, languageEnCheckBox.isChecked)
        appendToFilter(filter, StepikFilter.UPCOMING, upcomingCheckBox.isChecked)
        appendToFilter(filter, StepikFilter.ACTIVE, activeCheckBox.isChecked)
        appendToFilter(filter, StepikFilter.PAST, pastCheckBox.isChecked)
        appendToFilter(filter, StepikFilter.PERSISTENT, persistentCheckBox.isChecked)
        return filter
    }

    private fun appendToFilter(filter: EnumSet<StepikFilter>, stepikFilterValue: StepikFilter, needAppend: Boolean) {
        if (needAppend) {
            filter.add(stepikFilterValue)
        }
    }

    override fun onFilterAccepted() {
        //filter is accepted, close
        activity.setResult(Activity.RESULT_OK)
        activity.finish()
    }

    override fun onFiltersPreparedForView(filters: EnumSet<StepikFilter>) {
        //set filter, when it is prepared:
        applyFilterToView(filters, StepikFilter.RUSSIAN, languageRuCheckBox)
        applyFilterToView(filters, StepikFilter.ENGLISH, languageEnCheckBox)
        applyFilterToView(filters, StepikFilter.UPCOMING, upcomingCheckBox)
        applyFilterToView(filters, StepikFilter.ACTIVE, activeCheckBox)
        applyFilterToView(filters, StepikFilter.PAST, pastCheckBox)
        applyFilterToView(filters, StepikFilter.PERSISTENT, persistentCheckBox)
    }

}
