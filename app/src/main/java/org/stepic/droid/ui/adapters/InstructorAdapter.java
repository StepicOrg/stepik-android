package org.stepic.droid.ui.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.stepic.droid.R;
import org.stepic.droid.model.User;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class InstructorAdapter extends RecyclerView.Adapter<InstructorAdapter.InstructorViewHolder> {

    private List<User> instructors;
    private Context context;
    private Drawable placeholder;

    public InstructorAdapter(List<User> instructors, Context context) {
        this.instructors = instructors;
        this.context = context;
        placeholder = ContextCompat.getDrawable(context, R.drawable.placeholder_icon_trnsp);
    }


    @Override
    public InstructorViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.instructor_item, null);
        return new InstructorViewHolder(v);
    }

    @Override
    public void onBindViewHolder(InstructorViewHolder holder, int position) {
        User instructor = instructors.get(position);

        String firstLastNameString = instructor.getFirst_name() + " " + instructor.getLast_name();
        holder.firstLastName.setText(firstLastNameString);
        holder.courseShortBio.setText(instructor.getShort_bio());
        Glide.with(context)
                .load(instructor.getAvatar())
                .asBitmap()
                .placeholder(placeholder)
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

        public InstructorViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
