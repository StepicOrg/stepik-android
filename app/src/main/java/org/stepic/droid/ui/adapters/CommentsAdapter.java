package org.stepic.droid.ui.adapters;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.facebook.common.util.UriUtil;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.DraweeView;

import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.stepic.droid.R;
import org.stepic.droid.core.CommentManager;
import org.stepic.droid.model.CommentAdapterItem;
import org.stepic.droid.model.User;
import org.stepic.droid.model.comments.Comment;
import org.stepic.droid.model.comments.Vote;
import org.stepic.droid.model.comments.VoteValue;
import org.stepic.droid.util.AppConstants;
import org.stepic.droid.util.ColorUtil;
import org.stepic.droid.util.DateTimeHelper;
import org.stepic.droid.util.RWLocks;
import org.stepic.droid.ui.custom.LatexSupportableEnhancedFrameLayout;

import java.util.Locale;

import butterknife.BindView;
import butterknife.BindString;
import butterknife.ButterKnife;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.GenericViewHolder> {

    public static final int TYPE_PARENT_COMMENT = 1;
    public static final int TYPE_REPLY = 2;

    private CommentManager commentManager;
    private Context context;

    public DateTimeZone zone;
    public Locale locale;

    public CommentsAdapter(CommentManager commentManager, Context context) {
        this.commentManager = commentManager;
        this.context = context;
        zone = DateTimeZone.getDefault();
        locale = Locale.getDefault();
    }

    @Override
    public GenericViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_PARENT_COMMENT) {
            View v = LayoutInflater.from(context).inflate(R.layout.comment_item, parent, false);
            return new CommentsViewHolder(v);
        } else if (viewType == TYPE_REPLY) {
            View v = LayoutInflater.from(context).inflate(R.layout.reply_item, parent, false);
            return new ReplyViewHolder(v);
        } else {
            return null;
        }
    }

    @Override
    public int getItemViewType(int position) {
        CommentAdapterItem needUpdateAndComment = commentManager.getItemWithNeedUpdatingInfoByPosition(position);
        Long parentId = needUpdateAndComment.getComment().getParent();
        if (parentId == null) {
            return TYPE_PARENT_COMMENT;
        } else {
            return TYPE_REPLY;
        }
    }

    @Override
    public void onBindViewHolder(GenericViewHolder holder, int position) {
        holder.setDataOnView(position);
    }

    @Override
    public int getItemCount() {
        return commentManager.getSize();
    }

    class CommentsViewHolder extends GenericViewHolder {

        public CommentsViewHolder(View itemView) {
            super(itemView);
        }

        @Override
        public void setDataOnView(int position) {
            CommentAdapterItem needUpdateAndComment = commentManager.getItemWithNeedUpdatingInfoByPosition(position);
            initialSetUp(needUpdateAndComment);
            boolean needUpdate = needUpdateAndComment.isNeedUpdating();
            Comment comment = needUpdateAndComment.getComment();

            if (needUpdateAndComment.isParentLoading()) {
                loadMoreParentProgressState();
            } else {
                if (comment.getReply_count() == 0 && needUpdate) {
                    loadMoreSuggestLoadingState();
                } else {
                    loadMoreHide();
                }
            }
        }


    }

    class ReplyViewHolder extends GenericViewHolder {

        @BindView(R.id.load_more_reply)
        View loadMoreReply;

        @BindView(R.id.progress_load_more_replies)
        ProgressBar progressLoadMoreReplies;

        public ReplyViewHolder(View itemView) {
            super(itemView);
            loadMoreReply.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickLoadMoreReplies(getAdapterPosition());
                }
            });
        }

        void onClickLoadMoreReplies(int position) {
            if (position < 0 && position >= commentManager.getSize()) return;

            CommentAdapterItem needUpdateAndComment = commentManager.getItemWithNeedUpdatingInfoByPosition(position);
            Comment comment = needUpdateAndComment.getComment();
            commentManager.addToLoading(comment.getId());
            notifyItemChanged(position);
            commentManager.loadExtraReplies(comment);

        }

        @Override
        public void setDataOnView(int position) {
            CommentAdapterItem needUpdateAndComment = commentManager.getItemWithNeedUpdatingInfoByPosition(position);
            initialSetUp(needUpdateAndComment);
            Comment comment = needUpdateAndComment.getComment();

            if (!needUpdateAndComment.isLoading()) {
                progressLoadMoreReplies.setVisibility(View.GONE);
                boolean isNeedUpdate = needUpdateAndComment.isNeedUpdating();
                if (isNeedUpdate) {
                    loadMoreReply.setVisibility(View.VISIBLE);
                } else {
                    loadMoreReply.setVisibility(View.GONE);
                }
            } else {
                progressLoadMoreReplies.setVisibility(View.VISIBLE);
            }

            boolean isNeedUpdateParent = commentManager.isNeedUpdateParentInReply(comment);
            if (needUpdateAndComment.isParentLoading()) {
                loadMoreParentProgressState();
            } else {
                if (isNeedUpdateParent) {
                    loadMoreSuggestLoadingState();
                } else {
                    loadMoreHide();
                }
            }

        }
    }

    abstract class GenericViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.enhanced_text_view)
        LatexSupportableEnhancedFrameLayout commentTextEnhanced;

        @BindView(R.id.user_icon)
        DraweeView userIcon;

        @BindView(R.id.comment_clickable_root)
        ViewGroup commentClickableRoot;

        @BindView(R.id.user_name_comments)
        TextView userName;

        @BindView(R.id.load_more_view)
        View loadMoreView;

        @BindView(R.id.load_more_textview)
        View loadMoreTextView;

        @BindView(R.id.progress_load_more_comments)
        View progressLoadMoreComments;

        @BindView(R.id.like_root)
        View likeRoot;

        @BindView(R.id.like_count)
        TextView likeCount;

        @BindView(R.id.like_image)
        ImageView likeImage;

        @BindView(R.id.comment_time_tv)
        TextView commentTimeTextView;

        @BindView(R.id.pinned_indicator)
        View pinnedIndicator;

        @BindView(R.id.user_role)
        TextView userRole;

        @BindString(R.string.comment_is_deleted)
        String commentIsDeletedMessage;

        Drawable likeActiveDrawable;
        Drawable likeEmptyDrawable;

        public GenericViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            loadMoreView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        RWLocks.LoadMoreLock.writeLock().lock();
                        if (loadMoreTextView.getVisibility() == View.VISIBLE) {
                            onClickMoreLayout(getAdapterPosition());
                        }
                    } finally {
                        RWLocks.LoadMoreLock.writeLock().unlock();
                    }
                }
            });
            commentClickableRoot.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                    itemView.showContextMenu();
                    return true;
                }
            });

            commentClickableRoot.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    itemView.showContextMenu();
                }
            });

            likeActiveDrawable = ContextCompat.getDrawable(context, R.drawable.ic_thumb_up_white_24dp).mutate();
            likeEmptyDrawable = ContextCompat.getDrawable(context, R.drawable.ic_thumb_up_white_24dp).mutate();

            likeEmptyDrawable.setColorFilter(ColorUtil.INSTANCE.getColorArgb(R.color.material_grey, context), PorterDuff.Mode.MULTIPLY);
            likeActiveDrawable.setColorFilter(ColorUtil.INSTANCE.getColorArgb(R.color.stepic_blue_ribbon, context), PorterDuff.Mode.MULTIPLY);

        }

        private void onClickMoreLayout(int adapterPosition) {
            CommentAdapterItem commentAdapterItem = commentManager.getItemWithNeedUpdatingInfoByPosition(adapterPosition);
            commentManager.addCommentIdWhereLoadMoreClicked(commentAdapterItem.getComment().getId());
            notifyItemChanged(adapterPosition);
            commentManager.loadComments();
        }

        public abstract void setDataOnView(int position);

        protected final void initialSetUp(CommentAdapterItem needUpdateAndComment) {
            final Comment comment = needUpdateAndComment.getComment();

            final boolean isParent = comment.getParent() == null;

            if (comment.is_deleted() != null && comment.is_deleted()) {
                commentClickableRoot.setBackgroundColor(ColorUtil.INSTANCE.getColorArgb(R.color.wrong_answer_background, context));

                if (comment.getText() != null && !comment.getText().isEmpty()) {
                    int weakColorInt = ColorUtil.INSTANCE.getColorArgb(R.color.stepic_weak_text, context);
                    String hexColor = String.format("#%06X", (0xFFFFFF & weakColorInt));
                    String deletedCommentWithText = commentIsDeletedMessage + "<br>" + "<font color='" + hexColor + "'>" + comment.getText() + "</font>";
                    commentTextEnhanced.setText(deletedCommentWithText);
                } else {
                    commentTextEnhanced.setText(commentIsDeletedMessage);
                }
            } else {
                // not deleted
                commentClickableRoot.setBackgroundColor(ColorUtil.INSTANCE.getColorArgb(R.color.white, context));
                commentTextEnhanced.setText(comment.getText());
            }

            if (comment.getTime() != null && !comment.getTime().isEmpty()) {
                commentTimeTextView.setText(DateTimeHelper.INSTANCE.getPresentOfDate(comment.getTime(), DateTimeFormat.forPattern(AppConstants.COMMENT_DATE_TIME_PATTERN).withZone(zone).withLocale(locale)));
            } else {
                commentTimeTextView.setText("");
            }

            int epicCount = 0;
            if (comment.getEpic_count() != null) {
                epicCount = comment.getEpic_count();
            }

            String voteId = comment.getVote();
            if (voteId != null) {
                likeRoot.setVisibility(View.VISIBLE);

                if (comment.getEpic_count() != null) {
                    likeCount.setText(epicCount + "");
                } else {
                    likeCount.setText("");
                }

                Vote vote = commentManager.getVoteByVoteId(voteId);
                if (vote == null) {
                    showEmptyLikeState();
                } else if (vote.getValue() == null || vote.getValue().getValue() == null || vote.getValue().getValue().equals(VoteValue.dislike.getValue())) {
                    showEmptyLikeState();
                } else if (vote.getValue().getValue().equals(VoteValue.like.getValue())) {
                    showLikedState();
                }
            } else {
                likeRoot.setVisibility(View.GONE);
            }


            User user = getUser(comment);

            DraweeController controller = getControllerForUserAvatar(user);
            userIcon.setController(controller);

            if (user != null) {
                userName.setVisibility(View.VISIBLE);
                userName.setText(user.getFirst_name() + " " + user.getLast_name());
            } else {
                userName.setVisibility(View.GONE);
            }

            if (comment.is_pinned() != null && comment.is_pinned()) {
                pinnedIndicator.setVisibility(View.VISIBLE);
            } else {
                pinnedIndicator.setVisibility(View.GONE);
            }

            if (comment.getUser_role() != null && comment.getUser_role().getResource() != null) {
                userRole.setText(context.getString(comment.getUser_role().getResource()));
                userRole.setVisibility(View.VISIBLE);
            } else {
                userRole.setVisibility(View.GONE);
            }
        }

        private void showEmptyLikeState() {
            likeImage.setImageDrawable(likeEmptyDrawable);
        }

        private void showLikedState() {
            likeImage.setImageDrawable(likeActiveDrawable);
        }

        protected final User getUser(Comment comment) {
            if (comment.getUser() != null) {
                User user = commentManager.getUserById(comment.getUser());
                return user;
            }
            return null;
        }

        protected final DraweeController getControllerForUserAvatar(User user) {
            String userAvatar = null;
            if (user != null) {
                userAvatar = user.getAvatar();
            }
            DraweeController controller = null;
            if (userAvatar != null) {
                controller = Fresco.newDraweeControllerBuilder()
                        .setUri(userAvatar)
                        .setAutoPlayAnimations(true)
                        .build();

            } else {
                //for empty cover:
                Uri uri = new Uri.Builder()
                        .scheme(UriUtil.LOCAL_RESOURCE_SCHEME) // "res"
                        .path(String.valueOf(R.drawable.placeholder_icon))
                        .build();

                controller = Fresco.newDraweeControllerBuilder()
                        .setUri(uri)
                        .setAutoPlayAnimations(true)
                        .build();
            }
            return controller;
        }

        protected final void loadMoreParentProgressState() {
            loadMoreView.setVisibility(View.VISIBLE);
            progressLoadMoreComments.setVisibility(View.VISIBLE);
            loadMoreTextView.setVisibility(View.GONE);
        }


        protected final void loadMoreSuggestLoadingState() {
            loadMoreView.setVisibility(View.VISIBLE);
            progressLoadMoreComments.setVisibility(View.GONE);
            loadMoreTextView.setVisibility(View.VISIBLE);
        }

        protected final void loadMoreHide() {
            loadMoreView.setVisibility(View.GONE);
            loadMoreTextView.setVisibility(View.GONE);
            progressLoadMoreComments.setVisibility(View.GONE);
        }

    }
}
