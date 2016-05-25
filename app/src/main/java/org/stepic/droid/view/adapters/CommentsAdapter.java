package org.stepic.droid.view.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.stepic.droid.R;
import org.stepic.droid.core.CommentManager;
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
        if (comment.getParent() == null){
            isParent = true;
        }

        holder.commentText.setText(HtmlHelper.fromHtml(comment.getText()));
    }

    @Override
    public int getItemCount() {
        return commentManager.getSize();
    }

    static class CommentsViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.text_comment)
        TextView commentText;

        public CommentsViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
