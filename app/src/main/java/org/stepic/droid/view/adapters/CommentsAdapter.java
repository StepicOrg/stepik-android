package org.stepic.droid.view.adapters;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.facebook.common.util.UriUtil;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.DraweeView;

import org.stepic.droid.R;
import org.stepic.droid.core.CommentManager;
import org.stepic.droid.model.CommentAdapterItem;
import org.stepic.droid.model.User;
import org.stepic.droid.model.comments.Comment;
import org.stepic.droid.util.HtmlHelper;
import org.stepic.droid.util.RWLocks;
import org.stepic.droid.view.custom.LatexSupportableWebView;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.GenericViewHolder> {

    public static final int TYPE_PARENT_COMMENT = 1;
    public static final int TYPE_REPLY = 2;

    private CommentManager commentManager;
    private Context context;

    public CommentsAdapter(CommentManager commentManager, Context context) {
        this.commentManager = commentManager;
        this.context = context;
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

        @Bind(R.id.load_more_reply)
        View loadMoreReply;

        @Bind(R.id.progress_load_more_replies)
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

        @Bind(R.id.text_view_latex)
        LatexSupportableWebView commentTextWebView;

        @Bind(R.id.text_comment)
        TextView commentText;

        @Bind(R.id.user_icon)
        DraweeView userIcon;

        @Bind(R.id.comment_clickable_root)
        ViewGroup commentClickableRoot;

        @Bind(R.id.user_name_comments)
        TextView userName;

        @Bind(R.id.load_more_view)
        View loadMoreView;

        @Bind(R.id.load_more_textview)
        View loadMoreTextView;

        @Bind(R.id.progress_load_more_comments)
        View progressLoadMoreComments;

        @BindString(R.string.comment_is_deleted)
        String commentIsDeletedMessage;

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

            boolean isParent = false;
            if (comment.getParent() == null) {
                isParent = true;
            }

            boolean needWebView = HtmlHelper.isForWebView(comment.getText());

            if (!needWebView) {
                String str = HtmlHelper.trimTrailingWhitespace(HtmlHelper.fromHtml(comment.getText())).toString();
                commentText.setVisibility(View.VISIBLE);
                commentTextWebView.setVisibility(View.GONE);
                if (comment.is_deleted() != null && comment.is_deleted()) {
                    commentText.setText(commentIsDeletedMessage);
                } else {
                    commentText.setText(str);
                }
            } else {
                commentTextWebView.setVisibility(View.VISIBLE);
                commentText.setVisibility(View.GONE);
                commentTextWebView.setText(comment.getText());
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
