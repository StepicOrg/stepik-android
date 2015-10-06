package org.stepic.droid.view.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.stepic.droid.R;
import org.stepic.droid.model.Section;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class SectionAdapter extends RecyclerView.Adapter<SectionAdapter.SectionViewHolder> {
private final static String SECTION_TITLE_DELIMETER = ". ";

    private List<Section> sections;
    private Context mContext;

    public SectionAdapter(List<Section> sections, Context mContext) {
        this.sections = sections;
        this.mContext = mContext;
    }


    @Override
    public SectionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.section_item, parent, false);
        return new SectionViewHolder(v);
    }

    @Override
    public void onBindViewHolder(SectionViewHolder holder, int position) {
        Section section = sections.get(position);

        String title = section.getTitle();
        int positionOfSection = section.getPosition();
        title = positionOfSection + SECTION_TITLE_DELIMETER + title;
        holder.sectionTitle.setText(title);
        holder.startDate.setText(section.getFormattedBeginDate());
        holder.softDeadline.setText(section.getFormattedSoftDeadline());
        holder.hardDeadline.setText(section.getFormattedHardDeadline());
    }

    @Override
    public int getItemCount() {
        return sections.size();
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

        public SectionViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
