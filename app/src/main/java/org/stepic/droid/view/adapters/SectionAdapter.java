package org.stepic.droid.view.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.stepic.droid.R;
import org.stepic.droid.base.MainApplication;
import org.stepic.droid.core.IScreenManager;
import org.stepic.droid.model.Section;
import org.stepic.droid.store.IDownloadManager;
import org.stepic.droid.store.operations.DatabaseManager;
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

    private List<Section> mSections;
    private Context mContext;

    public SectionAdapter(List<Section> sections, Context mContext) {
        this.mSections = sections;
        this.mContext = mContext;

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
        if (formattedBeginDate == "") {
            holder.startDate.setText("");
            holder.startDate.setVisibility(View.GONE);
        } else {
            holder.startDate.setText(holder.beginDateString + " " + formattedBeginDate);
            holder.startDate.setVisibility(View.VISIBLE);
        }

        String formattedSoftDeadline = section.getFormattedSoftDeadline();
        if (formattedSoftDeadline == "") {
            holder.softDeadline.setText("");
            holder.softDeadline.setVisibility(View.GONE);
        } else {
            holder.softDeadline.setText(holder.softDeadlineString + " " + formattedSoftDeadline);
            holder.softDeadline.setVisibility(View.VISIBLE);
        }

        String formattedHardDeadline = section.getFormattedHardDeadline();
        if (formattedHardDeadline == "") {
            holder.hardDeadline.setText("");
            holder.hardDeadline.setVisibility(View.GONE);
        } else {
            holder.hardDeadline.setText(holder.hardDeadlineString + " " + formattedHardDeadline);
            holder.hardDeadline.setVisibility(View.VISIBLE);
        }

        if (section.is_cached()) {

            // FIXME: 05.11.15 Delete course from cache. Set CLICK LISTENER.
            //cached

            holder.preLoadIV.setVisibility(View.GONE);
            holder.whenLoad.setVisibility(View.GONE);
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
                holder.whenLoad.setVisibility(View.GONE);
                holder.afterLoad.setVisibility(View.GONE);
            }

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
    public void onClickLoad(int itemPosition) {
        if (itemPosition >= 0 && itemPosition < mSections.size()) {
            Section section = mSections.get(itemPosition);

            if (section.is_cached()) {
                // TODO: 11.11.15 delete section
            } else {
                if (section.is_loading()) {
                    // TODO: 11.11.15 cancel downloading
                } else {
                    mDownloadManager.addSection(section);
                    section.setIs_cached(false);
                    section.setIs_loading(true);
                    notifyDataSetChanged();
                }
            }
        }
    }

    public static class SectionViewHolder extends RecyclerView.ViewHolder {

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
