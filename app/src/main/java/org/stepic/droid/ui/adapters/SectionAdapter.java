package org.stepic.droid.ui.adapters;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.LongSparseArray;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.stepic.droid.R;
import org.stepic.droid.analytic.AmplitudeAnalytic;
import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.base.App;
import org.stepic.droid.core.ScreenManager;
import org.stepic.droid.core.presenters.CalendarPresenter;
import org.stepic.droid.core.presenters.DownloadingInteractionPresenter;
import org.stepic.droid.core.presenters.SectionsPresenter;
import org.stepic.droid.features.deadlines.model.Deadline;
import org.stepic.droid.features.deadlines.model.DeadlinesWrapper;
import org.stepic.droid.features.deadlines.presenters.PersonalDeadlinesPresenter;
import org.stepic.droid.persistence.model.DownloadProgress;
import org.stepik.android.model.Course;
import org.stepik.android.model.Section;
import org.stepic.droid.preferences.SharedPreferenceHelper;
import org.stepic.droid.storage.operations.DatabaseFacade;
import org.stepic.droid.ui.custom.progressbutton.ProgressWheel;
import org.stepic.droid.ui.dialogs.DeleteItemDialogFragment;
import org.stepic.droid.ui.dialogs.VideoQualityDetailedDialog;
import org.stepic.droid.ui.fragments.SectionsFragment;
import org.stepic.droid.ui.listeners.OnItemClickListener;
import org.stepic.droid.ui.util.ViewExtensionsKt;
import org.stepic.droid.util.ColorUtil;
import org.stepic.droid.util.DateTimeHelper;
import org.stepic.droid.util.SectionExtensionsKt;
import org.stepic.droid.viewmodel.ProgressViewModel;
import org.stepic.droid.web.storage.model.StorageRecord;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ThreadPoolExecutor;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import kotlin.Pair;
import kotlin.collections.MapsKt;

import static org.stepic.droid.ui.util.ViewExtensionsKt.changeVisibility;

public class SectionAdapter extends RecyclerView.Adapter<SectionAdapter.GenericViewHolder> {
    private final static String SECTION_TITLE_DELIMETER = ". ";

    private static final int TYPE_SECTION_ITEM = 1;
    private static final int TYPE_CALENDAR_HEADER = 2;
    private static final int TYPE_DEADLINES_HEADER = 3;

    public static final int PRE_SECTION_LIST_DELTA = 2;

    private int defaultHighlightPosition = -1;

    @Inject
    ScreenManager screenManager;

    @Inject
    DatabaseFacade databaseFacade;

    @Inject
    Analytic analytic;

    @Inject
    ThreadPoolExecutor threadPoolExecutor;

    @Inject
    SharedPreferenceHelper sharedPreferenceHelper;


    private final PersonalDeadlinesPresenter personalDeadlinesPresenter;
    private List<Section> sections;
    private AppCompatActivity activity;
    private CalendarPresenter calendarPresenter;
    private Course course;
    private boolean needShowCalendarWidget;
    private boolean needShowDeadlinesBanner;
    private Drawable highlightDrawable;
    @ColorInt
    private int defaultColor;
    private Map<String, ProgressViewModel> progressMap;
    private Fragment fragment;
    private final DownloadingInteractionPresenter downloadingInteractionPresenter;
    private final int durationMillis = 3000;
    private final SectionsPresenter sectionsPresenter;

    private final LongSparseArray<DownloadProgress.Status> downloadProgresses = new LongSparseArray<>();

    @Nullable
    private StorageRecord<DeadlinesWrapper> deadlinesRecord;

    public void setDefaultHighlightPosition(int defaultHighlightPosition) {
        this.defaultHighlightPosition = defaultHighlightPosition;
    }

    public SectionAdapter(List<Section> sections,
                          AppCompatActivity activity,
                          CalendarPresenter calendarPresenter,
                          PersonalDeadlinesPresenter personalDeadlinesPresenter,
                          Map<String, ProgressViewModel> progressMap,
                          Fragment fragment,
                          DownloadingInteractionPresenter downloadingInteractionPresenter,
                          SectionsPresenter sectionsPresenter) {
        this.sections = sections;
        this.activity = activity;
        this.calendarPresenter = calendarPresenter;
        this.personalDeadlinesPresenter = personalDeadlinesPresenter;
        highlightDrawable = ContextCompat.getDrawable(activity, R.drawable.section_background);
        defaultColor = ColorUtil.INSTANCE.getColorArgb(R.color.white, activity);
        this.progressMap = progressMap;
        this.fragment = fragment;
        this.downloadingInteractionPresenter = downloadingInteractionPresenter;
        this.sectionsPresenter = sectionsPresenter;
        App.Companion.component().inject(this);
    }


    @Override
    public GenericViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(activity);
        switch (viewType) {
            case TYPE_CALENDAR_HEADER:
                return new CalendarViewHolder(inflater.inflate(R.layout.export_calendar_view, parent, false));
            case TYPE_DEADLINES_HEADER:
                return new PersonalDeadlinesViewHolder(inflater.inflate(R.layout.header_personal_deadlines, parent, false));
            case TYPE_SECTION_ITEM:
                return new SectionViewHolder(inflater.inflate(R.layout.section_item, parent, false));
            default:
                throw new IllegalStateException("unknown viewType = " + viewType);
        }
    }


    @Override
    public int getItemViewType(int position) {
        switch (position) {
            case 0:
                return TYPE_CALENDAR_HEADER;
            case 1:
                return TYPE_DEADLINES_HEADER;
            default:
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
        onItemDownloadClicked(position);
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public void loadSection(int adapterPosition) {
        int sectionPosition = adapterPosition - PRE_SECTION_LIST_DELTA;
        if (sectionPosition >= 0 && sectionPosition < sections.size()) {
            downloadingInteractionPresenter.checkOnLoading(adapterPosition);
        }
    }

    public void loadAfterDetermineNetworkState(int adapterPosition) {
        int sectionPosition = adapterPosition - PRE_SECTION_LIST_DELTA;
        if (sectionPosition >= 0 && sectionPosition < sections.size()) {
            final Section section = sections.get(sectionPosition);
            analytic.reportEvent(Analytic.Interaction.CLICK_CACHE_SECTION, section.getId() + "");
            analytic.reportAmplitudeEvent(AmplitudeAnalytic.Downloads.STARTED,
                    MapsKt.mapOf(new Pair<String, Object>(AmplitudeAnalytic.Downloads.PARAM_CONTENT, AmplitudeAnalytic.Downloads.Values.SECTION)));
            sectionsPresenter.addDownloadTask(section);
        }
    }

    public void setNeedShowCalendarWidget(boolean needShowCalendarWidget) {
        this.needShowCalendarWidget = needShowCalendarWidget;
    }

    public void setDeadlinesRecord(@Nullable StorageRecord<DeadlinesWrapper> deadlinesRecord) {
        boolean needUpdate = this.deadlinesRecord != deadlinesRecord;
        this.deadlinesRecord = deadlinesRecord;
        if (needUpdate) {
            notifyDataSetChanged();
        }
    }

    @Nullable
    public StorageRecord<DeadlinesWrapper> getDeadlinesRecord() {
        return deadlinesRecord;
    }

    public void setNeedShowDeadlinesBanner(boolean needShowDeadlinesBanner) {
        this.needShowDeadlinesBanner = needShowDeadlinesBanner;
        notifyItemChanged(1);
    }

    public List<Section> getSections() {
        return sections;
    }

    private void onClickStartExam(int adapterPosition) {
        int position = adapterPosition - PRE_SECTION_LIST_DELTA;
        if (position >= 0 && position < sections.size()) {
            analytic.reportEvent(Analytic.Exam.START_EXAM);
            Section section = sections.get(position);
            screenManager.openSyllabusInWeb(activity, section.getCourse());
        }
    }

    public void requestClickDeleteSilence(int position) {
        if (position >= 0 && position < sections.size()) {
            final Section section = sections.get(position);
            sectionsPresenter.removeDownloadTask(section);
        }
    }

    private void onItemDownloadClicked(int adapterPosition) {
        if (sharedPreferenceHelper.isNeedToShowVideoQualityExplanation()) {
            VideoQualityDetailedDialog dialogFragment = VideoQualityDetailedDialog.Companion.newInstance(adapterPosition);
            dialogFragment.setTargetFragment(fragment, VideoQualityDetailedDialog.VIDEO_QUALITY_REQUEST_CODE);
            if (!dialogFragment.isAdded()) {
                FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
                ft.add(dialogFragment, null);
                ft.commitAllowingStateLoss();
            }
        } else {
            loadSection(adapterPosition);
        }
    }

    private void onItemRemoveClicked(int adapterPosition) {
        int sectionPosition = adapterPosition - PRE_SECTION_LIST_DELTA;
        if (sectionPosition >= 0 && sectionPosition < sections.size()) {
            final Section section = sections.get(sectionPosition);
            DownloadProgress.Status downloadProgressStatus = downloadProgresses.get(section.getId());
            if (downloadProgressStatus == DownloadProgress.Status.Cached.INSTANCE) {
                analytic.reportEvent(Analytic.Interaction.CLICK_DELETE_SECTION, section.getId() + "");
                analytic.reportAmplitudeEvent(AmplitudeAnalytic.Downloads.DELETED,
                        MapsKt.mapOf(new Pair<String, Object>(AmplitudeAnalytic.Downloads.PARAM_CONTENT, AmplitudeAnalytic.Downloads.Values.SECTION)));
                DeleteItemDialogFragment dialogFragment = DeleteItemDialogFragment.newInstance(sectionPosition);
                dialogFragment.setTargetFragment(fragment, SectionsFragment.DELETE_POSITION_REQUEST_CODE);
                if (!dialogFragment.isAdded()) {
                    FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
                    ft.add(dialogFragment, null);
                    ft.commitAllowingStateLoss();
                }
            } else {
                //cancel loading
                analytic.reportEvent(Analytic.Interaction.CLICK_CANCEL_SECTION, section.getId() + "");
                analytic.reportAmplitudeEvent(AmplitudeAnalytic.Downloads.CANCELLED,
                        MapsKt.mapOf(new Pair<String, Object>(AmplitudeAnalytic.Downloads.PARAM_CONTENT, AmplitudeAnalytic.Downloads.Values.SECTION)));
                sectionsPresenter.removeDownloadTask(section);
            }
        }
    }

    public void setItemDownloadProgress(DownloadProgress progress) {
        downloadProgresses.put(progress.getId(), progress.getStatus());
        int pos = -1;
        for (int i = 0; i < sections.size(); i++) {
            if (sections.get(i).getId() == progress.getId()) {
                pos = i;
                break;
            }
        }

        if (pos != -1) {
            notifyItemChanged(pos + PRE_SECTION_LIST_DELTA);
        }
    }

    class SectionViewHolder extends GenericViewHolder implements OnItemClickListener {

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

        @BindView(R.id.personal_deadline)
        TextView personalDeadline;

        @BindView(R.id.pre_load_iv)
        View preLoadIV;

        @BindView(R.id.when_load_view)
        ProgressWheel whenLoad;

        @BindView(R.id.after_load_iv)
        View afterLoad;

        @BindView(R.id.load_button)
        ViewGroup loadButton;

        @BindView(R.id.exam_title)
        View examTitle;

        @BindView(R.id.start_exam_button)
        View startExamButton;


        @BindView(R.id.section_text_score)
        TextView textScore;

        @BindView(R.id.section_student_progress_score_bar)
        ProgressBar progressScore;

        @BindView(R.id.loadStateUndefined)
        View loadStateUndefined;

        private long oldSectionId = -1;


        public SectionViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    SectionViewHolder.this.onItemClick(getAdapterPosition());
                }

            });

            preLoadIV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    whenLoad.setProgressPortion(0f, false);
                    onItemDownloadClicked(getAdapterPosition());
                }
            });

            whenLoad.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemRemoveClicked(getAdapterPosition());
                }
            });

            afterLoad.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemRemoveClicked(getAdapterPosition());
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
        public void onItemClick(int adapterPosition) {
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

            startDate.setText(getTimeWithLabelString(R.string.begin_date_section, section.getBeginDate()));
            ViewExtensionsKt.changeVisibility(startDate, section.getBeginDate() != null);

            softDeadline.setText(getTimeWithLabelString(R.string.soft_deadline_section, section.getSoftDeadline()));
            ViewExtensionsKt.changeVisibility(softDeadline, section.getSoftDeadline() != null);

            softDeadline.setText(getTimeWithLabelString(R.string.hard_deadline_section, section.getHardDeadline()));
            ViewExtensionsKt.changeVisibility(hardDeadline, section.getHardDeadline() != null);

            // personal deadlines
            boolean wasDeadlineSet = false;
            if (deadlinesRecord != null) {
                List<Deadline> deadlines = deadlinesRecord.getData().getDeadlines();
                Deadline deadline = null;

                // todo: investigate why it can be null
                if (deadlines != null) for (Deadline d : deadlines) {
                    if (d.getSection() == sectionId) {
                        deadline = d;
                        break;
                    }
                }

                if (deadline != null) {
                    personalDeadline.setText(getTimeWithLabelString(R.string.deadlines_section, deadline.getDeadline()));
                    wasDeadlineSet = true;
                }
            }
            changeVisibility(personalDeadline, wasDeadlineSet);


            if (SectionExtensionsKt.hasUserAccess(section, course)) {

                int strong_text_color = ColorUtil.INSTANCE.getColorArgb(R.color.stepic_regular_text, App.Companion.getAppContext());

                sectionTitle.setTextColor(strong_text_color);
                cv.setFocusable(false);
                cv.setClickable(true);
                cv.setFocusableInTouchMode(false);

                loadButton.setVisibility(View.VISIBLE);

                ViewExtensionsKt.hideAllChildren(loadButton);
                DownloadProgress.Status downloadProgressStatus = downloadProgresses.get(sectionId);
                if (downloadProgressStatus == DownloadProgress.Status.Cached.INSTANCE) {
                    afterLoad.setVisibility(View.VISIBLE);
                } else if (downloadProgressStatus == DownloadProgress.Status.NotCached.INSTANCE) {
                    preLoadIV.setVisibility(View.VISIBLE);
                } else if (downloadProgressStatus instanceof DownloadProgress.Status.InProgress) {
                    whenLoad.setVisibility(View.VISIBLE);
                    whenLoad.setProgressPortion(((DownloadProgress.Status.InProgress) downloadProgressStatus).getProgress(), needAnimation);
                } else {
                    loadStateUndefined.setVisibility(View.VISIBLE);
                }
            } else {
                //Not active section or not enrollment

                loadButton.setVisibility(View.GONE);
                preLoadIV.setVisibility(View.GONE);
                whenLoad.setVisibility(View.INVISIBLE);
                afterLoad.setVisibility(View.GONE);

                int weak_text_color = ColorUtil.INSTANCE.getColorArgb(R.color.stepic_weak_text, App.Companion.getAppContext());
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

        @Contract(pure = true)
        private String getTimeWithLabelString(@StringRes int labelResId, @Nullable Date date) {
            if (date == null) {
                return "";
            } else {
                return itemView.getContext().getString(labelResId,
                        DateTimeHelper.INSTANCE.getPrintableDate(date, DateTimeHelper.DISPLAY_DATETIME_PATTERN, TimeZone.getDefault()));
            }
        }
    }

    class CalendarViewHolder extends GenericViewHolder {

        @BindView(R.id.export_calendar_button)
        View addToCalendarButton;

        @BindView(R.id.not_now_button)
        View notNowButton;


        public CalendarViewHolder(View itemView) {
            super(itemView);
            //calendar view holder is created only once
            addToCalendarButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    analytic.reportEventWithIdName(Analytic.Calendar.USER_CLICK_ADD_WIDGET, course.getId() + "", course.getTitle());
                    calendarPresenter.addDeadlinesToCalendar(SectionAdapter.this.sections, null);
                }
            });

            notNowButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    analytic.reportEventWithIdName(Analytic.Calendar.USER_CLICK_NOT_NOW, course.getId() + "", course.getTitle());
                    calendarPresenter.clickNotNow();
                }
            });

        }

        @Override
        public void setDataOnView(int position) {
            final int height = needShowCalendarWidget ? ViewGroup.LayoutParams.WRAP_CONTENT : 0;
            ViewExtensionsKt.setHeight(itemView, height);
        }

        @Override
        public void clearAnimation() {
            itemView.clearAnimation();
        }
    }

    class PersonalDeadlinesViewHolder extends GenericViewHolder {

        @BindView(R.id.action)
        View actionButton;

        @BindView(R.id.not_now_button)
        View notNowButton;

        public PersonalDeadlinesViewHolder(View itemView) {
            super(itemView);
            actionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    personalDeadlinesPresenter.onClickCreateDeadlines(true);
                }
            });

            notNowButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    personalDeadlinesPresenter.onClickHideDeadlinesBanner();
                }
            });
        }

        @Override
        public void setDataOnView(int position) {
            final int height = needShowDeadlinesBanner ? ViewGroup.LayoutParams.WRAP_CONTENT : 0;
            ViewExtensionsKt.setHeight(itemView, height);
        }

        @Override
        public void clearAnimation() {
            itemView.clearAnimation();
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
        holder.clearAnimation();
    }
}
