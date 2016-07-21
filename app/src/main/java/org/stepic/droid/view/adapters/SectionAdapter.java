package org.stepic.droid.view.adapters;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.stepic.droid.R;
import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.base.MainApplication;
import org.stepic.droid.core.CalendarPresenter;
import org.stepic.droid.core.IScreenManager;
import org.stepic.droid.core.IShell;
import org.stepic.droid.model.Course;
import org.stepic.droid.model.Section;
import org.stepic.droid.store.CleanManager;
import org.stepic.droid.store.IDownloadManager;
import org.stepic.droid.store.operations.DatabaseFacade;
import org.stepic.droid.util.AppConstants;
import org.stepic.droid.util.ColorUtil;
import org.stepic.droid.view.dialogs.ExplainExternalStoragePermissionDialog;
import org.stepic.droid.view.listeners.OnClickLoadListener;
import org.stepic.droid.view.listeners.StepicOnClickItemListener;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;

public class SectionAdapter extends RecyclerView.Adapter<SectionAdapter.GenericViewHolder> implements OnClickLoadListener {
    private final static String SECTION_TITLE_DELIMETER = ". ";

    public static final int TYPE_SECTION_ITEM = 1;
    public static final int TYPE_TITLE = 2;

    public static final int SECTION_LIST_DELTA = 1;

    @Inject
    IScreenManager mScreenManager;
    @Inject
    IDownloadManager mDownloadManager;

    @Inject
    DatabaseFacade mDatabaseFacade;

    @Inject
    IShell mShell;

    @Inject
    CleanManager mCleaner;

    @Inject
    Analytic analytic;

    @Inject
    CalendarPresenter calendarPresenter;

    private List<Section> mSections;
    private Context mContext;
    private AppCompatActivity mActivity;
    private Course course;
    private boolean needShowCalendarWidget;

    public SectionAdapter(List<Section> sections, Context mContext, AppCompatActivity activity) {
        this.mSections = sections;
        this.mContext = mContext;
        mActivity = activity;

        MainApplication.component().inject(this);
    }


    @Override
    public GenericViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_SECTION_ITEM) {
            View v = LayoutInflater.from(mContext).inflate(R.layout.section_item, parent, false);
            return new SectionViewHolder(v);
        } else if (viewType == TYPE_TITLE) {
            View v = LayoutInflater.from(mContext).inflate(R.layout.export_calendar_view, parent, false);
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
        return mSections.size() + SECTION_LIST_DELTA;
    }


    public void requestClickLoad(int position) {
        onClickLoad(position);
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    @Override
    public void onClickLoad(int adapterPosition) {
        int sectionPosition = adapterPosition - SECTION_LIST_DELTA;
        if (sectionPosition >= 0 && sectionPosition < mSections.size()) {
            Section section = mSections.get(sectionPosition);

            int permissionCheck = ContextCompat.checkSelfPermission(MainApplication.getAppContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                mShell.getSharedPreferenceHelper().storeTempPosition(adapterPosition);
                if (ActivityCompat.shouldShowRequestPermissionRationale(mActivity,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.

                    DialogFragment dialog = ExplainExternalStoragePermissionDialog.newInstance();
                    if (!dialog.isAdded()) {
                        dialog.show(mActivity.getSupportFragmentManager(), null);
                    }

                } else {
                    // No explanation needed, we can request the permission.

                    ActivityCompat.requestPermissions(mActivity,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            AppConstants.REQUEST_EXTERNAL_STORAGE);

                }
                return;
            }

            if (section.is_cached()) {
                analytic.reportEvent(Analytic.Interaction.CLICK_DELETE_SECTION, section.getId() + "");
                mCleaner.removeSection(section);
                section.set_loading(false);
                section.set_cached(false);
                mDatabaseFacade.updateOnlyCachedLoadingSection(section);
                notifyItemChanged(adapterPosition);
            } else {
                if (section.is_loading()) {
                    analytic.reportEvent(Analytic.Interaction.CLICK_CANCEL_SECTION, section.getId() + "");
                    mScreenManager.showDownload(mContext);
                } else {
                    analytic.reportEvent(Analytic.Interaction.CLICK_CACHE_SECTION, section.getId() + "");
                    section.set_cached(false);
                    section.set_loading(true);
                    mDatabaseFacade.updateOnlyCachedLoadingSection(section);
                    mDownloadManager.addSection(section);
                    notifyItemChanged(adapterPosition);
                }
            }
        }
    }

    public void setNeedShowCalendarWidget(boolean needShowCalendarWidget) {
        if (this.needShowCalendarWidget != needShowCalendarWidget){
            this.needShowCalendarWidget = needShowCalendarWidget;
            notifyItemChanged(0);
        }
    }

    public boolean isNeedShowCalendarWidget() {
        return needShowCalendarWidget;
    }

    class SectionViewHolder extends GenericViewHolder implements StepicOnClickItemListener {

        @BindView(R.id.cv)
        View cv;

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
        View whenLoad;

        @BindView(R.id.after_load_iv)
        View afterLoad;

        @BindView(R.id.load_button)
        View mLoadButton;


        public SectionViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    SectionViewHolder.this.onClick(getAdapterPosition());
                }

            });

            mLoadButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickLoad(getAdapterPosition());
                }
            });
        }


        @Override
        public void onClick(int adapterPosition) {
            int itemPosition = adapterPosition - SECTION_LIST_DELTA;
            if (itemPosition >= 0 && itemPosition < mSections.size()) {
                mScreenManager.showUnitsForSection(mContext, mSections.get(itemPosition));
            }
        }

        @Override
        public void setDataOnView(int positionInAdapter) {
            // the 0 index always for calendar, we make its GONE, if calendar is not needed.
            int position = positionInAdapter - SECTION_LIST_DELTA;
            Section section = mSections.get(position);

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

            if (section.is_active() && course.getEnrollment() > 0) {

                int strong_text_color = ColorUtil.INSTANCE.getColorArgb(R.color.stepic_regular_text, MainApplication.getAppContext());

                sectionTitle.setTextColor(strong_text_color);
                cv.setFocusable(false);
                cv.setClickable(true);
                cv.setFocusableInTouchMode(false);

                mLoadButton.setVisibility(View.VISIBLE);
                if (section.is_cached()) {

                    // FIXME: 05.11.15 Delete course from cache. Set CLICK LISTENER.
                    //cached

                    preLoadIV.setVisibility(View.GONE);
                    whenLoad.setVisibility(View.INVISIBLE);
                    afterLoad.setVisibility(View.VISIBLE); //can

                } else {
                    if (section.is_loading()) {

                        preLoadIV.setVisibility(View.GONE);
                        whenLoad.setVisibility(View.VISIBLE);
                        afterLoad.setVisibility(View.GONE);

                        //todo: add cancel of downloading
                    } else {
                        //not cached not loading
                        preLoadIV.setVisibility(View.VISIBLE);
                        whenLoad.setVisibility(View.INVISIBLE);
                        afterLoad.setVisibility(View.GONE);
                    }

                }
            } else {
                //Not active section or not enrollment

                mLoadButton.setVisibility(View.GONE);
                preLoadIV.setVisibility(View.GONE);
                whenLoad.setVisibility(View.INVISIBLE);
                afterLoad.setVisibility(View.GONE);

                int weak_text_color = ColorUtil.INSTANCE.getColorArgb(R.color.stepic_weak_text, MainApplication.getAppContext());
                sectionTitle.setTextColor(weak_text_color);
                cv.setFocusable(false);
                cv.setClickable(false);
                cv.setFocusableInTouchMode(false);
            }
        }
    }


    class CalendarViewHolder extends GenericViewHolder {

        View rootView;

        @BindView(R.id.export_calendar_button)
        View addToCalendarButton;

        public CalendarViewHolder(View itemView) {
            super(itemView);
            rootView = itemView;
            addToCalendarButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    calendarPresenter.addDeadlinesToCalendar(SectionAdapter.this.mSections);
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

        private boolean shouldBeHidden() {
            return !SectionAdapter.this.needShowCalendarWidget;
        }

        private void hide() {
            ViewGroup.LayoutParams layoutParams = rootView.getLayoutParams();
            layoutParams.height = 0;
            rootView.setLayoutParams(layoutParams);
        }

        private void show() {
            ViewGroup.LayoutParams layoutParams = rootView.getLayoutParams();
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            rootView.setLayoutParams(layoutParams);
        }
    }

    abstract class GenericViewHolder extends RecyclerView.ViewHolder {

        public GenericViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public abstract void setDataOnView(int position);
    }
}
