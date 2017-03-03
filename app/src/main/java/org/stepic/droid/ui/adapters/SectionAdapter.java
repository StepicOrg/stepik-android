package org.stepic.droid.ui.adapters;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.stepic.droid.R;
import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.base.App;
import org.stepic.droid.core.ScreenManager;
import org.stepic.droid.core.Shell;
import org.stepic.droid.core.presenters.CalendarPresenter;
import org.stepic.droid.model.Course;
import org.stepic.droid.model.Section;
import org.stepic.droid.model.SectionLoadingState;
import org.stepic.droid.store.SectionDownloader;
import org.stepic.droid.store.operations.DatabaseFacade;
import org.stepic.droid.ui.custom.progressbutton.ProgressWheel;
import org.stepic.droid.ui.dialogs.ExplainExternalStoragePermissionDialog;
import org.stepic.droid.ui.dialogs.OnLoadPositionListener;
import org.stepic.droid.ui.dialogs.VideoQualityDetailedDialog;
import org.stepic.droid.ui.listeners.OnClickLoadListener;
import org.stepic.droid.ui.listeners.StepicOnClickItemListener;
import org.stepic.droid.util.AppConstants;
import org.stepic.droid.util.ColorUtil;
import org.stepic.droid.viewmodel.ProgressViewModel;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

import javax.inject.Inject;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class SectionAdapter extends RecyclerView.Adapter<SectionAdapter.GenericViewHolder> implements OnClickLoadListener, OnLoadPositionListener {
    private final static String SECTION_TITLE_DELIMETER = ". ";

    public static final int TYPE_SECTION_ITEM = 1;
    public static final int TYPE_TITLE = 2;

    public static final int PRE_SECTION_LIST_DELTA = 1;

    private int defaultHighlightPosition = -1;

    @Inject
    ScreenManager screenManager;

    @Inject
    DatabaseFacade databaseFacade;

    @Inject
    Shell shell;

    @Inject
    Analytic analytic;

    @Inject
    ThreadPoolExecutor threadPoolExecutor;

    @Inject
    SectionDownloader sectionDownloader;


    private List<Section> sections;
    private AppCompatActivity activity;
    private CalendarPresenter calendarPresenter;
    private Course course;
    private boolean needShowCalendarWidget;
    private Drawable highlightDrawable;
    @ColorInt
    private int defaultColor;
    private Map<String, ProgressViewModel> progressMap;
    private Map<Long, SectionLoadingState> sectionIdToLoadingStateMap;
    private final int durationMillis = 3000;

    public void setDefaultHighlightPosition(int defaultHighlightPosition) {
        this.defaultHighlightPosition = defaultHighlightPosition;
    }

    public SectionAdapter(List<Section> sections, AppCompatActivity activity, CalendarPresenter calendarPresenter, Map<String, ProgressViewModel> progressMap, Map<Long, SectionLoadingState> sectionIdToLoadingStateMap) {
        this.sections = sections;
        this.activity = activity;
        this.calendarPresenter = calendarPresenter;
        highlightDrawable = ContextCompat.getDrawable(activity, R.drawable.section_background);
        defaultColor = ColorUtil.INSTANCE.getColorArgb(R.color.white, activity);
        this.progressMap = progressMap;
        this.sectionIdToLoadingStateMap = sectionIdToLoadingStateMap;
        App.component().inject(this);
    }


    @Override
    public GenericViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_SECTION_ITEM) {
            View v = LayoutInflater.from(activity).inflate(R.layout.section_item, parent, false);
            return new SectionViewHolder(v);
        } else if (viewType == TYPE_TITLE) {
            View v = LayoutInflater.from(activity).inflate(R.layout.export_calendar_view, parent, false);
            return new CalendarViewHolder(v);
        } else {
            return null;
        }
    }


    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_TITLE;
        } else {
            return TYPE_SECTION_ITEM;
        }
    }


    @Override
    public void onBindViewHolder(GenericViewHolder holder, int position) {
        holder.setDataOnView(position);
    }

    @Override
    public int getItemCount() {
        return sections.size() + PRE_SECTION_LIST_DELTA;
    }


    public void requestClickLoad(int position) {
        onClickLoad(position);
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    @Override
    public void onClickLoad(int adapterPosition) {
        int sectionPosition = adapterPosition - PRE_SECTION_LIST_DELTA;
        if (sectionPosition >= 0 && sectionPosition < sections.size()) {
            final Section section = sections.get(sectionPosition);

            int permissionCheck = ContextCompat.checkSelfPermission(App.getAppContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                shell.getSharedPreferenceHelper().storeTempPosition(adapterPosition);
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

            if (section.is_cached()) {
                analytic.reportEvent(Analytic.Interaction.CLICK_DELETE_SECTION, section.getId() + "");
                section.set_loading(false);
                section.set_cached(false);
                threadPoolExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        databaseFacade.updateOnlyCachedLoadingSection(section);
                        sectionDownloader.deleteWholeSection(section.getId());
                    }
                });
                notifyItemChanged(adapterPosition);
            } else {
                if (section.is_loading()) {
                    //cancel loading
                    analytic.reportEvent(Analytic.Interaction.CLICK_CANCEL_SECTION, section.getId() + "");
                    section.set_loading(false);
                    section.set_cached(false);
                    threadPoolExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            databaseFacade.updateOnlyCachedLoadingSection(section);
                            sectionDownloader.cancelSectionLoading(section.getId());
                        }
                    });

                    notifyItemChanged(adapterPosition);
                } else {
                    if (shell.getSharedPreferenceHelper().isNeedToShowVideoQualityExplanation()) {
                        VideoQualityDetailedDialog dialogFragment = VideoQualityDetailedDialog.Companion.newInstance(adapterPosition);
                        dialogFragment.setOnLoadPositionListener(this);
                        if (!dialogFragment.isAdded()) {
                            FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
                            ft.add(dialogFragment, null);
                            ft.commitAllowingStateLoss();
                        }
                    } else {
                        loadSection(adapterPosition);
                    }
                }
            }
        }
    }

    private void loadSection(int adapterPosition) {
        int sectionPosition = adapterPosition - PRE_SECTION_LIST_DELTA;
        if (sectionPosition >= 0 && sectionPosition < sections.size()) {
            final Section section = sections.get(sectionPosition);
            analytic.reportEvent(Analytic.Interaction.CLICK_CACHE_SECTION, section.getId() + "");
            section.set_cached(false);
            section.set_loading(true);
            threadPoolExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    databaseFacade.updateOnlyCachedLoadingSection(section);
                    sectionDownloader.downloadSection(section.getId());
                }
            });
            notifyItemChanged(adapterPosition);
        }
    }

    public void setNeedShowCalendarWidget(boolean needShowCalendarWidget) {
        this.needShowCalendarWidget = needShowCalendarWidget;
    }

    @Override
    public void onNeedLoadPosition(int adapterPosition) {
        loadSection(adapterPosition);
    }

    private void onClickStartExam(int adapterPosition) {
        int position = adapterPosition - PRE_SECTION_LIST_DELTA;
        if (position >= 0 && position < sections.size()) {
            analytic.reportEvent(Analytic.Exam.SHOW_EXAM);
            Section section = sections.get(position);
            screenManager.openSyllabusInWeb(activity, section.getCourse());
        }
    }

    class SectionViewHolder extends GenericViewHolder implements StepicOnClickItemListener {

        @BindView(R.id.cv)
        ViewGroup cv;

        @BindView(R.id.section_title)
        TextView sectionTitle;

        @BindView(R.id.start_date)
        TextView startDate;

        @BindView(R.id.soft_deadline)
        TextView softDeadline;

        @BindView(R.id.hard_deadline)
        TextView hardDeadline;

        @BindString(R.string.hard_deadline_section)
        String hardDeadlineString;
        @BindString(R.string.soft_deadline_section)
        String softDeadlineString;
        @BindString(R.string.begin_date_section)
        String beginDateString;

        @BindView(R.id.pre_load_iv)
        View preLoadIV;

        @BindView(R.id.when_load_view)
        ProgressWheel whenLoad;

        @BindView(R.id.after_load_iv)
        View afterLoad;

        @BindView(R.id.load_button)
        View loadButton;

        @BindView(R.id.exam_title)
        View examTitle;

        @BindView(R.id.start_exam_button)
        View startExamButton;


        @BindView(R.id.section_text_score)
        TextView textScore;

        @BindView(R.id.section_student_progress_score_bar)
        ProgressBar progressScore;

        private long oldSectionId = -1;


        public SectionViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    SectionViewHolder.this.onClick(getAdapterPosition());
                }

            });

            loadButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickLoad(getAdapterPosition());
                }
            });

            startExamButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SectionAdapter.this.onClickStartExam(getAdapterPosition());
                }
            });
        }


        @Override
        public void onClick(int adapterPosition) {
            int itemPosition = adapterPosition - PRE_SECTION_LIST_DELTA;
            if (itemPosition >= 0 && itemPosition < sections.size()) {
                screenManager.showUnitsForSection(activity, sections.get(itemPosition));
            }
        }

        @Override
        public void setDataOnView(int positionInAdapter) {
            // the 0 index always for calendar, we make its GONE, if calendar is not needed.
            int position = positionInAdapter - PRE_SECTION_LIST_DELTA;
            Section section = sections.get(position);

            Timber.d("onBind Holder = %s", this);
            long sectionId = section.getId();
            boolean needAnimation = true;
            if (oldSectionId != sectionId) {
                //if rebinding than animation is not needed
                oldSectionId = sectionId;
                needAnimation = false;
            }


            String title = section.getTitle();
            int positionOfSection = section.getPosition();
            title = positionOfSection + SECTION_TITLE_DELIMETER + title;
            sectionTitle.setText(title);


            String formattedBeginDate = section.getFormattedBeginDate();
            if (formattedBeginDate.equals("")) {
                startDate.setText("");
                startDate.setVisibility(View.GONE);
            } else {
                startDate.setText(beginDateString + " " + formattedBeginDate);
                startDate.setVisibility(View.VISIBLE);
            }

            String formattedSoftDeadline = section.getFormattedSoftDeadline();
            if (formattedSoftDeadline.equals("")) {
                softDeadline.setText("");
                softDeadline.setVisibility(View.GONE);
            } else {
                softDeadline.setText(softDeadlineString + ": " + formattedSoftDeadline);
                softDeadline.setVisibility(View.VISIBLE);
            }

            String formattedHardDeadline = section.getFormattedHardDeadline();
            if (formattedHardDeadline.equals("")) {
                hardDeadline.setText("");
                hardDeadline.setVisibility(View.GONE);
            } else {
                hardDeadline.setText(hardDeadlineString + ": " + formattedHardDeadline);
                hardDeadline.setVisibility(View.VISIBLE);
            }

            if ((section.is_active() || (section.getActions() != null && section.getActions().getTest_section() != null)) && course.getEnrollment() > 0 && !section.isExam()) {

                int strong_text_color = ColorUtil.INSTANCE.getColorArgb(R.color.stepic_regular_text, App.getAppContext());

                sectionTitle.setTextColor(strong_text_color);
                cv.setFocusable(false);
                cv.setClickable(true);
                cv.setFocusableInTouchMode(false);

                loadButton.setVisibility(View.VISIBLE);
                if (section.is_cached()) {
                    //cached
                    preLoadIV.setVisibility(View.GONE);
                    whenLoad.setVisibility(View.INVISIBLE);
                    afterLoad.setVisibility(View.VISIBLE); //can

                    whenLoad.setProgressPortion(0, false);
                } else {
                    if (section.is_loading()) {

                        preLoadIV.setVisibility(View.GONE);
                        whenLoad.setVisibility(View.VISIBLE);
                        afterLoad.setVisibility(View.GONE);

                        SectionLoadingState sectionLoadingState = sectionIdToLoadingStateMap.get(section.getId());
                        if (sectionLoadingState != null) {
                            whenLoad.setProgressPortion(sectionLoadingState.getPortion(), needAnimation);
                        }

                    } else {
                        //not cached not loading
                        preLoadIV.setVisibility(View.VISIBLE);
                        whenLoad.setVisibility(View.INVISIBLE);
                        afterLoad.setVisibility(View.GONE);

                        whenLoad.setProgressPortion(0, false);
                    }

                }
            } else {
                //Not active section or not enrollment

                loadButton.setVisibility(View.GONE);
                preLoadIV.setVisibility(View.GONE);
                whenLoad.setVisibility(View.INVISIBLE);
                afterLoad.setVisibility(View.GONE);

                int weak_text_color = ColorUtil.INSTANCE.getColorArgb(R.color.stepic_weak_text, App.getAppContext());
                sectionTitle.setTextColor(weak_text_color);
                cv.setFocusable(false);
                cv.setClickable(false);
                cv.setFocusableInTouchMode(false);
            }

            showExamView(section.isExam());

            if (defaultHighlightPosition >= 0 && defaultHighlightPosition == position) {
                cv.clearAnimation();
                setAnimation(cv);
            } else {
                cv.setBackgroundColor(defaultColor);
            }

            ProgressViewModel progressViewModel;
            try {
                progressViewModel = progressMap.get(section.getProgress());
            } catch (Exception ex) {
                progressViewModel = null;
            }

            boolean needShow = progressViewModel != null && progressViewModel.getCost() > 0;
            int progressVisibility = needShow ? View.VISIBLE : View.GONE;
            if (needShow) {
                textScore.setText(progressViewModel.getScoreAndCostText());
                progressScore.setMax(progressViewModel.getCost());
                progressScore.setProgress(progressViewModel.getScore());
            }
            textScore.setVisibility(progressVisibility);
            progressScore.setVisibility(progressVisibility);
        }

        private void showExamView(boolean isExam) {
            int needShow = isExam ? View.VISIBLE : View.GONE;
            examTitle.setVisibility(needShow);
            startExamButton.setVisibility(needShow);
        }

        @Override
        public void clearAnimation() {
            Drawable backgroundDrawable = cv.getBackground();
            if (backgroundDrawable != null && backgroundDrawable instanceof TransitionDrawable) {
                cv.setBackgroundColor(defaultColor);
            }
            cv.clearAnimation();
        }

        private void setAnimation(View viewToAnimate) {
            TransitionDrawable drawable = (TransitionDrawable) highlightDrawable;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                viewToAnimate.setBackground(highlightDrawable);
            } else {
                viewToAnimate.setBackgroundDrawable(highlightDrawable);
            }
            drawable.startTransition(durationMillis);
            defaultHighlightPosition = -1;
        }
    }

    class CalendarViewHolder extends GenericViewHolder {

        View rootView;

        @BindView(R.id.export_calendar_button)
        View addToCalendarButton;

        @BindView(R.id.not_now_button)
        View notNowButton;


        public CalendarViewHolder(View itemView) {
            super(itemView);
            rootView = itemView;
            //calendar view holder is created only once
            addToCalendarButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    analytic.reportEventWithIdName(Analytic.Calendar.USER_CLICK_ADD_WIDGET, course.getCourseId() + "", course.getTitle());
                    calendarPresenter.addDeadlinesToCalendar(SectionAdapter.this.sections, null);
                }
            });

            notNowButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    analytic.reportEventWithIdName(Analytic.Calendar.USER_CLICK_NOT_NOW, course.getCourseId() + "", course.getTitle());
                    calendarPresenter.clickNotNow();
                }
            });

        }

        @Override
        public void setDataOnView(int position) {
            // TODO: 19.07.16 resolve showing of calendar depend on mCourse and mSections.
            if (shouldBeHidden()) {
                hide();
            } else {
                show();
            }
        }

        @Override
        public void clearAnimation() {
            rootView.clearAnimation();
        }

        private boolean shouldBeHidden() {
            return !SectionAdapter.this.needShowCalendarWidget;
        }

        private void hide() {
            changeHeightOfRootView(0);
        }

        private void show() {
            changeHeightOfRootView(ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        private void changeHeightOfRootView(int height) {
            ViewGroup.LayoutParams layoutParams = rootView.getLayoutParams();
            layoutParams.height = height;
            rootView.setLayoutParams(layoutParams);
        }
    }

    abstract class GenericViewHolder extends RecyclerView.ViewHolder {

        public GenericViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public abstract void setDataOnView(int position);

        public abstract void clearAnimation();
    }

    @Override
    public void onViewDetachedFromWindow(GenericViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        ((GenericViewHolder) holder).clearAnimation();
    }
}
