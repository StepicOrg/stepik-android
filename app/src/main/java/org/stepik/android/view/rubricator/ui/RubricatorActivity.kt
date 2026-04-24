package org.stepik.android.view.rubricator.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.MenuItem
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import by.kirich1409.viewbindingdelegate.viewBinding
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.base.FragmentActivityBase
import org.stepic.droid.databinding.ActivityRubricatorBinding
import org.stepic.droid.databinding.ItemRubricatorBinding
import org.stepik.android.domain.filter.model.CourseListFilterQuery
import org.stepik.android.presentation.rubricator.RubricatorFeature
import org.stepik.android.presentation.rubricator.RubricatorViewModel
import org.stepik.android.view.course_list.ui.activity.CourseListSearchActivity
import org.stepik.android.view.rubricator.model.RubricatorItem
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder
import ru.nobird.android.ui.adapterdelegates.dsl.adapterDelegate
import ru.nobird.android.ui.adapters.DefaultDelegateAdapter
import ru.nobird.android.view.redux.ui.extension.reduxViewModel
import ru.nobird.app.presentation.redux.container.ReduxView
import javax.inject.Inject

class RubricatorActivity : FragmentActivityBase(),
    ReduxView<RubricatorFeature.State, RubricatorFeature.Action.ViewAction> {

    private val rubricatorBinding: ActivityRubricatorBinding by viewBinding(
        ActivityRubricatorBinding::bind
    )

    private val viewStateDelegate = ViewStateDelegate<RubricatorFeature.RubricatorState>()

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private val rubricatorViewModel: RubricatorViewModel by reduxViewModel(this) { viewModelFactory }

    private val rubricatorItemAdapter: DefaultDelegateAdapter<RubricatorItem> =
        DefaultDelegateAdapter()

    companion object {
        private const val EXTRA_RUBRICATOR_URL = "rubricator_url"
        fun createIntent(context: Context, rubricatorUrl: String): Intent =
            Intent(context, RubricatorActivity::class.java)
                .putExtra(EXTRA_RUBRICATOR_URL, rubricatorUrl)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rubricator)
        injectComponent()
        initViewStateDelegate()

        initToolbar()
        setToolbarTitle(getToolbarMainTitle())

        rubricatorItemAdapter += buildSubjectAdapterDelegate()
        rubricatorItemAdapter += buildMetaCategoryAdapterDelegate()
        rubricatorItemAdapter += buildCourseListAdapterDelegate()

        with(rubricatorBinding.rubricatorRecycler) {
            adapter = rubricatorItemAdapter
            layoutManager = LinearLayoutManager(context)
            itemAnimator = null
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL).apply {
                AppCompatResources.getDrawable(context, R.drawable.bg_divider_vertical)
                    ?.let(::setDrawable)
            })
        }

        val rubricatorUrl = intent.getStringExtra(EXTRA_RUBRICATOR_URL) ?: ""
        rubricatorViewModel.onNewMessage(RubricatorFeature.Message.InitMessage(rubricatorUrl))
    }

    private fun injectComponent() {
        App.component()
            .rubricatorComponentBuilder()
            .build()
            .inject(this)
    }

    private fun initViewStateDelegate() {
        viewStateDelegate.addState<RubricatorFeature.RubricatorState.Idle>()
        viewStateDelegate.addState<RubricatorFeature.RubricatorState.Loading>(rubricatorBinding.rubricatorProgressBar.root)
        viewStateDelegate.addState<RubricatorFeature.RubricatorState.Error>(rubricatorBinding.rubricatorError.error)
        viewStateDelegate.addState<RubricatorFeature.RubricatorState.Content>(rubricatorBinding.rubricatorRecycler)
    }

    override fun onAction(action: RubricatorFeature.Action.ViewAction) {
        if (action is RubricatorFeature.Action.ViewAction.CloseScreen) {
            finish()
        }
    }

    override fun render(state: RubricatorFeature.State) {
        viewStateDelegate.switchState(state.rubricatorState)
        if (state.rubricatorState is RubricatorFeature.RubricatorState.Content) {
            val adapterItems = when {
                state.courseListState is RubricatorFeature.CourseListState.Content -> {
                    setToolbarTitle(state.courseListState.metaCategoryTitle)
                    state.courseListState.courseLists.map(RubricatorItem::CourseListItem)
                }

                state.metaCategoryState is RubricatorFeature.MetaCategoryState.Content -> {
                    setToolbarTitle(state.metaCategoryState.subjectTitle)
                    state.metaCategoryState.metaCategories.map(RubricatorItem::MetaCategoryItem)
                }

                else -> {
                    setToolbarTitle(getToolbarMainTitle())
                    state.rubricatorState.rubricatorData.subjects.map(RubricatorItem::SubjectItem)
                }
            }

            rubricatorItemAdapter.items = adapterItems
        }
    }

    override fun onBackPressed() {
        rubricatorViewModel.onNewMessage(RubricatorFeature.Message.OnBackPressed)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                // Respond to the action bar's Up/Home button
                rubricatorViewModel.onNewMessage(RubricatorFeature.Message.OnBackPressed)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun buildSubjectAdapterDelegate(): AdapterDelegate<RubricatorItem, DelegateViewHolder<RubricatorItem>> =
        adapterDelegate<RubricatorItem, RubricatorItem>(
            layoutResId = R.layout.item_rubricator,
            isForViewType = { _, viewType -> viewType is RubricatorItem.SubjectItem }
        ) {
            val rubricatorItemBinding = ItemRubricatorBinding.bind(this.itemView)

            rubricatorItemBinding.root.setOnClickListener {
                (item as? RubricatorItem.SubjectItem)?.let { subjectItem ->
                    rubricatorViewModel.onNewMessage(
                        RubricatorFeature.Message.FetchMetaCategories(
                            subjectItem.subject
                        )
                    )
                }
            }

            onBind { data ->
                data as RubricatorItem.SubjectItem
                rubricatorItemBinding.itemRubricatorTitle.text = data.subject.title
                rubricatorItemBinding.itemRubricatorChevron.isVisible = true
            }
        }

    private fun buildMetaCategoryAdapterDelegate(): AdapterDelegate<RubricatorItem, DelegateViewHolder<RubricatorItem>> =
        adapterDelegate<RubricatorItem, RubricatorItem>(
            layoutResId = R.layout.item_rubricator,
            isForViewType = { _, viewType -> viewType is RubricatorItem.MetaCategoryItem }
        ) {
            val rubricatorItemBinding = ItemRubricatorBinding.bind(this.itemView)

            rubricatorItemBinding.root.setOnClickListener {
                (item as? RubricatorItem.MetaCategoryItem)?.let { metaCategoryItem ->
                    if (metaCategoryItem.metaCategory.courseLists.size == 1) {
                        openCourseList(metaCategoryItem.metaCategory.courseLists.first())
                    } else {
                        rubricatorViewModel.onNewMessage(
                            RubricatorFeature.Message.FetchCourseLists(
                                metaCategoryItem.metaCategory
                            )
                        )
                    }
                }
            }

            onBind { data ->
                data as RubricatorItem.MetaCategoryItem
                rubricatorItemBinding.itemRubricatorTitle.text = data.metaCategory.title
                rubricatorItemBinding.itemRubricatorChevron.isVisible =
                    data.metaCategory.courseLists.size > 1
            }
        }

    private fun buildCourseListAdapterDelegate(): AdapterDelegate<RubricatorItem, DelegateViewHolder<RubricatorItem>> =
        adapterDelegate<RubricatorItem, RubricatorItem>(
            layoutResId = R.layout.item_rubricator,
            isForViewType = { _, viewType -> viewType is RubricatorItem.CourseListItem }
        ) {
            val rubricatorItemBinding = ItemRubricatorBinding.bind(this.itemView)

            rubricatorItemBinding.root.setOnClickListener {
                (item as? RubricatorItem.CourseListItem)?.let { courseListItem ->
                    openCourseList(courseListItem.courseList.id)
                }
            }

            onBind { data ->
                data as RubricatorItem.CourseListItem
                rubricatorItemBinding.itemRubricatorTitle.text = data.courseList.title
                rubricatorItemBinding.itemRubricatorChevron.isVisible = false
            }
        }

    private fun openCourseList(courseListId: Long) {
        val courseListFilterQuery = CourseListFilterQuery(
            courseList = courseListId
        )
        val intent = createSearchViewIntent(courseListFilterQuery)
        startActivity(intent)
    }

    private fun createSearchViewIntent(filterQuery: CourseListFilterQuery): Intent {
        val intent = Intent(this, CourseListSearchActivity::class.java)
        intent.putExtra(
            CourseListSearchActivity.EXTRA_COURSE_LIST_FILTER_QUERY,
            filterQuery as Parcelable
        )
        intent.action = Intent.ACTION_SEARCH
        return intent
    }

    private fun initToolbar() {
        val toolbar = rubricatorBinding
            .rubricatorAppbar
            .viewCenteredToolbarBinding
            .centeredToolbar

        setSupportActionBar(toolbar)

        val actionBar = this.supportActionBar
            ?: throw IllegalStateException("support action bar should be set")

        actionBar.setDisplayShowTitleEnabled(false)
        actionBar.setDisplayHomeAsUpEnabled(true)
    }

    private fun getToolbarMainTitle(): String =
        getString(R.string.rubricator_main_toolbar_title)

    private fun setToolbarTitle(title: String) {
        rubricatorBinding
            .rubricatorAppbar
            .viewCenteredToolbarBinding
            .centeredToolbarTitle
            .text = title
    }
}