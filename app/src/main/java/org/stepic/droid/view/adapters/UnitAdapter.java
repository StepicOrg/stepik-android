package org.stepic.droid.view.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.stepic.droid.R;
import org.stepic.droid.base.MainApplication;
import org.stepic.droid.model.Section;
import org.stepic.droid.model.Unit;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class UnitAdapter extends RecyclerView.Adapter<UnitAdapter.UnitViewHolder> implements View.OnClickListener {

    private final static String DELIMITER = ".";

    private final Context mContext;
    private final Section mParentSection;
    private final List<Unit> mUnitList;
    private RecyclerView mRecyclerView;

    public UnitAdapter(Context context, Section parentSection, List<Unit> unitList) {

        this.mContext = context;
        this.mParentSection = parentSection;
        this.mUnitList = unitList;

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
        v.setOnClickListener(this);
        return new UnitViewHolder(v);
    }

    @Override
    public void onBindViewHolder(UnitViewHolder holder, int position) {
        Unit unit = mUnitList.get(position);

        StringBuilder titleBuilder = new StringBuilder();
        titleBuilder.append(mParentSection.getPosition());
        titleBuilder.append(DELIMITER);
        titleBuilder.append(unit.getPosition());
        titleBuilder.append(" ");
        titleBuilder.append("Title of lessons in unit context");

        holder.unitTitle.setText(titleBuilder.toString());
    }

    @Override
    public int getItemCount() {
        return mUnitList.size();
    }

    @Override
    public void onClick(View v) {
        //do sth
    }

    public static class UnitViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.cv)
        View cv;

        @Bind(R.id.unit_layout)
        View layout;

        @Bind(R.id.unit_title)
        TextView unitTitle;


        public UnitViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
