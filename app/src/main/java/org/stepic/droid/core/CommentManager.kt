package org.stepic.droid.core

import com.squareup.otto.Bus

import org.stepic.droid.base.MainApplication
import org.stepic.droid.events.comments.CommentsLoadedSuccessfullyEvent
import org.stepic.droid.model.User
import org.stepic.droid.model.comments.Comment
import org.stepic.droid.model.comments.DiscussionProxy
import org.stepic.droid.web.CommentsResponse
import org.stepic.droid.web.IApi
import retrofit.Callback
import retrofit.Response
import retrofit.Retrofit
import java.util.*

import javax.inject.Inject

class CommentManager {

    @Inject
    lateinit var bus: Bus

    @Inject
    lateinit var api: IApi

    private val maxOfParentInQuery = 10 // server supports 20, but we can change it
    private val maxOfRepliesInQuery = 20 // we can't change it
    private var sumOfCachedParent: Int = 0;
    var discussionProxy: DiscussionProxy? = null
    val parentCommentToSumOfCachedReplies: MutableMap<Long, Int> = HashMap()
    val cachedCommentsSetMap: MutableMap<Long, Comment> = HashMap()
    val cachedCommentsList: MutableList<Comment> = ArrayList()
    val userSetMap: MutableMap<Int, User> = HashMap() //userId -> User


    @Inject
    constructor() {
        MainApplication.component().inject(this)
    }

    fun loadComments() {
        val orderOfComments = discussionProxy?.discussions
        orderOfComments?.let {
            val maxSize = Math.max((sumOfCachedParent + maxOfParentInQuery), orderOfComments.size)
            if (maxSize == sumOfCachedParent) {
                // we don't need to load comments
                return
            }

            val idsForLoading = it.subList(sumOfCachedParent, sumOfCachedParent + maxOfParentInQuery).toLongArray()
            loadCommentsByIds(idsForLoading)
        }
    }

    private fun loadCommentsByIds(idsForLoading: LongArray) {
        api.getCommentsByIds(idsForLoading).enqueue(object : Callback<CommentsResponse> {
            override fun onResponse(response: Response<CommentsResponse>?, retrofit: Retrofit?) {

                if (response != null && response.isSuccess) {
                    val stepicResponse = response.body()
                    if (stepicResponse != null) {
                        stepicResponse.comments
                                .forEach {
                                    if (it.id != null && !(it.id in cachedCommentsSetMap)) {
                                        cachedCommentsSetMap.put(it.id, it)
                                        if (it.parent == null) {
                                            sumOfCachedParent++;
                                        } else {
                                            var numberOfCachedBefore: Int = parentCommentToSumOfCachedReplies[it.parent] ?: 0
                                            numberOfCachedBefore++
                                            parentCommentToSumOfCachedReplies[it.parent] = numberOfCachedBefore
                                        }
                                    }
                                }

                        cachedCommentsList.clear()
                        var i = 0
                        var j = 0
                        while (i < sumOfCachedParent) {
                            val parentCommentId = discussionProxy?.discussions?.get(j)
                            j++

                            val parentComment = cachedCommentsSetMap[parentCommentId] ?: break
                            cachedCommentsList.add(parentComment)
                            i++
                            if (parentComment.replies != null && !parentComment.replies.isEmpty()) {
                                var childIndex = 0
                                val cachedRepliesNumber = parentCommentToSumOfCachedReplies.get(parentComment.id) ?: 0
                                while (i < sumOfCachedParent && childIndex < cachedRepliesNumber) {
                                    val childComment = cachedCommentsSetMap [parentComment.replies[childIndex]] ?: break
                                    cachedCommentsList.add(childComment)
                                    i++
                                    childIndex++
                                }
                            }
                        }

                        stepicResponse.users
                                .forEach {
                                    if (!(it.id in userSetMap)) {
                                        userSetMap.put(it.id, it)
                                    }
                                }
                        bus.post(CommentsLoadedSuccessfullyEvent()) // notify UI
                    } else {
                        //todo on fail
                    }
                } else {
                    //todo on fail
                }
            }

            override fun onFailure(t: Throwable?) {
                //todo on fail
            }
        })
    }


}
