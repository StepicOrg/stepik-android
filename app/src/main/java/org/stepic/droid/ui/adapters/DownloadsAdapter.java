package org.stepic.droid.ui.adapters;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.jetbrains.annotations.NotNull;
import org.stepic.droid.R;
import org.stepic.droid.base.App;
import org.stepic.droid.core.ScreenManager;
import org.stepic.droid.model.CachedVideo;
import org.stepic.droid.model.DownloadingVideoItem;
import org.stepik.android.model.Lesson;
import org.stepik.android.model.Step;
import org.stepic.droid.storage.CancelSniffer;
import org.stepic.droid.storage.CleanManager;
import org.stepic.droid.storage.IDownloadManager;
import org.stepic.droid.storage.operations.DatabaseFacade;
import org.stepic.droid.ui.custom.progressbutton.ProgressWheel;
import org.stepic.droid.ui.fragments.DownloadsFragment;
import org.stepic.droid.ui.listeners.OnClickCancelListener;
import org.stepic.droid.ui.listeners.OnClickLoadListener;
import org.stepic.droid.ui.listeners.OnItemClickListener;
import org.stepic.droid.util.FileUtil;
import org.stepic.droid.util.ThumbnailParser;
import org.stepic.droid.util.VideoStepHelperKt;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;

import javax.inject.Inject;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;

public class DownloadsAdapter extends RecyclerView.Adapter<DownloadsAdapter.GenericViewHolder> implements OnItemClickListener, OnClickLoadListener, OnClickCancelListener {

    public static final int TYPE_DOWNLOADING_VIDEO = 1;
    public static final int TYPE_DOWNLOADED_VIDEO = 2;
    public static final int TYPE_TITLE = 3;

    private List<CachedVideo> cachedVideoList;
    private Activity sourceActivity;
    private Map<Long, Lesson> stepIdToLessonMap;
    final private List<DownloadingVideoItem> downloadingVideoList;

    @Inject
    CleanManager cleanManager;

    @Inject
    DatabaseFacade databaseFacade;

    @Inject
    ScreenManager screenManager;

    @Inject
    ThreadPoolExecutor threadPoolExecutor;

    @Inject
    CancelSniffer cancelSniffer;

    @Inject
    IDownloadManager downloadManager;

    @NotNull
    private Drawable placeholder;

    private DownloadsFragment downloadsFragment;
    private final OnClickCancelListener onClickCancelListener;
    private Set<Long> cachedStepsSet;

    public DownloadsAdapter(List<CachedVideo> cachedVideos,
                            Map<Long, Lesson> videoIdToStepMap,
                            Activity context,
                            DownloadsFragment downloadsFragment,
                            List<DownloadingVideoItem> downloadingList,
                            Set<Long> cachedStepsSet,
                            OnClickCancelListener onClickCancelListener) {
        this.downloadsFragment = downloadsFragment;
        this.onClickCancelListener = onClickCancelListener;
        App.Companion.component().inject(this);
        cachedVideoList = cachedVideos;
        sourceActivity = context;
        stepIdToLessonMap = videoIdToStepMap;
        downloadingVideoList = downloadingList;
        this.cachedStepsSet = cachedStepsSet;
        placeholder = ContextCompat.getDrawable(App.Companion.getAppContext(), R.drawable.video_placeholder);
    }

    @Override
    public GenericViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_DOWNLOADED_VIDEO) {
            View v = LayoutInflater.from(sourceActivity).inflate(R.layout.cached_video_item, parent, false);
            return new DownloadsViewHolder(v, this, this);
        } else if (viewType == TYPE_DOWNLOADING_VIDEO) {
            View v = LayoutInflater.from(sourceActivity).inflate(R.layout.downloading_video_item, parent, false);
            return new DownloadingViewHolder(v, this);
        } else if (viewType == TYPE_TITLE) {
            View v = LayoutInflater.from(sourceActivity).inflate(R.layout.header_download_item, parent, false);
            return new TitleViewHolder(v, onClickCancelListener);
        } else {
            return null;
        }
    }

    @Override
    public int getItemViewType(int position) {
        if ((!downloadingVideoList.isEmpty() && position == 0) || (!cachedVideoList.isEmpty() && position == (downloadingVideoList.size() + getTitleCount(downloadingVideoList)))) {
            return TYPE_TITLE;
        } else if (position >= downloadingVideoList.size() + getTitleCount(downloadingVideoList) + getTitleCount(cachedVideoList)) {
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
        final int countOnRecycler = cachedVideoList.size() + downloadingVideoList.size() + getTitleCount(downloadingVideoList) + getTitleCount(cachedVideoList);
        return countOnRecycler;
    }

    @Override
    public void onItemClick(int position) {
        //the position in oldList!
        if (position >= 0 && position < cachedVideoList.size()) {
            final CachedVideo originalVideo = cachedVideoList.get(position);
            final String originalPath = originalVideo.getUrl();

            final CachedVideo video = null; //videoFileResolver.resolveVideoFile(originalVideo);

            if (video != null && video.getUrl() != null) {
                if (!video.getUrl().equals(originalPath)) {
                    notifyItemChanged(position + downloadingVideoList.size() + getTitleCount(downloadingVideoList) + getTitleCount(cachedVideoList));
                }

                screenManager.showVideo(sourceActivity, VideoStepHelperKt.transformToVideo(video), null);
            } else {
                Toast.makeText(App.Companion.getAppContext(), R.string.sorry_moved, Toast.LENGTH_SHORT).show();
                threadPoolExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        Step step = databaseFacade.getStepById(originalVideo.getStepId());
                        cleanManager.removeStep(step);
                    }
                });
            }
        }
    }

    @Override
    public void onClickLoad(int position) {
        //the position in oldList!
        if (position >= 0 && position < cachedVideoList.size()) {
            CachedVideo video = cachedVideoList.get(position);
            cachedVideoList.remove(position);
            stepIdToLessonMap.remove(video.getStepId());
            cachedStepsSet.remove(video.getStepId());

            final long stepId = video.getStepId();

            AsyncTask<Void, Void, Step> task = new AsyncTask<Void, Void, Step>() {
                @Override
                protected Step doInBackground(Void... params) {
                    return databaseFacade.getStepById(stepId);
                }

                @Override
                protected void onPostExecute(Step step) {
                    super.onPostExecute(step);
                    cleanManager.removeStep(step);
                }
            };
            task.executeOnExecutor(threadPoolExecutor);
            downloadsFragment.checkForEmpty();
            notifyCachedVideoRemoved(position);
        }
    }

    @Override
    public void onClickCancel(int position) {
        //the position in oldList!
        if (position >= 0 && position < downloadingVideoList.size()) {
            DownloadingVideoItem downloadingVideoItem = downloadingVideoList.get(position);
            final long stepId = downloadingVideoItem.getDownloadEntity().getStepId();
            threadPoolExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    cancelSniffer.addStepIdCancel(stepId);
                    downloadManager.cancelStep(stepId);
                }
            });

            downloadingVideoList.remove(position);
            notifyDataSetChanged(); // TODO: 13.05.16 investigate and make remove animation
            if (downloadsFragment != null) {
                downloadsFragment.checkForEmpty();
            }
        }
    }

    public class DownloadingViewHolder extends GenericViewHolder {

        @BindView(R.id.when_load_view)
        ProgressWheel progressWheel;

        @BindView(R.id.cancel_load)
        View cancelLoad;

        @BindView(R.id.video_header)
        TextView videoHeader;

        @BindView(R.id.video_icon)
        ImageView mVideoIcon;

        @BindView(R.id.progress_text)
        TextView progressTextView;

        @BindString(R.string.kb)
        String kb;

        @BindString(R.string.mb)
        String mb;

        @BindView(R.id.progress_percent)
        TextView progressPercent;

        @BindString(R.string.delimiter_for_download)
        String downloadDelimiter;

        @BindString(R.string.download_pending)
        String downloadPending;

        private long oldVideoId = -1L;

        public DownloadingViewHolder(View itemView, final OnClickCancelListener cancelListener) {
            super(itemView);

            cancelLoad.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cancelListener.onClickCancel(getAdapterPosition() - getTitleCount(downloadingVideoList));
                }
            });
        }

        @Override
        public void setDataOnView(int position) {
            DownloadingVideoItem downloadingVideoItem = downloadingVideoList.get(position - 1);//here downloading oldList shoudn't be empty!
            long videoId = downloadingVideoItem.getDownloadEntity().getVideoId();
            boolean needAnimation = true;
            if (oldVideoId != videoId) {
                //if rebinding than animation is not needed
                oldVideoId = videoId;
                needAnimation = false;
            }

            String thumbnail = downloadingVideoItem.getDownloadEntity().getThumbnail();
            if (thumbnail != null) {
                Uri uriForThumbnail = ThumbnailParser.getUriForThumbnail(thumbnail);
                Glide.with(App.Companion.getAppContext())
                        .load(uriForThumbnail)
                        .placeholder(placeholder)
                        .into(mVideoIcon);
            } else {
                Glide.with(App.Companion.getAppContext())
                        .load("")
                        .placeholder(placeholder)
                        .into(mVideoIcon);
            }

            Lesson relatedLesson = stepIdToLessonMap.get(downloadingVideoItem.getDownloadEntity().getStepId());
            if (relatedLesson != null) {
                String header = relatedLesson.getTitle();
                videoHeader.setText(header);
            } else {
                videoHeader.setText("");
            }

            int bytesTotal = downloadingVideoItem.getDownloadReportItem().getBytesTotal();
            int bytesDownloaded = downloadingVideoItem.getDownloadReportItem().getBytesDownloaded();


            StringBuilder loadProgressStringBuilder = new StringBuilder();
            if (bytesTotal <= 0) {
                loadProgressStringBuilder.append(downloadPending);
                progressPercent.setVisibility(View.INVISIBLE);
            } else {
                int totalSizeForView = bytesTotal / 1024;
                int downloadedSieForView = bytesDownloaded / 1024;

                appendToSbSize(downloadedSieForView, loadProgressStringBuilder);
                loadProgressStringBuilder.append(downloadDelimiter);
                appendToSbSize(totalSizeForView, loadProgressStringBuilder);

                progressWheel.setProgressPortion(bytesDownloaded / (float) bytesTotal, needAnimation);

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

        @BindView(R.id.current_quality)
        TextView currentQuality;

        @BindView(R.id.size_of_cached_video)
        TextView size;

        @BindView(R.id.video_icon)
        ImageView videoIcon;

        @BindView(R.id.video_header)
        TextView videoHeader;


        @BindView(R.id.pre_load_iv)
        ImageView loadActionIcon;

        @BindView(R.id.when_load_view)
        View progressIcon;

        @BindView(R.id.after_load_iv)
        ImageView deleteIcon;

        @BindString(R.string.kb)
        String kb;

        @BindString(R.string.mb)
        String mb;

        @BindView(R.id.load_button)
        View loadRoot;

        public DownloadsViewHolder(View itemView, final OnItemClickListener click, final OnClickLoadListener loadListener) {
            super(itemView);

            itemView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    click.onItemClick(getAdapterPosition() - downloadingVideoList.size() - getTitleCount(downloadingVideoList) - getTitleCount(cachedVideoList));
                }
            });

            loadRoot.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    loadListener.onClickLoad(getAdapterPosition() - downloadingVideoList.size() - getTitleCount(downloadingVideoList) - getTitleCount(cachedVideoList));
                }
            });
        }

        @Override
        public void setDataOnView(int position) {
            CachedVideo cachedVideo = cachedVideoList.get(position - downloadingVideoList.size() - getTitleCount(downloadingVideoList) - getTitleCount(cachedVideoList));


            loadActionIcon.setVisibility(View.GONE);
            progressIcon.setVisibility(View.GONE);
            deleteIcon.setVisibility(View.VISIBLE);

            String thumbnail = cachedVideo.getThumbnail();
            if (thumbnail != null) {
                Uri uriForThumbnail = ThumbnailParser.getUriForThumbnail(thumbnail);
                Glide.with(App.Companion.getAppContext())
                        .load(uriForThumbnail)
                        .placeholder(placeholder)
                        .into(videoIcon);
            } else {
                Glide.with(App.Companion.getAppContext())
                        .load("")
                        .placeholder(placeholder)
                        .into(videoIcon);
            }

            Lesson relatedLesson = stepIdToLessonMap.get(cachedVideo.getStepId());
            if (relatedLesson != null) {
                String header = relatedLesson.getTitle();
                videoHeader.setText(header);
            } else {
                videoHeader.setText("");
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
            this.size.setText(sizeString);

            String quality = cachedVideo.getQuality();
            if (quality == null || quality.length() == 0) {
                currentQuality.setText("");
            } else {
                quality += "p";
                currentQuality.setText(quality);
            }
        }

    }

    public class TitleViewHolder extends GenericViewHolder {

        @BindView(R.id.button_header_download_item)
        Button headerButton;

        @BindView(R.id.text_header_download_item)
        TextView headerTextView;

        String titleDownloading;
        String titleForDownloadingButton;
        String titleCached;
        String titleForCachedButton;

        public TitleViewHolder(View itemView, final OnClickCancelListener onClickCancelListener) {
            super(itemView);
            titleDownloading = App.Companion.getAppContext().getString(R.string.downloading_title);
            titleForDownloadingButton = App.Companion.getAppContext().getString(R.string.downloading_cancel_all);
            titleCached = App.Companion.getAppContext().getString(R.string.cached_title);
            titleForCachedButton = App.Companion.getAppContext().getString(R.string.remove_all);
            headerButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickCancelListener.onClickCancel(getAdapterPosition());
                }
            });
        }

        @Override
        public void setDataOnView(int position) {
            if (position == 0 && !downloadingVideoList.isEmpty()) {
                headerTextView.setText(titleDownloading);
                headerButton.setText(titleForDownloadingButton);
            } else {
                headerTextView.setText(titleCached);
                headerButton.setText(titleForCachedButton);
            }
        }
    }

    abstract class GenericViewHolder extends RecyclerView.ViewHolder {

        public GenericViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public abstract void setDataOnView(int position);

    }

    public void notifyCachedVideoInserted(long stepId, int position) {
        // when cached video is insert, we should remove downloading
        int downloadingPos = -1;
        for (int i = 0; i < downloadingVideoList.size(); i++) {
            if (downloadingVideoList.get(i).getDownloadEntity().getStepId() == stepId) {
                downloadingPos = i;
                break;
            }
        }


        boolean isVideoWasInDownloading = downloadingPos >= 0;


        if (isVideoWasInDownloading) {
            downloadingVideoList.remove(downloadingPos);
        }

        notifyDataSetChanged();

//        downloadingPos += (getTitleCount(mDownloadingVideoList) + getTitleCount(mCachedVideoList)); //title
//        position += (getTitleCount(mDownloadingVideoList) + getTitleCount(mCachedVideoList)); //title
//
//        int realPosition = position + mDownloadingVideoList.size();
//
//        if (isVideoWasInDownloading) {
//            if (downloadingPos == realPosition) {
//                notifyDataSetChanged();
//            } else {
//                if (downloadingPos != 1) {
//                    notifyItemMoved(downloadingPos, realPosition);
//                    notifyItemRangeChanged(downloadingPos + 1, getItemCount());
//                } else {
//                    notifyDataSetChanged();
//                }
//            }
//        } else {
//            notifyItemInserted(realPosition);
//        }


    }

    public void notifyDownloadingVideoChanged(int position, long stepId) {
        DownloadingVideoItem item = downloadingVideoList.get(position);
        if (item != null) {
            if (item.getDownloadEntity().getStepId() == stepId) {
                notifyItemChanged(position + 1);
            } else {
                notifyDataSetChanged();
            }
        } else {
            notifyDataSetChanged();
        }
    }

    public void notifyDownloadingItemInserted(int position) {
        notifyDataSetChanged();
        if (downloadingVideoList.size() <= 1) {
            notifyDataSetChanged();
        } else {
            notifyItemChanged(0);
            notifyItemInserted(position + 1);
        }

    }

    public void notifyCachedVideoRemoved(int position) {
        notifyDataSetChanged(); //it is okay
    }

    public void notifyDownloadingVideoRemoved(int positionInList, long downloadId) {
        notifyDataSetChanged();
//        if (mDownloadingVideoList.isEmpty()) {
//            notifyItemRemoved(0);//title
//            notifyItemRemoved(1);//last view
//        } else {
//            notifyItemRemoved(positionInList + 1);
//        }
    }

    public static int getTitleCount(Collection collection) {
        return collection.isEmpty() ? 0 : 1;
    }
}
