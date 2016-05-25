package org.stepic.droid.view.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import org.stepic.droid.core.CommentManager;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentsViewHolder> {

    private CommentManager commentManager;

    public CommentsAdapter(CommentManager commentManager) {
        this.commentManager = commentManager;
    }

    @Override
    public CommentsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(CommentsViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    static class CommentsViewHolder extends RecyclerView.ViewHolder {
        public CommentsViewHolder(View itemView) {
            super(itemView);
        }
    }
}
