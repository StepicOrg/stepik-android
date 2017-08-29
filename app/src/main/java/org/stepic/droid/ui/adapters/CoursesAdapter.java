package org.stepic.droid.ui.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.stepic.droid.R;
import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.base.App;
import org.stepic.droid.configuration.Config;
import org.stepic.droid.configuration.RemoteConfig;
import org.stepic.droid.core.ScreenManager;
import org.stepic.droid.core.presenters.ContinueCoursePresenter;
import org.stepic.droid.model.Course;
import org.stepic.droid.storage.operations.Table;
import org.stepic.droid.util.StepikLogicHelper;
import org.stepic.droid.util.resolvers.text.TextResolver;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CoursesAdapter extends RecyclerView.Adapter<CoursesAdapter.CourseViewHolderBase> {

    @Inject
    Config config;

    @Inject
    TextResolver textResolver;

    @Inject
    ScreenManager screenManager;

    @Inject
    Analytic analytic;

    @Inject
    FirebaseRemoteConfig firebaseRemoteConfig;

    private Drawable coursePlaceholder;

    private LayoutInflater inflater;

    private Activity contextActivity;
    private final List<Course> courses;
    private final ContinueCoursePresenter continueCoursePresenter;
    private int footerViewType = 1;
    private int itemViewType = 2;
    private int NUMBER_OF_EXTRA_ITEMS = 1;
    private boolean isNeedShowFooter;
    private final String continueTitle;
    private final boolean isContinueExperimentEnabled;

    public CoursesAdapter(Fragment fragment, List<Course> courses, @Nullable Table type, @NotNull ContinueCoursePresenter continueCoursePresenter) {
        contextActivity = fragment.getActivity();
        this.courses = courses;
        this.continueCoursePresenter = continueCoursePresenter;
        inflater = (LayoutInflater) contextActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        App.Companion.component().inject(this);
        coursePlaceholder = ContextCompat.getDrawable(fragment.getContext(), R.drawable.general_placeholder);
        isContinueExperimentEnabled = firebaseRemoteConfig.getBoolean(RemoteConfig.INSTANCE.getContinueCourseExperimentEnabledKey());
        if (isContinueExperimentEnabled) {
            continueTitle = contextActivity.getString(R.string.continue_course_title_experimental);
        } else {
            continueTitle = contextActivity.getString(R.string.continue_course_title);
        }
    }

    @Override
    public CourseViewHolderBase onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == footerViewType) {
            View view = inflater.inflate(R.layout.loading_view, parent, false);
            return new FooterViewHolderItem(view);
        } else if (itemViewType == viewType) {
            View view = inflater.inflate(R.layout.new_course_item, parent, false);
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
        if (position >= 0 && position < courses.size()) {
            analytic.reportEvent(Analytic.Interaction.CLICK_CONTINUE_COURSE);
            analytic.reportEvent(isContinueExperimentEnabled ? Analytic.ContinueExperiment.CONTINUE_NEW : Analytic.ContinueExperiment.CONTINUE_OLD);
            Course course = courses.get(position);
            continueCoursePresenter.continueCourse(course); //provide position?
        }
    }

    private void onClickCourse(int position) {
        if (position >= courses.size() || position < 0) return;
        analytic.reportEvent(Analytic.Interaction.CLICK_COURSE);
        Course course = courses.get(position);
        if (course.getEnrollment() != 0) {
            analytic.reportEvent(isContinueExperimentEnabled ? Analytic.ContinueExperiment.COURSE_NEW : Analytic.ContinueExperiment.COURSE_OLD);
            screenManager.showSections(contextActivity, course);
        } else {
            screenManager.showCourseDescription(contextActivity, course);
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

        @BindView(R.id.courseItemName)
        TextView courseName;

        @BindView(R.id.courseItemImage)
        ImageView courseIcon;

        @BindView(R.id.continueButton)
        Button continueButton;

        GlideDrawableImageViewTarget imageViewTarget;

        CourseViewHolderItem(final View itemView) {
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

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                    itemView.showContextMenu();
                    return true;
                }
            });
            continueButton.setText(continueTitle);
        }

        @Override
        void setDataOnView(int position) {
            final Course course = courses.get(position);

            courseName.setText(course.getTitle());
            Glide
                    .with(contextActivity)
                    .load(StepikLogicHelper.getPathForCourseOrEmpty(course, config))
                    .placeholder(coursePlaceholder)
                    .centerCrop()
                    .into(imageViewTarget);


            //// FIXME: 29.08.17 change logic to GONE-> "Join course"
            if (course.getEnrollment() != 0 && course.isActive() && course.getLastStepId() != null) {
                continueButton.setVisibility(View.VISIBLE);
            } else {
                continueButton.setVisibility(View.GONE);
            }
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
        try {
            notifyItemChanged(getItemCount() - 1);
        } catch (IllegalStateException ignored) {
            //if it is already notified
        }
    }

}
