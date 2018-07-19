package org.stepic.droid.ui.adapters;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PictureDrawable;
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

import com.bumptech.glide.GenericRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.caverock.androidsvg.SVG;

import org.jetbrains.annotations.Nullable;
import org.stepic.droid.R;
import org.stepic.droid.base.App;
import org.stepic.droid.core.CommentManager;
import org.stepic.droid.model.CommentAdapterItem;
import org.stepik.android.model.UserRole;
import org.stepik.android.model.user.User;
import org.stepic.droid.model.comments.Comment;
import org.stepic.droid.model.comments.Vote;
import org.stepic.droid.model.comments.VoteValue;
import org.stepic.droid.ui.custom.LatexSupportableEnhancedFrameLayout;
import org.stepic.droid.util.AppConstants;
import org.stepic.droid.util.ColorUtil;
import org.stepic.droid.util.DateTimeHelper;
import org.stepic.droid.util.RWLocks;
import org.stepic.droid.util.svg.GlideSvgRequestFactory;

import java.io.InputStream;
import java.util.Locale;
import java.util.TimeZone;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;

public final class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.GenericViewHolder> {

    private final int TYPE_PARENT_COMMENT = 1;
    private final int TYPE_REPLY = 2;

    private final CommentManager commentManager;
    private final Context context;

    private final Drawable placeholderUserIcon;

    public CommentsAdapter(CommentManager commentManager, Context context) {
        this.commentManager = commentManager;
        this.context = context;
        placeholderUserIcon = ContextCompat.getDrawable(App.Companion.getAppContext(), R.drawable.general_placeholder);
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
                if (comment.getReplyCount() != null && comment.getReplyCount() == 0 && needUpdate) {
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
        ImageView userIcon;

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

        final GenericRequestBuilder<Uri, InputStream, SVG, PictureDrawable> svgRequestBuilder;

        public GenericViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            loadMoreView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        //// FIXME: 08.11.16 this lock work only on main thread and may produce performance problem
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

            svgRequestBuilder = GlideSvgRequestFactory.create(itemView.getContext(), placeholderUserIcon);
        }

        private void onClickMoreLayout(int adapterPosition) {
            CommentAdapterItem commentAdapterItem = commentManager.getItemWithNeedUpdatingInfoByPosition(adapterPosition);
            commentManager.addCommentIdWhereLoadMoreClicked(commentAdapterItem.getComment().getId());
            notifyItemChanged(adapterPosition);
            commentManager.loadComments();
        }

        public abstract void setDataOnView(int position);

        final void initialSetUp(CommentAdapterItem needUpdateAndComment) {
            final Comment comment = needUpdateAndComment.getComment();

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
                commentTimeTextView.setText(DateTimeHelper.INSTANCE.getPrintableOfIsoDate(comment.getTime(), AppConstants.COMMENT_DATE_TIME_PATTERN, TimeZone.getDefault()));
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
                    likeCount.setText(String.format(Locale.getDefault(), "%d", epicCount));
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
            String userAvatar = "";
            if (user != null) {
                userAvatar = user.getAvatar();
                if (userAvatar == null) {
                    userAvatar = "";
                }
            }

            if (userAvatar.endsWith(AppConstants.SVG_EXTENSION)) {
                Uri uri = Uri.parse(userAvatar);
                svgRequestBuilder
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .load(uri)
                        .into(userIcon);
            } else {
                Glide.with(App.Companion.getAppContext())
                        .load(userAvatar)
                        .asBitmap()
                        .placeholder(placeholderUserIcon)
                        .into(userIcon);
            }

            if (user != null) {
                userName.setVisibility(View.VISIBLE);
                userName.setText(user.getFullName());
            } else {
                userName.setVisibility(View.GONE);
            }

            if (comment.isPinned()) {
                pinnedIndicator.setVisibility(View.VISIBLE);
            } else {
                pinnedIndicator.setVisibility(View.GONE);
            }

            final String userRoleLabel = getStringForUserRole(comment.getUserRole());
            if (userRoleLabel != null) {
                userRole.setText(userRoleLabel);
                userRole.setVisibility(View.VISIBLE);
            } else {
                userRole.setVisibility(View.GONE);
            }
        }

        @Nullable
        private String getStringForUserRole(@Nullable UserRole role) {
            if (role == null) {
                return null;
            } else switch (role) {
                case STUDENT: return null;
                case STUFF: return context.getString(R.string.staff_label);
                case TEACHER: return context.getString(R.string.teacher_label);
                default: return null;
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
                return commentManager.getUserById(comment.getUser());
            }
            return null;
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
