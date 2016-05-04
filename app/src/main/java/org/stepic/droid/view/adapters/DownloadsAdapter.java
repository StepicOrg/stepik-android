package org.stepic.droid.view.adapters;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.stepic.droid.R;
import org.stepic.droid.base.MainApplication;
import org.stepic.droid.core.IScreenManager;
import org.stepic.droid.model.CachedVideo;
import org.stepic.droid.model.Lesson;
import org.stepic.droid.model.Step;
import org.stepic.droid.store.CleanManager;
import org.stepic.droid.store.operations.DatabaseFacade;
import org.stepic.droid.util.FileUtil;
import org.stepic.droid.util.ThumbnailParser;
import org.stepic.droid.view.fragments.DownloadsFragment;
import org.stepic.droid.view.listeners.OnClickLoadListener;
import org.stepic.droid.view.listeners.StepicOnClickItemListener;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.BindDrawable;
import butterknife.BindString;
import butterknife.ButterKnife;

public class DownloadsAdapter extends RecyclerView.Adapter<DownloadsAdapter.DownloadsViewHolder> implements StepicOnClickItemListener, OnClickLoadListener {

    public static final int TYPE_DOWNLOADING_VIDEO = 1;
    public static final int TYPE_DOWNLOADED_VIDEO = 2;

    private List<CachedVideo> mCachedVideoList;
    private Activity sourceActivity;
    private Map<Long, Lesson> mStepIdToLessonMap;

    @Inject
    CleanManager mCleanManager;

    @Inject
    DatabaseFacade mDatabaseFacade;
    @Inject
    IScreenManager mScreenManager;

    @Inject
    ThreadPoolExecutor threadPoolExecutor;

    private DownloadsFragment downloadsFragment;

    public DownloadsAdapter(List<CachedVideo> cachedVideos, Map<Long, Lesson> videoIdToStepMap, Activity context, DownloadsFragment downloadsFragment) {
        this.downloadsFragment = downloadsFragment;
        MainApplication.component().inject(this);
        mCachedVideoList = cachedVideos;
        sourceActivity = context;
        mStepIdToLessonMap = videoIdToStepMap;
    }

    @Override
    public DownloadsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(sourceActivity).inflate(R.layout.cached_video_item, null);
        return new DownloadsViewHolder(v, this, this);
    }


    @Override
    public void onBindViewHolder(DownloadsViewHolder holder, int position) {
        CachedVideo cachedVideo = mCachedVideoList.get(position);


        holder.loadActionIcon.setVisibility(View.GONE);
        holder.progressIcon.setVisibility(View.GONE);
        holder.deleteIcon.setVisibility(View.VISIBLE);

        String thumbnail = cachedVideo.getThumbnail();
        if (thumbnail != null) {
            Uri uriForThumbnail = ThumbnailParser.getUriForThumbnail(thumbnail);
            Picasso.with(MainApplication.getAppContext())
                    .load(uriForThumbnail)
                    .placeholder(holder.placeholder)
                    .error(holder.placeholder)
                    .into(holder.mVideoIcon);
        } else {
            Picasso.with(MainApplication.getAppContext())
                    .load(R.drawable.video_placeholder)
                    .placeholder(holder.placeholder)
                    .error(holder.placeholder)
                    .into(holder.mVideoIcon);
        }

        Lesson relatedLesson = mStepIdToLessonMap.get(cachedVideo.getStepId());
        if (relatedLesson != null) {
            String header = relatedLesson.getTitle();
            holder.mVideoHeader.setText(header);
        } else {
            holder.mVideoHeader.setText("");
        }
        File file = new File(cachedVideo.getUrl()); // predict: heavy operation
        long size = FileUtil.getFileOrFolderSizeInKb(file);
        String sizeString;
        if (size < 1024) {
            sizeString = size + " " + holder.kb;
        } else {
            size /= 1024;
            sizeString = size + " " + holder.mb;
        }
        holder.mSize.setText(sizeString);

        String quality = cachedVideo.getQuality();
        if (quality == null || quality.length() == 0) {
            holder.mCurrentQuality.setText("");
        } else {
            quality += "p";
            holder.mCurrentQuality.setText(quality);
        }

    }

    @Override
    public int getItemCount() {
        return mCachedVideoList.size();
    }

    @Override
    public void onClick(int position) {
        if (position >= 0 && position < mCachedVideoList.size()) {
            CachedVideo video = mCachedVideoList.get(position);
            mScreenManager.showVideo(sourceActivity, video.getUrl());
        }
    }

    @Override
    public void onClickLoad(int position) {
        if (position >= 0 && position < mCachedVideoList.size()) {
            CachedVideo video = mCachedVideoList.get(position);
            mCachedVideoList.remove(position);
            mStepIdToLessonMap.remove(video.getStepId());
            final long stepId = video.getStepId();

            AsyncTask<Void, Void, Step> task = new AsyncTask<Void, Void, Step>() {
                @Override
                protected Step doInBackground(Void... params) {
                    return mDatabaseFacade.getStepById(stepId);
                }

                @Override
                protected void onPostExecute(Step step) {
                    super.onPostExecute(step);
                    mCleanManager.removeStep(step);
                }
            };
            task.executeOnExecutor(threadPoolExecutor);
            downloadsFragment.checkForEmpty();
            notifyItemRemoved(position);
        }
    }

    public static class DownloadsViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.current_quality)
        TextView mCurrentQuality;

        @Bind(R.id.size_of_cached_video)
        TextView mSize;

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

        @BindString(R.string.kb)
        String kb;

        @BindString(R.string.mb)
        String mb;

        @Bind(R.id.load_button)
        View mLoadRoot;

        public DownloadsViewHolder(View itemView, final StepicOnClickItemListener click, final OnClickLoadListener loadListener) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    click.onClick(getAdapterPosition());
                }
            });

            mLoadRoot.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    loadListener.onClickLoad(getAdapterPosition());
                }
            });


        }
    }
}
