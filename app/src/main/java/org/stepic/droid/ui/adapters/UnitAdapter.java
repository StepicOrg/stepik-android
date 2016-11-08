package org.stepic.droid.ui.adapters;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
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
import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.base.MainApplication;
import org.stepic.droid.core.ScreenManager;
import org.stepic.droid.core.IShell;
import org.stepic.droid.model.Lesson;
import org.stepic.droid.model.Progress;
import org.stepic.droid.model.Section;
import org.stepic.droid.model.Unit;
import org.stepic.droid.store.CleanManager;
import org.stepic.droid.store.IDownloadManager;
import org.stepic.droid.store.operations.DatabaseFacade;
import org.stepic.droid.ui.dialogs.ExplainExternalStoragePermissionDialog;
import org.stepic.droid.ui.dialogs.OnLoadPositionListener;
import org.stepic.droid.ui.dialogs.VideoQualityDetailedDialog;
import org.stepic.droid.ui.listeners.OnClickLoadListener;
import org.stepic.droid.ui.listeners.StepicOnClickItemListener;
import org.stepic.droid.util.AppConstants;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

import javax.inject.Inject;

import butterknife.BindDrawable;
import butterknife.BindView;
import butterknife.ButterKnife;

public class UnitAdapter extends RecyclerView.Adapter<UnitAdapter.UnitViewHolder> implements StepicOnClickItemListener, OnClickLoadListener, OnLoadPositionListener {


    @Inject
    ScreenManager screenManager;

    @Inject
    IDownloadManager downloadManager;

    @Inject
    DatabaseFacade databaseFacade;

    @Inject
    IShell shell;

    @Inject
    CleanManager cleanManager;

    @Inject
    Analytic analytic;

    @Inject
    ThreadPoolExecutor threadPoolExecutor;


    private final static String DELIMITER = ".";

    private final Section parentSection;
    private final List<Lesson> lessonList;
    private AppCompatActivity activity;
    private final List<Unit> unitList;
    private RecyclerView recyclerView;
    private final Map<Long, Progress> unitProgressMap;

    public UnitAdapter(Section parentSection, List<Unit> unitList, List<Lesson> lessonList, Map<Long, Progress> unitProgressMap, AppCompatActivity activity) {
        this.activity = activity;
        this.parentSection = parentSection;
        this.unitList = unitList;
        this.lessonList = lessonList;
        this.unitProgressMap = unitProgressMap;
        MainApplication.component().inject(this);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        this.recyclerView = null;
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

        StringBuilder titleBuilder = new StringBuilder();
        titleBuilder.append(parentSection.getPosition());
        titleBuilder.append(DELIMITER);
        titleBuilder.append(unit.getPosition());
        titleBuilder.append(" ");
        titleBuilder.append(lesson.getTitle());

        holder.unitTitle.setText(titleBuilder.toString());

        Progress progress = unitProgressMap.get(unit.getId());
        int cost = 0;
        double doubleScore = 0;
        String scoreString = "";
        if (progress != null) {
            cost = progress.getCost();
            scoreString = progress.getScore();
            try {
                doubleScore = Double.parseDouble(scoreString);
                if ((doubleScore == Math.floor(doubleScore)) && !Double.isInfinite(doubleScore)) {
                    scoreString = (int) doubleScore + "";
                } else {
                    scoreString = String.format(Locale.getDefault(), "%.2f", doubleScore);
                }

            } catch (Exception ignored) {
            }
        }

        Glide.with(MainApplication.getAppContext())
                .load(lesson.getCover_url())
                .placeholder(holder.mLessonPlaceholderDrawable)
                .into(holder.mLessonIcon);

        if (cost != 0) {
            StringBuilder sb = new StringBuilder();
            sb.append(scoreString);
            sb.append(AppConstants.DELIMITER_TEXT_SCORE);
            sb.append(cost);
            holder.mTextScore.setVisibility(View.VISIBLE);
            holder.mProgressScore.setVisibility(View.VISIBLE);
            holder.mProgressScore.setMax(cost);
            holder.mProgressScore.setProgress((int) doubleScore);
            holder.mTextScore.setText(sb.toString());
        } else {
            holder.mTextScore.setVisibility(View.GONE);
            holder.mProgressScore.setVisibility(View.GONE);
        }


        if (unit.is_viewed_custom()) {
            holder.viewedItem.setVisibility(View.VISIBLE);
        } else {
            holder.viewedItem.setVisibility(View.INVISIBLE);
        }

        if (unit.is_cached()) {

            // FIXME: 05.11.15 Delete course from cache. Set CLICK LISTENER.
            //cached

            holder.preLoadIV.setVisibility(View.GONE);
            holder.whenLoad.setVisibility(View.INVISIBLE);
            holder.afterLoad.setVisibility(View.VISIBLE); //can

        } else {
            if (unit.is_loading()) {

                holder.preLoadIV.setVisibility(View.GONE);
                holder.whenLoad.setVisibility(View.VISIBLE);
                holder.afterLoad.setVisibility(View.GONE);

                //todo: add cancel of downloading
            } else {
                //not cached not loading
                holder.preLoadIV.setVisibility(View.VISIBLE);
                holder.whenLoad.setVisibility(View.INVISIBLE);
                holder.afterLoad.setVisibility(View.GONE);
            }

        }
    }

    @Override
    public int getItemCount() {
        return unitList.size();
    }


    @Override
    public void onClick(int itemPosition) {
        if (itemPosition >= 0 && itemPosition < unitList.size()) {
            screenManager.showSteps(activity, unitList.get(itemPosition), lessonList.get(itemPosition), parentSection);
        }
    }

    @Override
    public void onClickLoad(int position) {
        if (position >= 0 && position < unitList.size()) {
            final Unit unit = unitList.get(position);
            final Lesson lesson = lessonList.get(position);

            int permissionCheck = ContextCompat.checkSelfPermission(MainApplication.getAppContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                shell.getSharedPreferenceHelper().storeTempPosition(position);
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


            if (unit.is_cached()) {
                //delete
                analytic.reportEvent(Analytic.Interaction.CLICK_DELETE_UNIT, unit.getId() + "");
                cleanManager.removeUnitLesson(unit, lesson);
                unit.set_loading(false);
                unit.set_cached(false);
                lesson.set_loading(false);
                lesson.set_cached(false);
                threadPoolExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        databaseFacade.updateOnlyCachedLoadingLesson(lesson);
                        databaseFacade.updateOnlyCachedLoadingUnit(unit);
                    }
                });
                notifyItemChanged(position);
            } else {
                if (unit.is_loading()) {
                    //cancel loading
                    analytic.reportEvent(Analytic.Interaction.CLICK_CANCEL_UNIT, unit.getId() + "");
                    screenManager.showDownload(activity);
                } else {
                    if (shell.getSharedPreferenceHelper().isNeedToShowVideoQualityExplanation()) {
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
            final Unit unit = unitList.get(position);
            final Lesson lesson = lessonList.get(position);
            analytic.reportEvent(Analytic.Interaction.CLICK_CACHE_UNIT, unit.getId() + "");
            unit.set_cached(false);
            lesson.set_cached(false);
            unit.set_loading(true);
            lesson.set_loading(true);
            threadPoolExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    databaseFacade.updateOnlyCachedLoadingLesson(lesson);
                    databaseFacade.updateOnlyCachedLoadingUnit(unit);
                    downloadManager.addUnitLesson(unit, lesson);
                }
            });
            notifyItemChanged(position);
        }
    }


    public void requestClickLoad(int position) {
        onClickLoad(position);
    }

    @Override
    public void onNeedLoadPosition(int adapterPosition) {
        load(adapterPosition);
    }


    public static class UnitViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.cv)
        View cv;

        @BindView(R.id.unit_title)
        TextView unitTitle;


        @BindView(R.id.pre_load_iv)
        View preLoadIV;

        @BindView(R.id.when_load_view)
        View whenLoad;

        @BindView(R.id.after_load_iv)
        View afterLoad;

        @BindView(R.id.load_button)
        View loadButton;

        @BindView(R.id.viewed_item)
        View viewedItem;

        @BindView(R.id.text_score)
        TextView mTextScore;

        @BindView(R.id.student_progress_score_bar)
        ProgressBar mProgressScore;

        @BindView(R.id.lesson_icon)
        ImageView mLessonIcon;

        @BindDrawable(R.drawable.ic_lesson_cover)
        Drawable mLessonPlaceholderDrawable;

        public UnitViewHolder(View itemView, final StepicOnClickItemListener listener, final OnClickLoadListener loadListener) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onClick(getAdapterPosition());
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
