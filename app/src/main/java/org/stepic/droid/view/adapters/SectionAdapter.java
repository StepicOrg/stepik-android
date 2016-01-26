package org.stepic.droid.view.adapters;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.yandex.metrica.YandexMetrica;

import org.stepic.droid.R;
import org.stepic.droid.base.MainApplication;
import org.stepic.droid.core.IScreenManager;
import org.stepic.droid.model.Section;
import org.stepic.droid.store.CleanManager;
import org.stepic.droid.store.IDownloadManager;
import org.stepic.droid.store.operations.DatabaseManager;
import org.stepic.droid.util.AppConstants;
import org.stepic.droid.util.JsonHelper;
import org.stepic.droid.view.dialogs.ExplainPermissionDialog;
import org.stepic.droid.view.listeners.OnClickLoadListener;
import org.stepic.droid.view.listeners.StepicOnClickItemListener;

import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;

public class SectionAdapter extends RecyclerView.Adapter<SectionAdapter.SectionViewHolder> implements StepicOnClickItemListener, OnClickLoadListener {
    private final static String SECTION_TITLE_DELIMETER = ". ";

    @Inject
    IScreenManager mScreenManager;
    @Inject
    IDownloadManager mDownloadManager;

    @Inject
    DatabaseManager mDatabaseManager;

    @Inject
    CleanManager mCleaner;

    private List<Section> mSections;
    private Context mContext;
    private Activity mActivity;

    public SectionAdapter(List<Section> sections, Context mContext, Activity activity) {
        this.mSections = sections;
        this.mContext = mContext;
        mActivity = activity;

        MainApplication.component().inject(this);
    }

    @Override
    public SectionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.section_item, parent, false);
        return new SectionViewHolder(v, this, this);
    }

    @Override
    public void onBindViewHolder(SectionViewHolder holder, int position) {
        Section section = mSections.get(position);

        String title = section.getTitle();
        int positionOfSection = section.getPosition();
        title = positionOfSection + SECTION_TITLE_DELIMETER + title;
        holder.sectionTitle.setText(title);


        String formattedBeginDate = section.getFormattedBeginDate();
        if (formattedBeginDate.equals("")) {
            holder.startDate.setText("");
            holder.startDate.setVisibility(View.GONE);
        } else {
            holder.startDate.setText(holder.beginDateString + " " + formattedBeginDate);
            holder.startDate.setVisibility(View.VISIBLE);
        }

        String formattedSoftDeadline = section.getFormattedSoftDeadline();
        if (formattedSoftDeadline.equals("")) {
            holder.softDeadline.setText("");
            holder.softDeadline.setVisibility(View.GONE);
        } else {
            holder.softDeadline.setText(holder.softDeadlineString + " " + formattedSoftDeadline);
            holder.softDeadline.setVisibility(View.VISIBLE);
        }

        String formattedHardDeadline = section.getFormattedHardDeadline();
        if (formattedHardDeadline.equals("")) {
            holder.hardDeadline.setText("");
            holder.hardDeadline.setVisibility(View.GONE);
        } else {
            holder.hardDeadline.setText(holder.hardDeadlineString + " " + formattedHardDeadline);
            holder.hardDeadline.setVisibility(View.VISIBLE);
        }

        if (section.is_active()) {

            int strong_text_color;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                strong_text_color = MainApplication.getAppContext().getResources().getColor(R.color.stepic_regular_text, MainApplication.getAppContext().getTheme());
            } else {
                strong_text_color = MainApplication.getAppContext().getResources().getColor(R.color.stepic_regular_text);
            }

            holder.sectionTitle.setTextColor(strong_text_color);
            holder.cv.setFocusable(false);
            holder.cv.setClickable(true);
            holder.cv.setFocusableInTouchMode(false);

            holder.mLoadButton.setVisibility(View.VISIBLE);
            if (section.is_cached()) {

                // FIXME: 05.11.15 Delete course from cache. Set CLICK LISTENER.
                //cached

                holder.preLoadIV.setVisibility(View.GONE);
                holder.whenLoad.setVisibility(View.INVISIBLE);
                holder.afterLoad.setVisibility(View.VISIBLE); //can

            } else {
                if (section.is_loading()) {

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
        } else {
            //Not active section


            holder.mLoadButton.setVisibility(View.GONE);
            holder.preLoadIV.setVisibility(View.GONE);
            holder.whenLoad.setVisibility(View.INVISIBLE);
            holder.afterLoad.setVisibility(View.GONE);

            int weak_text_color;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                weak_text_color = MainApplication.getAppContext().getColor(R.color.stepic_weak_text);
            } else {
                weak_text_color = MainApplication.getAppContext().getResources().getColor(R.color.stepic_weak_text);
            }
            holder.sectionTitle.setTextColor(weak_text_color);
            holder.cv.setFocusable(false);
            holder.cv.setClickable(false);
            holder.cv.setFocusableInTouchMode(false);
        }
    }

    @Override
    public int getItemCount() {
        return mSections.size();
    }

    @Override
    public void onClick(int itemPosition) {
        if (itemPosition >= 0 && itemPosition < mSections.size()) {

            mScreenManager.showUnitsForSection(mContext, mSections.get(itemPosition));
        }
    }

    @Override
    public void onClickLoad(int position) {
        if (position >= 0 && position < mSections.size()) {
            Section section = mSections.get(position);

            int permissionCheck = ContextCompat.checkSelfPermission(MainApplication.getAppContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(mActivity,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.

                    ExplainPermissionDialog dialog = new ExplainPermissionDialog();
                    dialog.show(mActivity.getFragmentManager(), null);

                } else {

                    // No explanation needed, we can request the permission.

                    ActivityCompat.requestPermissions(mActivity,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            AppConstants.REQUEST_WIFI);

                    // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                }
                return;
            }

            if (section.is_cached()) {
                YandexMetrica.reportEvent(AppConstants.METRICA_CLICK_DELETE_SECTION, JsonHelper.toJson(section));
                mCleaner.removeSection(section);
                section.setIs_loading(false);
                section.setIs_cached(false);
                mDatabaseManager.updateOnlyCachedLoadingSection(section);
                notifyItemChanged(position);
            } else {
                if (section.is_loading()) {
                    // TODO: 11.11.15 cancel downloading
                } else {
                    YandexMetrica.reportEvent(AppConstants.METRICA_CLICK_CACHE_SECTION, JsonHelper.toJson(section));
                    section.setIs_cached(false);
                    section.setIs_loading(true);
                    mDatabaseManager.updateOnlyCachedLoadingSection(section);
                    mDownloadManager.addSection(section);
                    notifyItemChanged(position);
                }
            }
        }
    }

    public static class SectionViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.cv)
        View cv;

        @Bind(R.id.section_title)
        TextView sectionTitle;

        @Bind(R.id.start_date)
        TextView startDate;

        @Bind(R.id.soft_deadline)
        TextView softDeadline;

        @Bind(R.id.hard_deadline)
        TextView hardDeadline;

        @BindString(R.string.hard_deadline_section)
        String hardDeadlineString;
        @BindString(R.string.soft_deadline_section)
        String softDeadlineString;
        @BindString(R.string.begin_date_section)
        String beginDateString;

        @Bind(R.id.pre_load_iv)
        View preLoadIV;

        @Bind(R.id.when_load_view)
        View whenLoad;

        @Bind(R.id.after_load_iv)
        View afterLoad;

        @Bind(R.id.load_button)
        View mLoadButton;


        public SectionViewHolder(View itemView, final StepicOnClickItemListener listener, final OnClickLoadListener loadSectionListener) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    listener.onClick(getAdapterPosition());
                }
            });

            mLoadButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    loadSectionListener.onClickLoad(getAdapterPosition());
                }
            });
        }
    }
}
