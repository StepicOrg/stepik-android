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
    private var discussionProxy: DiscussionProxy? = null
    val parentCommentToSumOfCachedReplies: MutableMap<Long, Int> = HashMap()
    val cachedCommentsSetMap: MutableMap<Long, Comment> = HashMap()
    val cachedCommentsList: MutableList<Comment> = ArrayList()
    val userSetMap: MutableMap<Int, User> = HashMap() //userId -> User
    val replyToPositionInParentMap: MutableMap<Long, Int> = HashMap()
    val parentIdToPositionInDiscussionMap: MutableMap<Long, Int> = HashMap()

    @Inject
    constructor() {
        MainApplication.component().inject(this)
    }

    fun loadComments() {
        val orderOfComments = discussionProxy?.discussions
        orderOfComments?.let {
            val sizeNeedLoad = Math.min((sumOfCachedParent + maxOfParentInQuery), orderOfComments.size)
            if (sizeNeedLoad == sumOfCachedParent || sizeNeedLoad == 0) {
                // we don't need to load comments
                return
            }

            val idsForLoading = it.subList(sumOfCachedParent, sumOfCachedParent + sizeNeedLoad).toLongArray()
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
                                    if (it.id != null && it.id !in cachedCommentsSetMap) {
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
                                    replyToPositionInParentMap.put(childComment.id!!, childIndex)
                                    cachedCommentsList.add(childComment)
                                    i++
                                    childIndex++
                                }
                            }
                        }

                        stepicResponse.users
                                .forEach {
                                    if (it.id !in userSetMap) {
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

    fun getSize() = cachedCommentsList.size

    fun getItemWithNeedUpdatingInfoByPosition(position: Int): Pair<Boolean, Comment> {
        val comment: Comment = cachedCommentsList[position]
        return getCommentAndNeedUpdateBase(comment)
    }

    private fun getCommentAndNeedUpdateBase(comment: Comment): Pair<Boolean, Comment> {
        var needUpdate = false
        val parentComment: Comment? = cachedCommentsSetMap[comment.parent] //comment.parent can be null

        if (parentComment == null) {
            //comment is parent comment
            val positionInDiscussion = parentIdToPositionInDiscussionMap[comment.id]!!
            if (discussionProxy!!.discussions.size > sumOfCachedParent && (positionInDiscussion + 1) == sumOfCachedParent ) {
                needUpdate = true
            }

        } else {
            //comment is reply
            val pos: Int = replyToPositionInParentMap[comment.id]!!
            val numberOfCached = parentCommentToSumOfCachedReplies[parentComment.id]
            if ((pos + 1) == numberOfCached && parentComment.reply_count > numberOfCached) {
                needUpdate = true
            }
        }
        return Pair(needUpdate, comment)
    }

    fun getUserById(userId: Int) = userSetMap[userId]

    fun isNeedUpdateParentInReply(commentReply: Comment): Boolean {
        val positionInParent = replyToPositionInParentMap[commentReply.id]
        if (discussionProxy!!.discussions.size > sumOfCachedParent) {
            //need update parent:
            //and it is last cached reply?
            val positionInParent = replyToPositionInParentMap[commentReply.id]
            val numberOfCached = parentCommentToSumOfCachedReplies[commentReply.parent]
            if (numberOfCached != null && positionInParent != null && (positionInParent + 1) == numberOfCached) {
                return true
            }
        }
        return false
    }

    fun setDiscussionProxy(dP: DiscussionProxy) {
        discussionProxy = dP
        //todo: remove dp from manager, make only list in discussion proxy based on sorting
        var i = 0
        while (i < dP.discussions.size) {
            parentIdToPositionInDiscussionMap.put(dP.discussions[i], i)
            i++
        }
    }

}
