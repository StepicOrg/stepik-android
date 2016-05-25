package org.stepic.droid.view.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import com.squareup.otto.Subscribe
import org.stepic.droid.R
import org.stepic.droid.base.FragmentBase
import org.stepic.droid.base.MainApplication
import org.stepic.droid.core.CommentManager
import org.stepic.droid.events.comments.CommentsLoadedSuccessfullyEvent
import org.stepic.droid.events.comments.DiscussionProxyLoadedSuccessfullyEvent
import org.stepic.droid.model.comments.Comment
import org.stepic.droid.model.comments.DiscussionProxy
import org.stepic.droid.util.ProgressHelper
import org.stepic.droid.web.DiscussionProxyResponse
import retrofit.Callback
import retrofit.Response
import retrofit.Retrofit
import java.util.*
import javax.inject.Inject

class CommentsFragment : FragmentBase() {
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
    lateinit var commentManager : CommentManager

    lateinit var discussionId: String

    lateinit var mToolbar: Toolbar
    lateinit var discussionTv: TextView
    lateinit var loadProgressBarOnCenter: ProgressBar

    val commentsList: MutableList<Comment> = ArrayList()
    val commentsIdSet: MutableSet<Long> = HashSet() //it is set of comments, which are already on screen
    var discussionProxy: DiscussionProxy? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        MainApplication.component().inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater?.inflate(R.layout.fragment_comments, container, false)
        discussionId = arguments.getString(discussionIdKey)
        setHasOptionsMenu(true)
        v?.let {
            initToolbar(v)
            initDiscussionTv(v)
            initEmptyProgressOnCenter(v)
        }
        return v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        showEmptyProgressOnCenter()
        loadDiscussionProxyById()
    }

    private fun initDiscussionTv(v: View) {
        discussionTv = v.findViewById(R.id.discussionTv) as TextView
        discussionTv.text = discussionId
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

    private fun loadComments(ids : LongArray) {
//if order is changed --> clear list and set, where it was changed

    }

    @Subscribe
    fun onDiscussionProxyLoadedSuccessfully(successfullyEvent: DiscussionProxyLoadedSuccessfullyEvent) {
        discussionProxy = successfullyEvent.discussionProxy
//        loadComments()
        val e = 0
    }


    @Subscribe
    fun onCommentsLoadedSuccessfully(successfullyEvent: CommentsLoadedSuccessfullyEvent) {

        successfullyEvent.comments
                .forEach {
                    if (it.id != null && !commentsIdSet.contains(it.id))
                        commentsIdSet.add(it.id)
                    commentsList.add(it)
                }
        //now in list we have new comments, which we should show
    }

    override fun onStart() {
        super.onStart()
        bus.register(this)
    }

    override fun onStop() {
        super.onStop()
        bus.unregister(this)
    }


    private fun showEmptyProgressOnCenter(needShow: Boolean = true) {
        if (needShow) {
            ProgressHelper.activate(loadProgressBarOnCenter)
        } else {
            ProgressHelper.dismiss(loadProgressBarOnCenter)
        }
    }

}