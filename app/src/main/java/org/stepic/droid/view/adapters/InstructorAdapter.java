package org.stepic.droid.view.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.stepic.droid.R;
import org.stepic.droid.model.User;

import java.util.List;

import butterknife.BindView;
import butterknife.BindDrawable;
import butterknife.ButterKnife;

public class InstructorAdapter extends RecyclerView.Adapter<InstructorAdapter.InstructorViewHolder> {

    private List<User> instructors;
    private Context mContext;

    public InstructorAdapter(List<User> instructors, Context context) {
        this.instructors = instructors;
        mContext = context;
    }


    @Override
    public InstructorViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.instructor_item, null);
        return new InstructorViewHolder(v);
    }

    @Override
    public void onBindViewHolder(InstructorViewHolder holder, int position) {
        User instructor = instructors.get(position);

        String firstLastNameString = instructor.getFirst_name() + " " + instructor.getLast_name();
        holder.firstLastName.setText(firstLastNameString);
        holder.courseShortBio.setText(instructor.getShort_bio());
        Picasso.with(mContext)
                .load(instructor.getAvatar())
                .placeholder(holder.placeholder)
                .error(holder.placeholder)
                .into(holder.instructorIcon);
    }

    @Override
    public int getItemCount() {
        return instructors.size();
    }

    public static class InstructorViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.instructor_icon)
        ImageView instructorIcon;

        @BindView(R.id.first_last_name)
        TextView firstLastName;

        @BindView(R.id.course_short_bio)
        TextView courseShortBio;

        @BindDrawable(R.drawable.placeholder_icon_trnsp)
        Drawable placeholder;

        public InstructorViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }
    }
}
