package org.stepic.droid.view.adapters;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.yandex.metrica.YandexMetrica;

import org.stepic.droid.R;
import org.stepic.droid.base.MainApplication;
import org.stepic.droid.core.IScreenManager;
import org.stepic.droid.core.IShell;
import org.stepic.droid.model.Lesson;
import org.stepic.droid.model.Progress;
import org.stepic.droid.model.Section;
import org.stepic.droid.model.Unit;
import org.stepic.droid.store.CleanManager;
import org.stepic.droid.store.IDownloadManager;
import org.stepic.droid.store.operations.DatabaseFacade;
import org.stepic.droid.util.AppConstants;
import org.stepic.droid.util.JsonHelper;
import org.stepic.droid.view.dialogs.ExplainPermissionDialog;
import org.stepic.droid.view.listeners.OnClickLoadListener;
import org.stepic.droid.view.listeners.StepicOnClickItemListener;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.BindDrawable;
import butterknife.ButterKnife;

public class UnitAdapter extends RecyclerView.Adapter<UnitAdapter.UnitViewHolder> implements StepicOnClickItemListener, OnClickLoadListener {


    @Inject
    IScreenManager mScreenManager;

    @Inject
    IDownloadManager mDownloadManager;

    @Inject
    DatabaseFacade mDbManager;

    @Inject
    IShell mShell;

    @Inject
    CleanManager mCleaner;


    private final static String DELIMITER = ".";

    private final Context mContext;
    private final Section mParentSection;
    private final List<Lesson> mLessonList;
    private Activity mActivity;
    private final List<Unit> mUnitList;
    private RecyclerView mRecyclerView;
    private final Map<Long, Progress> mUnitProgressMap;

    public UnitAdapter(Context context, Section parentSection, List<Unit> unitList, List<Lesson> lessonList, Map<Long, Progress> unitProgressMap, Activity activity) {

        this.mContext = context;
        this.mParentSection = parentSection;
        this.mUnitList = unitList;
        this.mLessonList = lessonList;
        mActivity = activity;
        mUnitProgressMap = unitProgressMap;
        MainApplication.component().inject(this);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mRecyclerView = recyclerView;

    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        mRecyclerView = null;

    }

    @Override
    public UnitViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.unit_item, parent, false);
        return new UnitViewHolder(v, this, this);
    }

    @Override
    public void onBindViewHolder(UnitViewHolder holder, int position) {
        Unit unit = mUnitList.get(position);
        Lesson lesson = mLessonList.get(position);

        StringBuilder titleBuilder = new StringBuilder();
        titleBuilder.append(mParentSection.getPosition());
        titleBuilder.append(DELIMITER);
        titleBuilder.append(unit.getPosition());
        titleBuilder.append(" ");
        titleBuilder.append(lesson.getTitle());

        holder.unitTitle.setText(titleBuilder.toString());

        Progress progress = mUnitProgressMap.get(unit.getId());
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
                }

            } catch (Exception ignored) {
            }
        }

        Picasso.with(MainApplication.getAppContext())
                .load(lesson.getCover_url())
                .placeholder(holder.mLessonPlaceholderDrawable)
                .error(holder.mLessonPlaceholderDrawable)
                .into(holder.mLessonIcon);

        if (cost != 0) {
            StringBuilder sb = new StringBuilder();
            sb.append(scoreString);
            sb.append(AppConstants.DELIMITER_TEXT_SCORE);
            sb.append(cost);
            holder.mTextScore.setVisibility(View.VISIBLE);
            holder.mProgressScore.setVisibility(View.VISIBLE);
            holder.mProgressScore.setMax(cost);
            holder.mProgressScore.setProgress((int)doubleScore);
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
        return mUnitList.size();
    }


    @Override
    public void onClick(int itemPosition) {
        if (itemPosition >= 0 && itemPosition < mUnitList.size()) {
            mScreenManager.showSteps(mContext, mUnitList.get(itemPosition), mLessonList.get(itemPosition));
        }
    }

    @Override
    public void onClickLoad(int position) {
        if (position >= 0 && position < mUnitList.size()) {
            Unit unit = mUnitList.get(position);
            Lesson lesson = mLessonList.get(position);

            int permissionCheck = ContextCompat.checkSelfPermission(MainApplication.getAppContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                mShell.getSharedPreferenceHelper().storeTempPosition(position);
                if (ActivityCompat.shouldShowRequestPermissionRationale(mActivity,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                    // Show an expanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.

                    ExplainPermissionDialog dialog = new ExplainPermissionDialog();
                    dialog.show(mActivity.getFragmentManager(), null);

                } else {

                    // No explanation needed, we can request the permission.

                    ActivityCompat.requestPermissions(mActivity,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            AppConstants.REQUEST_EXTERNAL_STORAGE);

                }
                return;
            }


            if (unit.is_cached()) {
                //delete
                YandexMetrica.reportEvent(AppConstants.METRICA_CLICK_DELETE_UNIT, JsonHelper.toJson(unit));
                mCleaner.removeUnitLesson(unit, lesson);
                unit.set_loading(false);
                unit.set_cached(false);
                lesson.set_loading(false);
                lesson.set_cached(false);
                mDbManager.updateOnlyCachedLoadingLesson(lesson);
                mDbManager.updateOnlyCachedLoadingUnit(unit);
                notifyItemChanged(position);
            } else {
                if (unit.is_loading()) {
                    //cancel loading
//                    Log.d("unit", "cancel loading");
//                    mDownloadManager.cancelUnitLoading(lesson);
//                    unit.set_loading(false);
//                    unit.set_cached(false);
//                    lesson.set_loading(false);
//                    lesson.set_cached(false);
//                    mDbManager.updateOnlyCachedLoadingLesson(lesson);
//                    mDbManager.updateOnlyCachedLoadingUnit(unit);
//                    notifyItemChanged(position);

                } else {

                    YandexMetrica.reportEvent(AppConstants.METRICA_CLICK_CACHE_UNIT, JsonHelper.toJson(unit));
                    unit.set_cached(false);
                    lesson.set_cached(false);
                    unit.set_loading(true);
                    lesson.set_loading(true);
                    mDbManager.updateOnlyCachedLoadingLesson(lesson);
                    mDbManager.updateOnlyCachedLoadingUnit(unit);
                    mDownloadManager.addUnitLesson(unit, lesson);
                    notifyItemChanged(position);
                }
            }
        }
    }


    public void requestClickLoad(int position) {
        onClickLoad(position);
    }


    public static class UnitViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.cv)
        View cv;

        @Bind(R.id.unit_title)
        TextView unitTitle;


        @Bind(R.id.pre_load_iv)
        View preLoadIV;

        @Bind(R.id.when_load_view)
        View whenLoad;

        @Bind(R.id.after_load_iv)
        View afterLoad;

        @Bind(R.id.load_button)
        View loadButton;

        @Bind(R.id.viewed_item)
        View viewedItem;

        @Bind(R.id.text_score)
        TextView mTextScore;

        @Bind(R.id.student_progress_score_bar)
        ProgressBar mProgressScore;

        @Bind(R.id.lesson_icon)
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
