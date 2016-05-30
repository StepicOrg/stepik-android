package org.stepic.droid.view.adapters;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
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
import org.stepic.droid.view.custom.LatexSupportableWebView;

import butterknife.Bind;
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
            if (comment.getReply_count() == 0 && needUpdate) {
                loadMoreView.setVisibility(View.VISIBLE);
            } else {
                loadMoreView.setVisibility(View.GONE);
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

        void onClickLoadMoreReplies(int position){
            CommentAdapterItem needUpdateAndComment =  commentManager.getItemWithNeedUpdatingInfoByPosition(position);
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
                }
                else{
                    loadMoreReply.setVisibility(View.GONE);
                }
            }
            else{
                progressLoadMoreReplies.setVisibility(View.VISIBLE);
            }

            boolean isNeedUpdateParent = commentManager.isNeedUpdateParentInReply(comment);
            if (isNeedUpdateParent) {
                loadMoreView.setVisibility(View.VISIBLE);
            } else {
                loadMoreView.setVisibility(View.GONE);
            }

        }
    }

    abstract class GenericViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.text_comment)
        LatexSupportableWebView commentText;

        @Bind(R.id.user_icon)
        DraweeView userIcon;

        @Bind(R.id.comment_root)
        ViewGroup commentRoot;

        @Bind(R.id.user_name_comments)
        TextView userName;

        @Bind(R.id.load_more_view)
        View loadMoreView;

        public GenericViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public abstract void setDataOnView(int position);

        protected final void initialSetUp(CommentAdapterItem needUpdateAndComment) {
            Comment comment = needUpdateAndComment.getComment();

            boolean isParent = false;
            if (comment.getParent() == null) {
                isParent = true;
            }

            commentText.setText(HtmlHelper.fromHtml(comment.getText()));

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
    }
}
