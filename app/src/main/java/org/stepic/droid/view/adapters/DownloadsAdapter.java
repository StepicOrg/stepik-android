package org.stepic.droid.view.adapters;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.stepic.droid.R;
import org.stepic.droid.base.MainApplication;
import org.stepic.droid.core.IScreenManager;
import org.stepic.droid.model.CachedVideo;
import org.stepic.droid.model.DownloadingVideoItem;
import org.stepic.droid.model.Lesson;
import org.stepic.droid.model.Step;
import org.stepic.droid.store.CleanManager;
import org.stepic.droid.store.operations.DatabaseFacade;
import org.stepic.droid.util.FileUtil;
import org.stepic.droid.util.ThumbnailParser;
import org.stepic.droid.view.fragments.DownloadsFragment;
import org.stepic.droid.view.listeners.OnClickCancelListener;
import org.stepic.droid.view.listeners.OnClickLoadListener;
import org.stepic.droid.view.listeners.StepicOnClickItemListener;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.BindDrawable;
import butterknife.BindString;
import butterknife.ButterKnife;
import me.zhanghai.android.materialprogressbar.HorizontalProgressDrawable;
import me.zhanghai.android.materialprogressbar.IndeterminateHorizontalProgressDrawable;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

public class DownloadsAdapter extends RecyclerView.Adapter<DownloadsAdapter.GenericViewHolder> implements StepicOnClickItemListener, OnClickLoadListener, OnClickCancelListener {

    public static final int TYPE_DOWNLOADING_VIDEO = 1;
    public static final int TYPE_DOWNLOADED_VIDEO = 2;
    public static final int TYPE_TITLE = 3;

    private List<CachedVideo> mCachedVideoList;
    private Activity sourceActivity;
    private Map<Long, Lesson> mStepIdToLessonMap;
    final private List<DownloadingVideoItem> mDownloadingVideoList;

    @Inject
    CleanManager mCleanManager;

    @Inject
    DatabaseFacade mDatabaseFacade;
    @Inject
    IScreenManager mScreenManager;

    @Inject
    ThreadPoolExecutor threadPoolExecutor;

    private DownloadsFragment downloadsFragment;
    private Set<Long> cachedStepsSet;

    public DownloadsAdapter(List<CachedVideo> cachedVideos, Map<Long, Lesson> videoIdToStepMap, Activity context, DownloadsFragment downloadsFragment, List<DownloadingVideoItem> downloadingList, Set<Long> cachedStepsSet) {
        this.downloadsFragment = downloadsFragment;
        MainApplication.component().inject(this);
        mCachedVideoList = cachedVideos;
        sourceActivity = context;
        mStepIdToLessonMap = videoIdToStepMap;
        mDownloadingVideoList = downloadingList;
        this.cachedStepsSet = cachedStepsSet;
    }

    @Override
    public GenericViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_DOWNLOADED_VIDEO) {
            View v = LayoutInflater.from(sourceActivity).inflate(R.layout.cached_video_item, null);
            return new DownloadsViewHolder(v, this, this);
        } else if (viewType == TYPE_DOWNLOADING_VIDEO) {
            View v = LayoutInflater.from(sourceActivity).inflate(R.layout.downloading_video_item, null);
            return new DownloadingViewHolder(v, this);
        } else if (viewType == TYPE_TITLE) {
            View v = LayoutInflater.from(sourceActivity).inflate(R.layout.header_download_item, null);
            return new TitleViewHolder(v);
        } else {
            return null;
        }
    }

    @Override
    public int getItemViewType(int position) {
        if ((!mDownloadingVideoList.isEmpty() && position == 0) || (!mCachedVideoList.isEmpty() && position == (mDownloadingVideoList.size() + getTitleCount(mDownloadingVideoList)))) {
            return TYPE_TITLE;
        } else if (position >= mDownloadingVideoList.size() + getTitleCount(mDownloadingVideoList) + getTitleCount(mCachedVideoList)) {
            return TYPE_DOWNLOADED_VIDEO;
        } else {
            return TYPE_DOWNLOADING_VIDEO;
        }
    }


    @Override
    public void onBindViewHolder(GenericViewHolder holder, int position) {
        holder.setDataOnView(position);
    }

    @Override
    public int getItemCount() {
        final int countOnRecycler = mCachedVideoList.size() + mDownloadingVideoList.size() + getTitleCount(mDownloadingVideoList) + getTitleCount(mCachedVideoList);
        return countOnRecycler;
    }

    @Override
    public void onClick(int position) {
        //the position in list!
        if (position >= 0 && position < mCachedVideoList.size()) {
            CachedVideo video = mCachedVideoList.get(position);
            mScreenManager.showVideo(sourceActivity, video.getUrl());
        }
    }

    @Override
    public void onClickLoad(int position) {
        //the position in list!
        if (position >= 0 && position < mCachedVideoList.size()) {
            CachedVideo video = mCachedVideoList.get(position);
            mCachedVideoList.remove(position);
            mStepIdToLessonMap.remove(video.getStepId());
            cachedStepsSet.remove(video.getStepId());

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
            notifyCachedVideoRemoved(position);
        }
    }

    @Override
    public void onClickCancel(int position) {
        //the position in list!
        if (position >= 0 && position < mDownloadingVideoList.size()) {
            Log.d("eee", "click cancel " + position);
        }
    }

    public void notifyCachedVideoRemoved(int position) {
        if (mCachedVideoList.isEmpty()) {
            notifyItemRemoved(position + mDownloadingVideoList.size() + getTitleCount(mDownloadingVideoList));//title
            notifyItemRemoved(position + mDownloadingVideoList.size() + getTitleCount(mDownloadingVideoList) + 1);//last item
        } else {
            notifyItemRemoved(position + mDownloadingVideoList.size() + getTitleCount(mDownloadingVideoList) + 1);
        }

    }

    public void notifyDownloadingVideoRemoved(int positionInList) {
        if (mDownloadingVideoList.isEmpty()) {
            notifyItemRemoved(0);//title
            notifyItemRemoved(1);//last view
        } else {
            notifyItemRemoved(positionInList + 1);
        }
    }

    public class DownloadingViewHolder extends GenericViewHolder {

        @Bind(R.id.cancel_load)
        View cancelLoad;
        @Bind(R.id.video_header)
        TextView mVideoHeader;

        @Bind(R.id.video_icon)
        ImageView mVideoIcon;

        @BindDrawable(R.drawable.video_placeholder)
        Drawable placeholder;

        @Bind(R.id.video_downloading_progress_bar)
        MaterialProgressBar downloadingProgressBar;

        @Bind(R.id.progress_text)
        TextView progressTextView;

        @BindString(R.string.kb)
        String kb;

        @BindString(R.string.mb)
        String mb;

        @Bind(R.id.progress_percent)
        TextView progressPercent;

        @BindString(R.string.delimiter_for_download)
        String downloadDelimiter;

        @BindString(R.string.download_pending)
        String downloadPending;

        Drawable indeterminateDrawable;

        Drawable finiteDrawable;

        public DownloadingViewHolder(View itemView, final OnClickCancelListener cancelListener) {
            super(itemView);

            cancelLoad.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cancelListener.onClickCancel(getAdapterPosition() - getTitleCount(mDownloadingVideoList));
                }
            });

            indeterminateDrawable = new IndeterminateHorizontalProgressDrawable(sourceActivity);
            finiteDrawable = new HorizontalProgressDrawable(sourceActivity);
        }

        @Override
        public void setDataOnView(int position) {
            DownloadingVideoItem downloadingVideoItem = mDownloadingVideoList.get(position - 1);//here downloading list shoudn't be empty!

            String thumbnail = downloadingVideoItem.getDownloadEntity().getThumbnail();
            if (thumbnail != null) {
                Uri uriForThumbnail = ThumbnailParser.getUriForThumbnail(thumbnail);
                Picasso.with(MainApplication.getAppContext())
                        .load(uriForThumbnail)
                        .placeholder(placeholder)
                        .error(placeholder)
                        .into(mVideoIcon);
            } else {
                Picasso.with(MainApplication.getAppContext())
                        .load(R.drawable.video_placeholder)
                        .placeholder(placeholder)
                        .error(placeholder)
                        .into(mVideoIcon);
            }

            Lesson relatedLesson = mStepIdToLessonMap.get(downloadingVideoItem.getDownloadEntity().getStepId());
            if (relatedLesson != null) {
                String header = relatedLesson.getTitle();
                mVideoHeader.setText(header);
            } else {
                mVideoHeader.setText("");
            }

            int bytesTotal = downloadingVideoItem.getDownloadReportItem().getBytesTotal();
            int bytesDownloaded = downloadingVideoItem.getDownloadReportItem().getBytesDownloaded();


            StringBuilder loadProgressStringBuilder = new StringBuilder();
            if (bytesTotal <= 0) {
                loadProgressStringBuilder.append(downloadPending);
                downloadingProgressBar.setIndeterminateDrawable(indeterminateDrawable);
                progressPercent.setVisibility(View.INVISIBLE);
            } else {
                int totalSizeForView = bytesTotal / 1024;
                int downloadedSieForView = bytesDownloaded / 1024;

                appendToSbSize(downloadedSieForView, loadProgressStringBuilder);
                loadProgressStringBuilder.append(downloadDelimiter);
                appendToSbSize(totalSizeForView, loadProgressStringBuilder);

                downloadingProgressBar.setMax(bytesTotal);
                downloadingProgressBar.setProgress(bytesDownloaded);
                downloadingProgressBar.setIndeterminateDrawable(finiteDrawable);

                int percentValue = (int) (((double) bytesDownloaded / (double) bytesTotal) * 100);
                progressPercent.setText(sourceActivity.getResources().getString(R.string.percent_symbol, percentValue));
                progressPercent.setVisibility(View.VISIBLE);
            }
            progressTextView.setText(loadProgressStringBuilder.toString());
        }

        private void appendToSbSize(int downloadedSieForView, StringBuilder stringBuilder) {


            if (downloadedSieForView < 1024) {
                stringBuilder.append(downloadedSieForView);
                stringBuilder.append(" ");
                stringBuilder.append(kb);
            } else {
                downloadedSieForView /= 1024;
                stringBuilder.append(downloadedSieForView);
                stringBuilder.append(" ");
                stringBuilder.append(mb);
            }
        }

    }

    public class DownloadsViewHolder extends GenericViewHolder {

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

            itemView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    click.onClick(getAdapterPosition() - mDownloadingVideoList.size() - getTitleCount(mDownloadingVideoList) - getTitleCount(mCachedVideoList));
                }
            });

            mLoadRoot.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    loadListener.onClickLoad(getAdapterPosition() - mDownloadingVideoList.size() - getTitleCount(mDownloadingVideoList) - getTitleCount(mCachedVideoList));
                }
            });
        }

        @Override
        public void setDataOnView(int position) {
            CachedVideo cachedVideo = mCachedVideoList.get(position - mDownloadingVideoList.size() - getTitleCount(mDownloadingVideoList) - getTitleCount(mCachedVideoList));


            loadActionIcon.setVisibility(View.GONE);
            progressIcon.setVisibility(View.GONE);
            deleteIcon.setVisibility(View.VISIBLE);

            String thumbnail = cachedVideo.getThumbnail();
            if (thumbnail != null) {
                Uri uriForThumbnail = ThumbnailParser.getUriForThumbnail(thumbnail);
                Picasso.with(MainApplication.getAppContext())
                        .load(uriForThumbnail)
                        .placeholder(placeholder)
                        .error(placeholder)
                        .into(mVideoIcon);
            } else {
                Picasso.with(MainApplication.getAppContext())
                        .load(R.drawable.video_placeholder)
                        .placeholder(placeholder)
                        .error(placeholder)
                        .into(mVideoIcon);
            }

            Lesson relatedLesson = mStepIdToLessonMap.get(cachedVideo.getStepId());
            if (relatedLesson != null) {
                String header = relatedLesson.getTitle();
                mVideoHeader.setText(header);
            } else {
                mVideoHeader.setText("");
            }
            File file = new File(cachedVideo.getUrl()); // predict: heavy operation
            long size = FileUtil.getFileOrFolderSizeInKb(file);
            String sizeString;
            if (size < 1024) {
                sizeString = size + " " + kb;
            } else {
                size /= 1024;
                sizeString = size + " " + mb;
            }
            mSize.setText(sizeString);

            String quality = cachedVideo.getQuality();
            if (quality == null || quality.length() == 0) {
                mCurrentQuality.setText("");
            } else {
                quality += "p";
                mCurrentQuality.setText(quality);
            }
        }

    }

    public class TitleViewHolder extends GenericViewHolder {

        @Bind(R.id.button_header_download_item)
        Button headerButton;

        @Bind(R.id.text_header_download_item)
        TextView headerTextView;

        String titleDownloading;
        String titleForDownloadingButton;
        String titleCached;
        String titleForCachedButton;

        public TitleViewHolder(View itemView) {
            super(itemView);
            titleDownloading = MainApplication.getAppContext().getString(R.string.downloading_title);
            titleForDownloadingButton = MainApplication.getAppContext().getString(R.string.downloading_cancel_all);
            titleCached = MainApplication.getAppContext().getString(R.string.cached_title);
            titleForCachedButton = MainApplication.getAppContext().getString(R.string.remove_all);
        }

        @Override
        public void setDataOnView(int position) {
            if (position == 0 && !mDownloadingVideoList.isEmpty()) {
                headerTextView.setText(titleDownloading);
                headerButton.setText(titleForDownloadingButton);
            } else {
                headerTextView.setText(titleCached);
                headerButton.setText(titleForCachedButton);
            }

        }

    }

    public abstract class GenericViewHolder extends RecyclerView.ViewHolder {

        public GenericViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public abstract void setDataOnView(int position);

    }

    public void notifyCachedVideoInserted(long stepId, int position) {
        // when cached video is insert, we should remove downloading
        int downloadingPos = -1;
        for (int i = 0; i < mDownloadingVideoList.size(); i++) {
            if (mDownloadingVideoList.get(i).getDownloadEntity().getStepId() == stepId) {
                downloadingPos = i;
                break;
            }
        }


        boolean isVideoWasInDownloading = downloadingPos >= 0;


        if (isVideoWasInDownloading) {
            mDownloadingVideoList.remove(downloadingPos);
        }

        downloadingPos += (getTitleCount(mDownloadingVideoList) + getTitleCount(mCachedVideoList)); //title
        position += (getTitleCount(mDownloadingVideoList) + getTitleCount(mCachedVideoList)); //title

        int realPosition = position + mDownloadingVideoList.size();

        if (isVideoWasInDownloading) {
            if (downloadingPos == realPosition) {
                notifyItemChanged(realPosition);
            } else {
                if (downloadingPos != 1) {
                    notifyItemMoved(downloadingPos, realPosition);
                    notifyItemRangeChanged(downloadingPos + 1, getItemCount());
                } else {
                    notifyDataSetChanged();
                }
            }
        } else {
            notifyItemInserted(realPosition);
        }


    }

    public void notifyDownloadingVideoChanged(int position) {
        notifyItemChanged(position + 1);
    }

    public void notifyDownloadingItemInserted(int position) {
        notifyItemChanged(0);
        notifyItemInserted(position + 1);
    }

    public static int getTitleCount(Collection collection) {
        return collection.isEmpty() ? 0 : 1;
    }
}
