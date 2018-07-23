package org.stepic.droid.model

import org.stepik.android.model.comments.Comment

data class CommentAdapterItem (val isNeedUpdating : Boolean, val isLoading : Boolean, val comment: Comment, val isParentLoading : Boolean)
