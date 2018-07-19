package org.stepic.droid.core

import org.stepic.droid.core.comments.contract.CommentsPoster
import org.stepic.droid.di.comment.CommentsScope
import org.stepic.droid.model.CommentAdapterItem
import org.stepik.android.model.comments.Comment
import org.stepik.android.model.comments.DiscussionProxy
import org.stepik.android.model.comments.Vote
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.web.Api
import org.stepic.droid.web.CommentsResponse
import org.stepik.android.model.user.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import javax.inject.Inject

@CommentsScope
class CommentManager @Inject constructor(
        private val api: Api,
        private val commentsPoster: CommentsPoster,
        private val sharedPrefs: SharedPreferenceHelper
) {

    private var discussionProxy: DiscussionProxy? = null
    private val discussionOrderList: MutableList<Long> = ArrayList()

    private val maxOfParentInQuery = 10 // server supports 20, but we can change it
    private val maxOfRepliesInQuery = 20 // we can't change it
    private var sumOfCachedParent: Int = 0
    private var discussionProxyId: String? = null
    private val parentCommentToSumOfCachedReplies: MutableMap<Long, Int> = HashMap()
    private val cachedCommentsSetMap: MutableMap<Long, Comment> = HashMap()
    private val cachedCommentsList: MutableList<Comment> = ArrayList()
    private val userSetMap: MutableMap<Long, User> = HashMap() //userId -> User
    private val replyToPositionInParentMap: MutableMap<Long, Int> = HashMap()
    private val parentIdToPositionInDiscussionMap: MutableMap<Long, Int> = HashMap()
    private val repliesIdIsLoading: MutableSet<Long> = HashSet()
    private val commentIdIsLoading: MutableSet<Long> = HashSet() //can be reply or comment (with 0 replies) for load more comments).
    private val voteMap: MutableMap<String, Vote> = HashMap()

    fun loadComments() {
        val orderOfComments = discussionOrderList
        orderOfComments.let {
            val sizeNeedLoad = Math.min((sumOfCachedParent + maxOfParentInQuery), orderOfComments.size)
            if (sizeNeedLoad == sumOfCachedParent || sizeNeedLoad == 0) {
                // we don't need to load comments
                commentsPoster.commentsLoaded()
                return
            }

            val idsForLoading = it.subList(sumOfCachedParent, sizeNeedLoad).toLongArray()
            loadCommentsByIds(idsForLoading)
        }
    }

    fun loadExtraReplies(oneOfReplyComment: Comment) {
        val parentCommentId = oneOfReplyComment.parent
        val parentCommentReplies = cachedCommentsSetMap[parentCommentId]?.replies

        if (parentCommentReplies != null) {
            val countOfCachedReplies: Int = parentCommentToSumOfCachedReplies[parentCommentId] ?: return

            val sizeNeedLoad = Math.min(parentCommentReplies.size, countOfCachedReplies + maxOfRepliesInQuery)
            if (sizeNeedLoad == countOfCachedReplies || sizeNeedLoad == 0) {
                commentsPoster.commentsLoaded()
                return
            }

            val idsForLoading = parentCommentReplies.subList(countOfCachedReplies, sizeNeedLoad).toLongArray()
            loadCommentsByIds(idsForLoading, fromReply = true)
        }
    }

    private fun addComments(stepicResponse: CommentsResponse, fromReply: Boolean = false) {
        updateOnlyCommentsIfCachedSilent(stepicResponse.comments)
        stepicResponse.users
                ?.forEach {
                    if (it.id !in userSetMap) {
                        userSetMap[it.id] = it
                    }
                }
        stepicResponse.votes?.forEach {
            //updating info
            voteMap[it.id] = it
        }
        //commentIdIsLoading = commentIdIsLoading.filterNot { cachedCommentsSetMap.containsKey(it) }.toHashSet()
        if (fromReply) {
            repliesIdIsLoading.clear()
        } else {
            commentIdIsLoading.clear()
        }
        commentsPoster.commentsLoaded()
    }

    fun updateOnlyCommentsIfCachedSilent(comments: List<Comment>?) {
        comments?.forEach {
            val previousValue: Comment? = cachedCommentsSetMap.put(it.id, it)
            val parentId: Long? = it.parent
            if (parentId != null && (previousValue == null)) {
                //first time
                var numberOfCachedBefore: Int = parentCommentToSumOfCachedReplies[parentId] ?: 0
                numberOfCachedBefore++
                parentCommentToSumOfCachedReplies[parentId] = numberOfCachedBefore
            }
        }
        sumOfCachedParent = cachedCommentsSetMap.filter { it.value.parent == null }.size
        if (sumOfCachedParent > discussionOrderList.size) {
            sumOfCachedParent = discussionOrderList.size
        }
        cachedCommentsList.clear()
        var i = 0
        var j = 0
        while (i < sumOfCachedParent) {
            val parentCommentId = discussionOrderList[j]
            j++

            val parentComment = cachedCommentsSetMap[parentCommentId] ?: break
            val parentCommentReplies = parentComment.replies
            cachedCommentsList.add(parentComment)
            i++
            if (parentCommentReplies != null && !parentCommentReplies.isEmpty()) {
                var childIndex = 0
                if (parentCommentToSumOfCachedReplies[parentComment.id] ?: 0 > parentComment.replyCount ?: 0) {
                    parentCommentToSumOfCachedReplies[parentComment.id] = parentComment.replyCount!! //if we remove some reply
                }
                val cachedRepliesNumber = parentCommentToSumOfCachedReplies[parentComment.id] ?: 0

                while (childIndex < cachedRepliesNumber/* && childIndex < parentComment.reply_count?:-1*/) {
                    val childComment: Comment? = cachedCommentsSetMap [parentCommentReplies[childIndex]]
                    if (childComment != null) {
                        replyToPositionInParentMap[childComment.id] = childIndex
                        cachedCommentsList.add(childComment)
                        childIndex++
                    } else {
                        //reply childIndex not found
                        parentCommentToSumOfCachedReplies[parentCommentId] = childIndex
                        for (indexForDelete in childIndex until parentCommentReplies.size) {
                            cachedCommentsSetMap.remove(parentCommentReplies[indexForDelete])
                        }
                        break
                    }
                }
            }
        }

    }

    fun loadCommentsByIds(idsForLoading: LongArray, fromReply: Boolean = false) {
        api.getCommentsByIds(idsForLoading).enqueue(object : Callback<CommentsResponse> {

            override fun onResponse(call: Call<CommentsResponse>?, response: Response<CommentsResponse>?) {

                if (response != null && response.isSuccessful) {
                    val stepicResponse = response.body()
                    if (stepicResponse != null) {
                        addComments(stepicResponse, fromReply)
                    } else {
                        commentsPoster.connectionProblem()
                    }
                } else {
                    commentsPoster.connectionProblem()
                }
            }

            override fun onFailure(call: Call<CommentsResponse>?, t: Throwable?) {
                commentsPoster.connectionProblem()
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
            if (discussionOrderList.size > sumOfCachedParent && (positionInDiscussion + 1) == sumOfCachedParent) {
                needUpdate = true
            }

        } else {
            //comment is reply
            val pos: Int = replyToPositionInParentMap[comment.id]!!
            val numberOfCached = parentCommentToSumOfCachedReplies[parentComment.id]
            if ((pos + 1) == numberOfCached && parentComment.replyCount ?: 0 > numberOfCached) {
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

    fun getUserById(userId: Long) = userSetMap[userId]

    fun isNeedUpdateParentInReply(commentReply: Comment): Boolean {
        if (discussionOrderList.size > sumOfCachedParent) {
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
        discussionProxyId = dP.id
        var i = 0
        val list = sharedPrefs.discussionOrder.getOrder(dP)
        discussionOrderList.clear()
        discussionOrderList.addAll(list)
        while (i < list.size) {
            parentIdToPositionInDiscussionMap[list[i]] = i
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

    fun isCommentCached(commentId: Long?): Boolean = if (commentId == null) {
        false
    } else {
        cachedCommentsSetMap.containsKey(commentId)
    }

    fun getVoteByVoteId(voteId: String): Vote? = voteMap[voteId]

    fun resetAll(dP: DiscussionProxy? = null) {
        parentIdToPositionInDiscussionMap.clear()
        if (dP != null) {
            setDiscussionProxy(dP)
        } else {
            setDiscussionProxy(discussionProxy!!)
        }
        sumOfCachedParent = 0
        parentCommentToSumOfCachedReplies.clear()
        cachedCommentsSetMap.clear()
        cachedCommentsList.clear()
        userSetMap.clear()
        replyToPositionInParentMap.clear()
        repliesIdIsLoading.clear()
        commentIdIsLoading.clear()
        voteMap.clear()
    }

    fun clearAllLoadings() {
        commentIdIsLoading.clear()
        repliesIdIsLoading.clear()
    }

    fun isDiscussionProxyNull() = (discussionProxyId == null)

}
