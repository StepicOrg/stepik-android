package org.stepic.droid.view.adapters;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.common.util.UriUtil;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.DraweeView;

import org.stepic.droid.R;
import org.stepic.droid.core.CommentManager;
import org.stepic.droid.model.User;
import org.stepic.droid.model.comments.Comment;
import org.stepic.droid.util.HtmlHelper;

import butterknife.Bind;
import butterknife.ButterKnife;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentsViewHolder> {

    private CommentManager commentManager;
    private Context context;

    public CommentsAdapter(CommentManager commentManager, Context context) {
        this.commentManager = commentManager;
        this.context = context;
    }

    @Override
    public CommentsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.comment_item, parent, false);
        return new CommentsViewHolder(v);
    }

    @Override
    public void onBindViewHolder(CommentsViewHolder holder, int position) {
        kotlin.Pair<Boolean, Comment> needUpdateAndComment = commentManager.getItemWithNeedUpdatingInfoByPosition(position);
        boolean needUpdate = needUpdateAndComment.component1();
        Comment comment = needUpdateAndComment.component2();

        boolean isParent = false;
        if (comment.getParent() == null) {
            isParent = true;
        }

        holder.commentText.setText(HtmlHelper.fromHtml(comment.getText()));

        DraweeController controller = getControllerForUserAvatar(comment);
        holder.userIcon.setController(controller);
    }

    private DraweeController getControllerForUserAvatar(Comment comment) {
        String userAvatar = null;
        if (comment.getUser() != null) {
            User user = commentManager.getUserById(comment.getUser());
            if (user != null) {
                userAvatar = user.getAvatar();
            }
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

    @Override
    public int getItemCount() {
        return commentManager.getSize();
    }

    static class CommentsViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.text_comment)
        TextView commentText;

        @Bind(R.id.user_icon)
        DraweeView userIcon;

        public CommentsViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
