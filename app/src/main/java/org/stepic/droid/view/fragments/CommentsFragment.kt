package org.stepic.droid.view.fragments

import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import com.squareup.otto.Subscribe
import org.stepic.droid.R
import org.stepic.droid.base.FragmentBase
import org.stepic.droid.base.MainApplication
import org.stepic.droid.core.CommentManager
import org.stepic.droid.events.comments.*
import org.stepic.droid.util.ProgressHelper
import org.stepic.droid.view.adapters.CommentsAdapter
import org.stepic.droid.web.DiscussionProxyResponse
import retrofit.Callback
import retrofit.Response
import retrofit.Retrofit
import javax.inject.Inject

class CommentsFragment : FragmentBase(), SwipeRefreshLayout.OnRefreshListener {

    companion object {
        private val discussionIdKey = "dis_id_key"
        private val stepIdKey = "stepId"

        fun newInstance(discussionId: String, stepId: Long): Fragment {
            val args = Bundle()
            args.putString(discussionIdKey, discussionId)
            args.putLong(stepIdKey, stepId)
            val fragment = CommentsFragment()
            fragment.arguments = args
            return fragment
        }
    }

    @Inject
    lateinit var commentManager: CommentManager

    lateinit var commentAdapter: CommentsAdapter

    lateinit var discussionId: String
    var stepId: Long? = null

    lateinit var mToolbar: Toolbar
    lateinit var loadProgressBarOnCenter: ProgressBar
    lateinit var swipeRefreshLayout: SwipeRefreshLayout
    lateinit var recyclerView: RecyclerView
    var floatingActionButton: FloatingActionButton? = null
    lateinit var emptyStateView: View
    lateinit var errorView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        MainApplication.component().inject(this)
        commentAdapter = CommentsAdapter(commentManager, context)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater?.inflate(R.layout.fragment_comments, container, false)
        discussionId = arguments.getString(discussionIdKey)
        stepId = arguments.getLong(stepIdKey)
        setHasOptionsMenu(true)
        v?.let {
            initToolbar(v)
            initEmptyProgressOnCenter(v)
            initSwipeRefreshLayout(v)
            initRecyclerView(v)
            initAddCommentButton(v)
            initEmptyState(v)
            initConnectionError(v)
        }
        return v
    }

    private fun initConnectionError(v: View) {
        errorView = v.findViewById(R.id.report_problem)
    }

    private fun initEmptyState(v: View) {
        emptyStateView = v.findViewById(R.id.empty_comments)
    }

    private fun initAddCommentButton(v: View) {
        floatingActionButton = v.findViewById(R.id.add_new_comment_button) as FloatingActionButton
        floatingActionButton!!.setOnClickListener {
            if (stepId != null) {
                mShell.screenProvider.openNewCommentForm(activity, stepId, null)
            }
        }
    }

    private fun initRecyclerView(v: View) {
        recyclerView = v.findViewById(R.id.recycler_view_comments) as RecyclerView
        recyclerView.adapter = commentAdapter
        recyclerView.layoutManager = LinearLayoutManager(context)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        bus.register(this)
        showEmptyProgressOnCenter()
        if (commentManager.isEmpty()) {
            loadDiscussionProxyById()
        }
        else{
            showEmptyProgressOnCenter(false)
        }
    }

    private fun initSwipeRefreshLayout(v: View) {
        swipeRefreshLayout = v.findViewById(R.id.swipe_refresh_layout_comments) as SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener(this)
        swipeRefreshLayout.setColorSchemeResources(
                R.color.stepic_brand_primary,
                R.color.stepic_orange_carrot,
                R.color.stepic_blue_ribbon)
    }

    private fun initEmptyProgressOnCenter(v: View) {
        loadProgressBarOnCenter = v.findViewById(R.id.load_progressbar) as ProgressBar
    }

    fun initToolbar(v: View) {
        mToolbar = v.findViewById(R.id.toolbar) as Toolbar
        (activity as AppCompatActivity).setSupportActionBar(mToolbar)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun loadDiscussionProxyById(id: String = discussionId) {
        mShell.api.getDiscussionProxies(id).enqueue(object : Callback<DiscussionProxyResponse> {
            override fun onResponse(response: Response<DiscussionProxyResponse>?, retrofit: Retrofit?) {
                if (response != null && response.isSuccess) {
                    val discussionProxy = response.body().discussionProxies.firstOrNull()
                    if (discussionProxy != null && discussionProxy.discussions.isNotEmpty()) {
                        bus.post(DiscussionProxyLoadedSuccessfullyEvent(discussionProxy))
                    } else {
                        bus.post(EmptyCommentsInDiscussionProxyEvent(id))
                    }
                } else {
                    bus.post(InternetConnectionProblemInCommentsEvent(discussionId))
                }
            }

            override fun onFailure(t: Throwable?) {
                bus.post(InternetConnectionProblemInCommentsEvent(discussionId))
            }

        })
    }

    @Subscribe
    fun onInternetConnectionProblem(event: InternetConnectionProblemInCommentsEvent) {
        if (event.discussionProxyId == discussionId) {
            cancelSwipeRefresh()
            if (commentManager.isEmpty()) {
                showInternetConnectionProblem()
            } else {
                Toast.makeText(context, R.string.connectionProblems, Toast.LENGTH_SHORT).show()
            }
        }
    }

    @Subscribe
    fun onEmptyComments(event: EmptyCommentsInDiscussionProxyEvent) {
        if (event.discussionProxyId == discussionId && commentManager.isEmpty()) {
            cancelSwipeRefresh()
            showEmptyState()
        }
    }

    @Subscribe
    fun onDiscussionProxyLoadedSuccessfully(successfullyEvent: DiscussionProxyLoadedSuccessfullyEvent) {
        commentManager.setDiscussionProxy(successfullyEvent.discussionProxy)
        commentManager.loadComments()
    }


    @Subscribe
    fun onCommentsLoadedSuccessfully(successfullyEvent: CommentsLoadedSuccessfullyEvent) {
        cancelSwipeRefresh()
        showEmptyProgressOnCenter(false)
        commentAdapter.notifyDataSetChanged();
    }

    @Subscribe
    fun onNeedUpdate(needUpdateEvent : NeedReloadCommentsEvent){
        if (needUpdateEvent.targetId == stepId)
        {
            //without animation.
            onRefresh() // it can be dangerous, when 10 or more comments was submit by another users.
        }
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        bus.unregister(this)
        swipeRefreshLayout.setOnRefreshListener(null)
        floatingActionButton?.setOnClickListener(null)
    }

    override fun onRefresh() {
        commentManager.reset()
        loadDiscussionProxyById()
    }

    private fun showEmptyProgressOnCenter(needShow: Boolean = true) {
        if (needShow) {
            ProgressHelper.activate(loadProgressBarOnCenter)
            showEmptyState(false)
            showInternetConnectionProblem(false)
        } else {
            ProgressHelper.dismiss(loadProgressBarOnCenter)
        }
    }

    private fun showEmptyState(isNeedShow: Boolean = true) {
        if (isNeedShow) {
            emptyStateView.visibility = View.VISIBLE
            showEmptyProgressOnCenter(false)
            showInternetConnectionProblem(false)
        } else {
            emptyStateView.visibility = View.GONE
        }
    }

    private fun showInternetConnectionProblem(needShow: Boolean = true) {
        if (needShow) {
            errorView.visibility = View.VISIBLE
            showEmptyState(false)
            showEmptyProgressOnCenter(false)
        } else {
            errorView.visibility = View.GONE
        }
    }

    private fun cancelSwipeRefresh() {
        ProgressHelper.dismiss(swipeRefreshLayout)
    }

}