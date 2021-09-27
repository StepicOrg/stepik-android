package org.stepik.android.view.course_search.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import by.kirich1409.viewbindingdelegate.viewBinding
import kotlinx.android.synthetic.main.view_search_toolbar.*
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.core.presenters.SearchSuggestionsPresenter
import org.stepic.droid.core.presenters.contracts.SearchSuggestionsView
import org.stepic.droid.databinding.DialogCourseSearchBinding
import org.stepic.droid.model.SearchQuery
import org.stepic.droid.model.SearchQuerySource
import org.stepic.droid.ui.custom.AutoCompleteSearchView
import ru.nobird.android.view.base.ui.extension.argument
import javax.inject.Inject

class CourseSearchDialogFragment :
    DialogFragment(),
    SearchSuggestionsView,
    AutoCompleteSearchView.FocusCallback {

    companion object {
        const val TAG = "CourseSearchDialogFragment"

        fun newInstance(courseId: Long): DialogFragment =
            CourseSearchDialogFragment().apply {
                this.courseId = courseId
            }
    }

    @Inject
    lateinit var searchSuggestionsPresenter: SearchSuggestionsPresenter

    private var courseId: Long by argument()

    private lateinit var searchIcon: ImageView

    private val courseSearchBinding: DialogCourseSearchBinding by viewBinding(DialogCourseSearchBinding::bind)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectComponent()
        setStyle(STYLE_NO_TITLE, R.style.ThemeOverlay_AppTheme_Dialog_Fullscreen)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.dialog_course_search, container, false)

    private fun injectComponent() {
        App.component()
            .courseSearchComponentBuilder()
            .courseId(courseId)
            .build()
            .inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        searchIcon = searchViewToolbar.findViewById(androidx.appcompat.R.id.search_mag_icon) as ImageView
        setupSearchBar()
    }

    override fun onStart() {
        super.onStart()
        searchSuggestionsPresenter.attachView(this)
    }

    override fun onStop() {
        searchSuggestionsPresenter.detachView(this)
        super.onStop()
    }

    override fun setSuggestions(suggestions: List<SearchQuery>, source: SearchQuerySource) {
        courseSearchBinding.viewSearchToolbarBinding.searchViewToolbar.setSuggestions(suggestions, source)
    }

    override fun onFocusChanged(hasFocus: Boolean) {
        if (hasFocus) {
            searchSuggestionsPresenter.onQueryTextChange(courseSearchBinding.viewSearchToolbarBinding.searchViewToolbar.query.toString())
        }
    }

    private fun setupSearchBar() {
        courseSearchBinding.viewSearchToolbarBinding.viewCenteredToolbarBinding.centeredToolbar.isVisible = false
        courseSearchBinding.viewSearchToolbarBinding.backIcon.isVisible = true
        courseSearchBinding.viewSearchToolbarBinding.searchViewToolbar.isVisible = true
        searchIcon.setImageResource(0)
        (courseSearchBinding.viewSearchToolbarBinding.searchViewToolbar.layoutParams as ViewGroup.MarginLayoutParams).setMargins(0, 0, 0, 0)
        setupSearchView(courseSearchBinding.viewSearchToolbarBinding.searchViewToolbar)
        courseSearchBinding.viewSearchToolbarBinding.searchViewToolbar.setFocusCallback(this)
        courseSearchBinding.viewSearchToolbarBinding.searchViewToolbar.setIconifiedByDefault(false)
        courseSearchBinding.viewSearchToolbarBinding.searchViewToolbar.setBackgroundColor(0)
        courseSearchBinding.viewSearchToolbarBinding.backIcon.setOnClickListener {
            val hasFocus = courseSearchBinding.viewSearchToolbarBinding.searchViewToolbar.hasFocus()
            if (hasFocus) {
                courseSearchBinding.viewSearchToolbarBinding.searchViewToolbar.clearFocus()
            } else {
                dismiss()
            }
        }
    }

    private fun setupSearchView(searchView: AutoCompleteSearchView) {
        searchView.initSuggestions(courseSearchBinding.courseSearchContainer)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                searchSuggestionsPresenter.onQueryTextSubmit(query)
                with(courseSearchBinding.viewSearchToolbarBinding.searchViewToolbar) {
                    onActionViewCollapsed()
                    onActionViewExpanded()
                    clearFocus()
                }
                // TODO Send message to ViewModel
                return true
            }

            override fun onQueryTextChange(query: String): Boolean {
                searchView.setConstraint(query)
                searchSuggestionsPresenter.onQueryTextChange(query)
                return false
            }
        })
        searchView.onActionViewExpanded()
        searchView.clearFocus()
    }
}