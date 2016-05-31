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
import org.stepic.droid.events.comments.CommentsLoadedSuccessfullyEvent
import org.stepic.droid.events.comments.DiscussionProxyLoadedSuccessfullyEvent
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

        fun newInstance(discussionId: String): Fragment {
            val args = Bundle()
            args.putString(discussionIdKey, discussionId)
            val fragment = CommentsFragment()
            fragment.arguments = args
            return fragment
        }
    }

    @Inject
    lateinit var commentManager: CommentManager

    lateinit var commentAdapter: CommentsAdapter

    lateinit var discussionId: String

    lateinit var mToolbar: Toolbar
    lateinit var loadProgressBarOnCenter: ProgressBar
    lateinit var swipeRefreshLayout: SwipeRefreshLayout
    lateinit var recyclerView: RecyclerView
    var floatingActionButton: FloatingActionButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        MainApplication.component().inject(this)
        commentAdapter = CommentsAdapter(commentManager, context)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater?.inflate(R.layout.fragment_comments, container, false)
        discussionId = arguments.getString(discussionIdKey)
        setHasOptionsMenu(true)
        v?.let {
            initToolbar(v)
            initEmptyProgressOnCenter(v)
            initSwipeRefreshLayout(v)
            initRecyclerView(v)
            initAddCommentButton(v)
        }
        return v
    }

    private fun initAddCommentButton(v: View) {
        floatingActionButton = v.findViewById(R.id.add_new_comment_button) as FloatingActionButton
        floatingActionButton!!.setOnClickListener { mShell.screenProvider.openNewCommentForm(activity) }
    }

    private fun initRecyclerView(v: View) {
        recyclerView = v.findViewById(R.id.recycler_view_comments) as RecyclerView
        recyclerView.adapter = commentAdapter
        recyclerView.layoutManager = LinearLayoutManager(context)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        showEmptyProgressOnCenter()
        loadDiscussionProxyById()
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
                    if (discussionProxy != null) {
                        bus.post(DiscussionProxyLoadedSuccessfullyEvent(discussionProxy))
                    } else {
                        //TODO IMPLEMENT ON FAIL.
                    }
                } else {
                    //TODO IMPLEMENT ON FAIL
                }
            }

            override fun onFailure(t: Throwable?) {
                //TODO IMPLEMENT ON FAIL
            }

        })
    }

    @Subscribe
    fun onDiscussionProxyLoadedSuccessfully(successfullyEvent: DiscussionProxyLoadedSuccessfullyEvent) {
        commentManager.setDiscussionProxy(successfullyEvent.discussionProxy)
        commentManager.loadComments()
    }


    @Subscribe
    fun onCommentsLoadedSuccessfully(successfullyEvent: CommentsLoadedSuccessfullyEvent) {
        ProgressHelper.dismiss(loadProgressBarOnCenter)
        commentAdapter.notifyDataSetChanged();
    }

    override fun onStart() {
        super.onStart()
        bus.register(this)
    }

    override fun onStop() {
        super.onStop()
        bus.unregister(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        swipeRefreshLayout.setOnRefreshListener(null)
        floatingActionButton?.setOnClickListener(null)
    }

    override fun onRefresh() {
        Toast.makeText(context, "Hey, try refresh need implementing!", Toast.LENGTH_LONG).show()
        // todo implement
    }

    private fun showEmptyProgressOnCenter(needShow: Boolean = true) {
        if (needShow) {
            ProgressHelper.activate(loadProgressBarOnCenter)
        } else {
            ProgressHelper.dismiss(loadProgressBarOnCenter)
        }
    }

}