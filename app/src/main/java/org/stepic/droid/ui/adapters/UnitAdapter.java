package org.stepic.droid.ui.adapters;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.stepic.droid.R;
import org.stepic.droid.analytic.AmplitudeAnalytic;
import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.base.App;
import org.stepic.droid.core.ScreenManager;
import org.stepic.droid.core.downloadingstate.DownloadingPresenter;
import org.stepic.droid.core.presenters.DownloadingInteractionPresenter;
import org.stepic.droid.model.Lesson;
import org.stepik.android.model.structure.Progress;
import org.stepic.droid.model.Section;
import org.stepic.droid.model.Unit;
import org.stepic.droid.preferences.SharedPreferenceHelper;
import org.stepic.droid.storage.LessonDownloader;
import org.stepic.droid.storage.operations.DatabaseFacade;
import org.stepic.droid.transformers.ProgressTransformerKt;
import org.stepic.droid.ui.custom.progressbutton.ProgressWheel;
import org.stepic.droid.ui.dialogs.DeleteItemDialogFragment;
import org.stepic.droid.ui.dialogs.ExplainExternalStoragePermissionDialog;
import org.stepic.droid.ui.dialogs.OnLoadPositionListener;
import org.stepic.droid.ui.dialogs.VideoQualityDetailedDialog;
import org.stepic.droid.ui.fragments.UnitsFragment;
import org.stepic.droid.ui.listeners.OnClickLoadListener;
import org.stepic.droid.ui.listeners.OnItemClickListener;
import org.stepic.droid.util.AppConstants;
import org.stepic.droid.viewmodel.ProgressViewModel;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

import javax.inject.Inject;

import butterknife.BindDrawable;
import butterknife.BindView;
import butterknife.ButterKnife;
import kotlin.Pair;
import kotlin.collections.MapsKt;

public class UnitAdapter extends RecyclerView.Adapter<UnitAdapter.UnitViewHolder> implements OnItemClickListener, OnClickLoadListener, OnLoadPositionListener {

    @Inject
    ScreenManager screenManager;

    @Inject
    DatabaseFacade databaseFacade;

    @Inject
    SharedPreferenceHelper sharedPreferenceHelper;

    @Inject
    Analytic analytic;

    @Inject
    ThreadPoolExecutor threadPoolExecutor;

    @Inject
    LessonDownloader lessonDownloader;


    private final static String DELIMITER = ".";

    private Section parentSection;
    private final List<Lesson> lessonList;
    private final DownloadingPresenter downloadingPresenter;
    private AppCompatActivity activity;
    private final List<Unit> unitList;
    private final Map<Long, Progress> unitProgressMap;
    private final Map<Long, Float> lessonIdToUnitLoadingStateMap;
    private Fragment fragment;
    private final DownloadingInteractionPresenter downloadingInteractionPresenter;

    public UnitAdapter(Section parentSection,
                       DownloadingPresenter downloadingPresenter,
                       List<Unit> unitList,
                       List<Lesson> lessonList,
                       Map<Long, Progress> unitProgressMap,
                       AppCompatActivity activity,
                       Map<Long, Float> lessonIdToUnitLoadingStateMap,
                       Fragment fragment,
                       DownloadingInteractionPresenter downloadingInteractionPresenter) {
        this.downloadingPresenter = downloadingPresenter;
        this.activity = activity;
        this.parentSection = parentSection;
        this.unitList = unitList;
        this.lessonList = lessonList;
        this.unitProgressMap = unitProgressMap;
        this.lessonIdToUnitLoadingStateMap = lessonIdToUnitLoadingStateMap;
        this.fragment = fragment;
        this.downloadingInteractionPresenter = downloadingInteractionPresenter;
        App.Companion.component().inject(this);
    }

    public void setSection(Section section) {
        this.parentSection = section;
    }


    @Override
    public UnitViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(activity).inflate(R.layout.unit_item, parent, false);
        return new UnitViewHolder(v, this, this);
    }

    @Override
    public void onBindViewHolder(UnitViewHolder holder, int position) {
        Unit unit = unitList.get(position);
        Lesson lesson = lessonList.get(position);

        long lessonId = lesson.getId();
        boolean needAnimation = true;
        if (holder.oldLessonId != lessonId) {
            //if rebinding than animation is not needed
            holder.oldLessonId = lessonId;
            needAnimation = false;
        }

        StringBuilder titleBuilder = new StringBuilder();
        titleBuilder.append(parentSection.getPosition());
        titleBuilder.append(DELIMITER);
        titleBuilder.append(unit.getPosition());
        titleBuilder.append(" ");
        titleBuilder.append(lesson.getTitle());

        holder.unitTitle.setText(titleBuilder.toString());

        Glide.with(App.Companion.getAppContext())
                .load(lesson.getCover_url())
                .placeholder(holder.lessonPlaceholderDrawable)
                .into(holder.lessonIcon);

        Progress progress = unitProgressMap.get(unit.getId());
        ProgressViewModel progressViewModel = null;
        try {
            progressViewModel = ProgressTransformerKt.transformToViewModel(progress);
        } catch (Exception ignored) {
        }

        boolean needShow = progressViewModel != null && progressViewModel.getCost() > 0;
        int progressVisibility = needShow ? View.VISIBLE : View.GONE;
        if (needShow) {
            holder.textScore.setText(progressViewModel.getScoreAndCostText());
            holder.progressScore.setMax(progressViewModel.getCost());
            holder.progressScore.setProgress(progressViewModel.getScore());
        }
        holder.textScore.setVisibility(progressVisibility);
        holder.progressScore.setVisibility(progressVisibility);

        if (unit.is_viewed_custom()) {
            holder.viewedItem.setVisibility(View.VISIBLE);
        } else {
            holder.viewedItem.setVisibility(View.INVISIBLE);
        }

        if (lesson.is_cached()) {
            //cached
            holder.preLoadIV.setVisibility(View.GONE);
            holder.whenLoad.setVisibility(View.INVISIBLE);
            holder.afterLoad.setVisibility(View.VISIBLE); //can

            holder.whenLoad.setProgressPortion(0, false);
        } else {
            if (lesson.is_loading()) {

                holder.preLoadIV.setVisibility(View.GONE);
                holder.whenLoad.setVisibility(View.VISIBLE);
                holder.afterLoad.setVisibility(View.GONE);

                Float lessonLoadingPortion = lessonIdToUnitLoadingStateMap.get(lesson.getId());
                if (lessonLoadingPortion != null) {
                    holder.whenLoad.setProgressPortion(lessonLoadingPortion, needAnimation);
                }
            } else {
                //not cached not loading
                holder.preLoadIV.setVisibility(View.VISIBLE);
                holder.whenLoad.setVisibility(View.INVISIBLE);
                holder.afterLoad.setVisibility(View.GONE);
                holder.whenLoad.setProgressPortion(0, false);
            }

        }
    }

    @Override
    public int getItemCount() {
        return unitList.size();
    }

    @Override
    public void onItemClick(int itemPosition) {
        if (itemPosition >= 0 && itemPosition < unitList.size()) {
            screenManager.showSteps(activity, unitList.get(itemPosition), lessonList.get(itemPosition), parentSection);
        }
    }

    @Override
    public void onClickLoad(int position) {
        if (position >= 0 && position < unitList.size()) {
            final Unit unit = unitList.get(position);
            final Lesson lesson = lessonList.get(position);

            int permissionCheck = ContextCompat.checkSelfPermission(App.Companion.getAppContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                sharedPreferenceHelper.storeTempPosition(position);
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.

                    DialogFragment dialog = ExplainExternalStoragePermissionDialog.newInstance();
                    if (!dialog.isAdded()) {
                        dialog.show(activity.getSupportFragmentManager(), null);
                    }

                } else {

                    // No explanation needed, we can request the permission.

                    ActivityCompat.requestPermissions(activity,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            AppConstants.REQUEST_EXTERNAL_STORAGE);

                }
                return;
            }


            if (lesson.is_cached()) {
                //delete
                analytic.reportEvent(Analytic.Interaction.CLICK_DELETE_LESSON, unit.getId() + "");
                analytic.reportAmplitudeEvent(AmplitudeAnalytic.Downloads.DELETED,
                        MapsKt.mapOf(new Pair<String, Object>(AmplitudeAnalytic.Downloads.PARAM_CONTENT, AmplitudeAnalytic.Downloads.Values.LESSON)));
                DeleteItemDialogFragment dialogFragment = DeleteItemDialogFragment.newInstance(position);
                dialogFragment.setTargetFragment(fragment, UnitsFragment.DELETE_POSITION_REQUEST_CODE);
                if (!dialogFragment.isAdded()) {
                    FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
                    ft.add(dialogFragment, null);
                    ft.commitAllowingStateLoss();
                }
            } else {
                if (lesson.is_loading()) {
                    //cancel loading
                    analytic.reportEvent(Analytic.Interaction.CLICK_CANCEL_LESSON, unit.getId() + "");
                    analytic.reportAmplitudeEvent(AmplitudeAnalytic.Downloads.CANCELLED,
                            MapsKt.mapOf(new Pair<String, Object>(AmplitudeAnalytic.Downloads.PARAM_CONTENT, AmplitudeAnalytic.Downloads.Values.LESSON)));
                    lesson.set_loading(false);
                    lesson.set_cached(false);
                    threadPoolExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            databaseFacade.updateOnlyCachedLoadingLesson(lesson);
                            lessonDownloader.cancelLessonLoading(lesson.getId());
                        }
                    });
                    downloadingPresenter.onStateChanged(lesson.getId(), false);
                    notifyItemChanged(position);
                } else {
                    if (sharedPreferenceHelper.isNeedToShowVideoQualityExplanation()) {
                        VideoQualityDetailedDialog dialogFragment = VideoQualityDetailedDialog.Companion.newInstance(position);
                        dialogFragment.setOnLoadPositionListener(this);
                        if (!dialogFragment.isAdded()) {
                            FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
                            ft.add(dialogFragment, null);
                            ft.commitAllowingStateLoss();
                        }
                    } else {
                        load(position);
                    }
                }
            }
        }
    }

    private void load(int position) {
        if (position >= 0 && position < unitList.size()) {
            downloadingInteractionPresenter.checkOnLoading(position);
        }
    }

    public void loadAfterDetermineNetworkState(int position) {
        if (position >= 0 && position < unitList.size()) {

            final Unit unit = unitList.get(position);
            final Lesson lesson = lessonList.get(position);
            analytic.reportEvent(Analytic.Interaction.CLICK_CACHE_LESSON, unit.getId() + "");
            analytic.reportAmplitudeEvent(AmplitudeAnalytic.Downloads.STARTED,
                    MapsKt.mapOf(new Pair<String, Object>(AmplitudeAnalytic.Downloads.PARAM_CONTENT, AmplitudeAnalytic.Downloads.Values.LESSON)));
            lesson.set_cached(false);
            lesson.set_loading(true);
            downloadingPresenter.onStateChanged(lesson.getId(), lesson.is_loading());
            threadPoolExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    databaseFacade.updateOnlyCachedLoadingLesson(lesson);
                    lessonDownloader.downloadLesson(lesson.getId());
                }
            });
            notifyItemChanged(position);
        }
    }


    public void requestClickLoad(int position) {
        onClickLoad(position);
    }

    public void requestClickDeleteSilence(int position) {
        if (position < lessonList.size() && position >= 0) {
            final Lesson lesson = lessonList.get(position);
            onClickDelete(lesson, position);
        }
    }

    private void onClickDelete(final Lesson lesson, int position) {
        lesson.set_loading(false);
        lesson.set_cached(false);
        threadPoolExecutor.execute(new Runnable() {
            @Override
            public void run() {
                databaseFacade.updateOnlyCachedLoadingLesson(lesson);
                lessonDownloader.deleteWholeLesson(lesson.getId());
            }
        });
        notifyItemChanged(position);
    }

    @Override
    public void onNeedLoadPosition(int adapterPosition) {
        load(adapterPosition);
    }


    static class UnitViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.cv)
        View cv;

        @BindView(R.id.unit_title)
        TextView unitTitle;


        @BindView(R.id.pre_load_iv)
        View preLoadIV;

        @BindView(R.id.when_load_view)
        ProgressWheel whenLoad;

        @BindView(R.id.after_load_iv)
        View afterLoad;

        @BindView(R.id.load_button)
        View loadButton;

        @BindView(R.id.viewed_item)
        View viewedItem;

        @BindView(R.id.text_score)
        TextView textScore;

        @BindView(R.id.student_progress_score_bar)
        ProgressBar progressScore;

        @BindView(R.id.lesson_icon)
        ImageView lessonIcon;

        @BindDrawable(R.drawable.general_placeholder)
        Drawable lessonPlaceholderDrawable;

        long oldLessonId = -1;

        UnitViewHolder(View itemView, final OnItemClickListener listener, final OnClickLoadListener loadListener) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(getAdapterPosition());
                }
            });

            loadButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    loadListener.onClickLoad(getAdapterPosition());
                }
            });

        }
    }

}
