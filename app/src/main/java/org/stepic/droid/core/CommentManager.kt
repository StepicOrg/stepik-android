package org.stepic.droid.core

import com.squareup.otto.Bus

import org.stepic.droid.base.MainApplication
import org.stepic.droid.events.comments.CommentsLoadedSuccessfullyEvent
import org.stepic.droid.events.comments.InternetConnectionProblemInCommentsEvent
import org.stepic.droid.model.CommentAdapterItem
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
    private val parentCommentToSumOfCachedReplies: MutableMap<Long, Int> = HashMap()
    private val cachedCommentsSetMap: MutableMap<Long, Comment> = HashMap()
    private val cachedCommentsList: MutableList<Comment> = ArrayList()
    private val userSetMap: MutableMap<Int, User> = HashMap() //userId -> User
    private val replyToPositionInParentMap: MutableMap<Long, Int> = HashMap()
    private val parentIdToPositionInDiscussionMap: MutableMap<Long, Int> = HashMap()
    private val repliesIdIsLoading: MutableSet<Long> = HashSet()
    private val commentIdIsLoading: MutableSet<Long> = HashSet() //can be reply or comment (with 0 replies) for load more comments).

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
                bus.post(CommentsLoadedSuccessfullyEvent()) // notify UI
                return
            }

            val idsForLoading = it.subList(sumOfCachedParent, sizeNeedLoad).toLongArray()
            loadCommentsByIds(idsForLoading)
        }
    }

    fun loadExtraReplies(oneOfReplyComment: Comment) {
        val parentCommentId = oneOfReplyComment.parent
        val parentComment = cachedCommentsSetMap[parentCommentId]
        if (parentComment != null && parentComment.replies != null) {
            val countOfCachedReplies: Int = parentCommentToSumOfCachedReplies[parentCommentId] ?: return

            val sizeNeedLoad = Math.min(parentComment.replies.size, countOfCachedReplies + maxOfRepliesInQuery)
            if (sizeNeedLoad == countOfCachedReplies || sizeNeedLoad == 0) {
                bus.post(CommentsLoadedSuccessfullyEvent()) // notify UI
                return
            }

            val idsForLoading = parentComment.replies.subList(countOfCachedReplies, sizeNeedLoad).toLongArray()
            loadCommentsByIds(idsForLoading, fromReply = true)
        }
    }

    fun loadCommentsByIds(idsForLoading: LongArray, fromReply: Boolean = false) {
        api.getCommentsByIds(idsForLoading).enqueue(object : Callback<CommentsResponse> {
            override fun onResponse(response: Response<CommentsResponse>?, retrofit: Retrofit?) {

                if (response != null && response.isSuccess) {
                    val stepicResponse = response.body()
                    if (stepicResponse != null) {
                        stepicResponse.comments
                                ?.forEach {
                                    if (it.id != null) {
                                        val previousValue: Comment? = cachedCommentsSetMap.put(it.id, it)
                                        val parentId: Long? = it.parent
                                        if (parentId != null && previousValue == null) {
                                            //first time
                                            var numberOfCachedBefore: Int = parentCommentToSumOfCachedReplies[parentId] ?: 0
                                            numberOfCachedBefore++
                                            parentCommentToSumOfCachedReplies[parentId] = numberOfCachedBefore
                                        }
                                    }
                                }
                        sumOfCachedParent = cachedCommentsSetMap.filter { it.value.parent == null }.size

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
                                while (childIndex < cachedRepliesNumber) {
                                    val childComment = cachedCommentsSetMap [parentComment.replies[childIndex]] ?: break
                                    replyToPositionInParentMap.put(childComment.id!!, childIndex)
                                    cachedCommentsList.add(childComment)
                                    childIndex++
                                }
                            }
                        }

                        stepicResponse.users
                                ?.forEach {
                                    if (it.id !in userSetMap) {
                                        userSetMap.put(it.id, it)
                                    }
                                }
                        //commentIdIsLoading = commentIdIsLoading.filterNot { cachedCommentsSetMap.containsKey(it) }.toHashSet()
                        if (fromReply) {
                            repliesIdIsLoading.clear()
                        } else {
                            commentIdIsLoading.clear()
                        }
                        bus.post(CommentsLoadedSuccessfullyEvent()) // notify UI
                    } else {
                        bus.post(InternetConnectionProblemInCommentsEvent(discussionProxy!!.id))
                    }
                } else {
                    bus.post(InternetConnectionProblemInCommentsEvent(discussionProxy!!.id))
                }
            }

            override fun onFailure(t: Throwable?) {
                bus.post(InternetConnectionProblemInCommentsEvent(discussionProxy!!.id))
            }
        })
    }

    fun getSize() = cachedCommentsList.size

    fun getItemWithNeedUpdatingInfoByPosition(position: Int): CommentAdapterItem {
        val comment: Comment = cachedCommentsList[position]
        return getCommentAndNeedUpdateBase(comment)
    }

    private fun getCommentAndNeedUpdateBase(comment: Comment): CommentAdapterItem {
        var needUpdate = false
        val parentComment: Comment? = cachedCommentsSetMap[comment.parent] //comment.parent can be null

        if (parentComment == null) {
            //comment is parent comment
            val positionInDiscussion = parentIdToPositionInDiscussionMap[comment.id]!!
            if (discussionProxy!!.discussions.size > sumOfCachedParent && (positionInDiscussion + 1) == sumOfCachedParent) {
                needUpdate = true
            }

        } else {
            //comment is reply
            val pos: Int = replyToPositionInParentMap[comment.id]!!
            val numberOfCached = parentCommentToSumOfCachedReplies[parentComment.id]
            if ((pos + 1) == numberOfCached && parentComment.reply_count ?: 0 > numberOfCached) {
                needUpdate = true
            }
        }

        val isLoading = repliesIdIsLoading.contains(comment.id)
        val isParentLoading = commentIdIsLoading.contains(comment.id)
        return CommentAdapterItem(isNeedUpdating = needUpdate, isLoading = isLoading, comment = comment, isParentLoading = isParentLoading)
    }

    fun addToLoading(commentId: Long) {
        repliesIdIsLoading.add(commentId)
    }

    fun getUserById(userId: Int) = userSetMap[userId]

    fun isNeedUpdateParentInReply(commentReply: Comment): Boolean {
        val positionInParent = replyToPositionInParentMap[commentReply.id]
        if (discussionProxy!!.discussions.size > sumOfCachedParent) {
            //need update parent:
            //and it is last cached reply?
            val positionInParent = replyToPositionInParentMap[commentReply.id]
            val numberOfCached = parentCommentToSumOfCachedReplies[commentReply.parent]
            val posInDisscussion = parentIdToPositionInDiscussionMap[commentReply.parent] ?: return false
            if (numberOfCached != null && positionInParent != null && (positionInParent + 1) == numberOfCached && (posInDisscussion + 1) == sumOfCachedParent) {
                return true
            }
        }
        return false
    }

    fun setDiscussionProxy(dP: DiscussionProxy) {
        discussionProxy = dP
        //todo: remove dp from manager, make only list in discussion proxy based on sorting and discussion id!
        var i = 0
        while (i < dP.discussions.size) {
            parentIdToPositionInDiscussionMap.put(dP.discussions[i], i)
            i++
        }
    }

    fun addCommentIdWhereLoadMoreClicked(commentId: Long) {
        commentIdIsLoading.add(commentId)
    }

    fun isEmpty() = cachedCommentsList.isEmpty()

    fun reset() {
        sumOfCachedParent = 0
    }

    fun isCommentCached(commentId: Long?) : Boolean {
        if (commentId == null) {
            return false
        } else {
            return cachedCommentsSetMap.containsKey(commentId)
        }
    }

}
