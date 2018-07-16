package org.stepic.droid.ui.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PictureDrawable;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.GenericRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.caverock.androidsvg.SVG;

import org.stepic.droid.R;
import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.base.App;
import org.stepic.droid.core.ScreenManager;
import org.stepik.android.model.User;
import org.stepic.droid.util.AppConstants;
import org.stepic.droid.util.svg.GlideSvgRequestFactory;

import java.io.InputStream;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class InstructorAdapter extends RecyclerView.Adapter<InstructorAdapter.InstructorViewHolder> {

    private List<User> instructors;
    private Activity activity;
    private Drawable placeholder;

    @Inject
    public Analytic analytic;

    @Inject
    public ScreenManager screenManager;

    public InstructorAdapter(List<User> instructors, Activity activity) {
        App.Companion.component().inject(this);
        this.instructors = instructors;
        this.activity = activity;
        placeholder = ContextCompat.getDrawable(activity, R.drawable.placeholder_icon_trnsp);
    }

    private void onClickInstructor(int position) {
        if (position >= 0 && position < instructors.size()) {
            analytic.reportEvent(Analytic.Profile.CLICK_INSTRUCTOR);
            User user = instructors.get(position);
            screenManager.openProfile(activity, user.getId());
        }
    }

    @Override
    public InstructorViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(activity).inflate(R.layout.instructor_item, parent, false);
        return new InstructorViewHolder(v);
    }

    @Override
    public void onBindViewHolder(InstructorViewHolder holder, int position) {
        User instructor = instructors.get(position);

        holder.firstLastName.setText(instructor.getFullName());
        holder.courseShortBio.setText(instructor.getShortBio());
        String svgAvatarPath = instructor.getAvatar();

        String userAvatar = instructor.getAvatar();
        if (userAvatar == null) {
            userAvatar = "";
        }
        if (userAvatar.endsWith(AppConstants.SVG_EXTENSION)) {
            Uri uri = Uri.parse(svgAvatarPath);
            holder.svgRequestBuilder
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .load(uri)
                    .into(holder.instructorIcon);
        } else {
            Glide.with(activity)
                    .load(userAvatar)
                    .asBitmap()
                    .placeholder(placeholder)
                    .into(holder.instructorIcon);
        }
    }


    @Override
    public int getItemCount() {
        return instructors.size();
    }

    public class InstructorViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.instructor_icon)
        ImageView instructorIcon;

        @BindView(R.id.first_last_name)
        TextView firstLastName;

        @BindView(R.id.course_short_bio)
        TextView courseShortBio;

        final GenericRequestBuilder<Uri, InputStream, SVG, PictureDrawable> svgRequestBuilder;

        public InstructorViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    InstructorAdapter.this.onClickInstructor(getAdapterPosition());
                }
            });

            final Context context = itemView.getContext();
            svgRequestBuilder = GlideSvgRequestFactory.create(context, InstructorAdapter.this.placeholder);
        }
    }
}
