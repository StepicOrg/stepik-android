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
import org.stepic.droid.view.listeners.StepicOnClickItemListener;

import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;

public class SectionAdapter extends RecyclerView.Adapter<SectionAdapter.SectionViewHolder> implements StepicOnClickItemListener {
    private final static String SECTION_TITLE_DELIMETER = ". ";

    @Inject
    IScreenManager mScreenManager;

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
        return new SectionViewHolder(v, this);
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


        public SectionViewHolder(View itemView, final StepicOnClickItemListener listener) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    listener.onClick(getAdapterPosition());
                }
            });
        }
    }
}
