package org.stepic.droid.ui.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;

import org.jetbrains.annotations.Nullable;
import org.stepic.droid.R;
import org.stepic.droid.base.MainApplication;
import org.stepic.droid.configuration.IConfig;
import org.stepic.droid.core.IShell;
import org.stepic.droid.model.Course;
import org.stepic.droid.store.operations.Table;
import org.stepic.droid.util.StepicLogicHelper;
import org.stepic.droid.util.resolvers.text.TextResolver;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CoursesAdapter extends RecyclerView.Adapter<CoursesAdapter.CourseViewHolderBase> {


    @Inject
    IConfig config;

    @Inject
    TextResolver textResolver;

    @Inject
    IShell shell;

    private Drawable coursePlaceholder;

    private LayoutInflater inflater;

    private Activity contextActivity;
    private final List<Course> courses;
    private int footerViewType = 1;
    private int itemViewType = 2;
    private int NUMBER_OF_EXTRA_ITEMS = 1;
    private boolean isNeedShowFooter;

    public CoursesAdapter(Fragment fragment, List<Course> courses, @Nullable Table type) {
        contextActivity = fragment.getActivity();
        this.courses = courses;
        inflater = (LayoutInflater) contextActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        MainApplication.component().inject(this);
        coursePlaceholder = ContextCompat.getDrawable(fragment.getContext(), R.drawable.ic_course_placeholder);
    }

    @Override
    public CourseViewHolderBase onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == footerViewType) {
            View view = inflater.inflate(R.layout.loading_view, parent, false);
            return new FooterViewHolderItem(view);
        } else if (itemViewType == viewType) {
            View view = inflater.inflate(R.layout.course_item, parent, false);
            return new CourseViewHolderItem(view);
        } else {
            throw new IllegalStateException("Not valid item type");
        }
    }

    @Override
    public void onBindViewHolder(CourseViewHolderBase holder, int position) {
        holder.setDataOnView(position);
    }


    @Override
    public int getItemViewType(int position) {
        if (position == getItemCount() - 1) {
            return footerViewType;
        } else {
            return itemViewType;
        }
    }

    @Override
    public int getItemCount() {
        return courses.size() + NUMBER_OF_EXTRA_ITEMS;
    }

    private void onClickContinue(int position) {
        Toast.makeText(contextActivity, "you click continue at " + position, Toast.LENGTH_SHORT).show(); //// FIXME: 09.12.16 open smth
    }

    private void onClickCourse(int position) {
        if (position >= courses.size() || position < 0) return;
        Course course = courses.get(position);
        if (course.getEnrollment() != 0) {
            shell.getScreenProvider().showSections(contextActivity, course);
        } else {
            shell.getScreenProvider().showCourseDescription(contextActivity, course);
        }
    }


    abstract static class CourseViewHolderBase extends RecyclerView.ViewHolder {

        public CourseViewHolderBase(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        abstract void setDataOnView(int position);
    }

    class CourseViewHolderItem extends CourseViewHolderBase {

        @BindView(R.id.course_name)
        TextView courseName;

        @BindView(R.id.course_info)
        TextView courseSummary;

        @BindView(R.id.course_icon)
        ImageView courseIcon;

        @BindView(R.id.continue_button)
        View continueButton;

        GlideDrawableImageViewTarget imageViewTarget;

        CourseViewHolderItem(View itemView) {
            super(itemView);
            imageViewTarget = new GlideDrawableImageViewTarget(courseIcon);
            continueButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    CoursesAdapter.this.onClickContinue(getAdapterPosition());
                }
            });
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CoursesAdapter.this.onClickCourse(getAdapterPosition());
                }
            });
        }

        @Override
        void setDataOnView(int position) {
            final Course course = courses.get(position);

            courseName.setText(course.getTitle());
            courseSummary.setText(textResolver.fromHtml(course.getSummary()).toString());
            Glide
                    .with(contextActivity)
                    .load(StepicLogicHelper.getPathForCourseOrEmpty(course, config))
                    .placeholder(coursePlaceholder)
                    .centerCrop()
                    .into(imageViewTarget);
        }
    }

    class FooterViewHolderItem extends CourseViewHolderBase {

        @BindView(R.id.loading_root)
        View loadingRoot;

        FooterViewHolderItem(View itemView) {
            super(itemView);
        }

        @Override
        void setDataOnView(int position) {
            loadingRoot.setVisibility(isNeedShowFooter ? View.VISIBLE : View.GONE);
        }
    }

    public void showLoadingFooter(boolean isNeedShow) {
        isNeedShowFooter = isNeedShow;
        notifyItemChanged(getItemCount() - 1);
    }

}
