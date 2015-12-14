package org.stepic.droid.view.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.yandex.metrica.YandexMetrica;

import org.stepic.droid.R;
import org.stepic.droid.base.MainApplication;
import org.stepic.droid.model.CachedVideo;
import org.stepic.droid.model.Lesson;
import org.stepic.droid.model.Step;
import org.stepic.droid.store.CleanManager;
import org.stepic.droid.store.operations.DatabaseManager;
import org.stepic.droid.util.FileUtil;
import org.stepic.droid.util.ThumbnailParser;
import org.stepic.droid.view.listeners.OnClickLoadListener;
import org.stepic.droid.view.listeners.StepicOnClickItemListener;

import java.io.File;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.BindDrawable;
import butterknife.BindString;
import butterknife.ButterKnife;

public class DownloadsAdapter extends RecyclerView.Adapter<DownloadsAdapter.DownloadsViewHolder> implements StepicOnClickItemListener, OnClickLoadListener {

    private List<CachedVideo> mCachedVideoList;
    private Context mContext;
    private Map<Long, Lesson> mStepIdToLessonMap;

    @Inject
    CleanManager mCleanManager;

    @Inject
    DatabaseManager mDatabaseManager;

    public DownloadsAdapter(List<CachedVideo> cachedVideos, Map<Long, Lesson> videoIdToStepMap, Context context) {
        MainApplication.component().inject(this);
        mCachedVideoList = cachedVideos;
        mContext = context;
        mStepIdToLessonMap = videoIdToStepMap;
    }

    @Override
    public DownloadsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.cached_video_item, null);
        return new DownloadsViewHolder(v, this, this);
    }


    @Override
    public void onBindViewHolder(DownloadsViewHolder holder, int position) {
        CachedVideo cachedVideo = mCachedVideoList.get(position);


        holder.loadActionIcon.setVisibility(View.GONE);
        holder.progressIcon.setVisibility(View.GONE);
        holder.deleteIcon.setVisibility(View.VISIBLE);

        Uri uriForThumbnail = ThumbnailParser.getUriForThumbnail(cachedVideo.getThumbnail());
        Picasso.with(MainApplication.getAppContext())
                .load(uriForThumbnail)
                .placeholder(holder.placeholder)
                .error(holder.placeholder)
                .into(holder.mVideoIcon);

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
            Uri videoUri = Uri.parse(video.getUrl());

            Intent intent = new Intent(Intent.ACTION_VIEW, videoUri);
            intent.setDataAndType(videoUri, "video/*");
            try {
                mContext.startActivity(intent);
            } catch (Exception ex) {
                YandexMetrica.reportError("NotPlayer", ex);
                Toast.makeText(mContext, R.string.not_video_player_error, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onClickLoad(int position) {
        if (position >= 0 && position < mCachedVideoList.size()) {
            CachedVideo video = mCachedVideoList.get(position);
            mCachedVideoList.remove(position);
            final long stepId = video.getStepId();

            AsyncTask<Void, Void, Step> task = new AsyncTask<Void, Void, Step>() {
                @Override
                protected Step doInBackground(Void... params) {
                    return mDatabaseManager.getStepById(stepId);
                }

                @Override
                protected void onPostExecute(Step step) {
                    super.onPostExecute(step);
                    mCleanManager.removeStep(step);
                }
            };
            task.execute();
            notifyDataSetChanged();
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
