package org.stepic.droid.view.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.stepic.droid.R;
import org.stepic.droid.model.Section;

import java.util.List;

public class SectionAdapter extends RecyclerView.Adapter<SectionAdapter.SectionViewHolder> {

    private List<Section> sections;
    private Context mContext;

    public SectionAdapter(List<Section> sections, Context mContext) {
        this.sections = sections;
        this.mContext = mContext;
    }


    @Override
    public SectionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.section_item, null);
        return new SectionViewHolder(v);
    }

    @Override
    public void onBindViewHolder(SectionViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return sections.size();
    }

    public static class SectionViewHolder extends RecyclerView.ViewHolder {

        public SectionViewHolder(View itemView) {
            super(itemView);
        }
    }
}
