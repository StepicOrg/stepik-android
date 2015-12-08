package org.stepic.droid.view.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.stepic.droid.R;
import org.stepic.droid.base.MainApplication;
import org.stepic.droid.model.CachedVideo;
import org.stepic.droid.util.ThumbnailParser;

import java.util.List;

import butterknife.Bind;
import butterknife.BindDrawable;
import butterknife.ButterKnife;

public class DownloadsAdapter extends RecyclerView.Adapter<DownloadsAdapter.DownloadsViewHolder> {

    List<CachedVideo> mCachedVideoList;
    Context mContext;

    public DownloadsAdapter(List<CachedVideo> cachedVideos, Context context) {
        mCachedVideoList = cachedVideos;
        mContext = context;
    }

    @Override
    public DownloadsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.cached_video_item, null);
        return new DownloadsViewHolder(v);
    }


    @Override
    public void onBindViewHolder(DownloadsViewHolder holder, int position) {
        CachedVideo cachedVideo = mCachedVideoList.get(position);


        holder.loadActionIcon.setVisibility(View.GONE);
        holder.progressIcon.setVisibility(View.GONE);
        holder.deleteIcon.setVisibility(View.VISIBLE);
        Uri uriForThumbnail = ThumbnailParser.getUriForThumbnail(cachedVideo.getThumbnail());
        holder.mVideoHeader.setText("LALALAALAL");
        Picasso.with(MainApplication.getAppContext())
                .load(uriForThumbnail)
                .placeholder(holder.placeholder)
                .error(holder.placeholder)
                .into(holder.mVideoIcon);
    }

    @Override
    public int getItemCount() {
        return mCachedVideoList.size();
    }

    public static class DownloadsViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.video_icon)
        ImageView mVideoIcon;

        @Bind(R.id.video_header)
        TextView mVideoHeader;

        @BindDrawable(R.drawable.video_placeholder)
        Drawable placeholder;

        @Bind(R.id.pre_load_iv)
        ImageView loadActionIcon;

        @Bind(R.id.when_load_view)
        View progressIcon;

        @Bind(R.id.after_load_iv)
        ImageView deleteIcon;


        public DownloadsViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
