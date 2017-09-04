package org.stepic.droid.ui.adapters.view_hoders;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.PopupMenu;
import android.view.HapticFeedbackConstants;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.stepic.droid.R;
import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.configuration.Config;
import org.stepic.droid.core.ScreenManager;
import org.stepic.droid.core.presenters.ContinueCoursePresenter;
import org.stepic.droid.core.presenters.DroppingPresenter;
import org.stepic.droid.model.Course;
import org.stepic.droid.storage.operations.Table;
import org.stepic.droid.util.ContextMenuCourseUtil;
import org.stepic.droid.util.StepikLogicHelper;

import java.util.List;
import java.util.Locale;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;

public class CourseItemViewHolder extends CourseViewHolderBase {

    @BindView(R.id.courseItemName)
    TextView courseName;

    @BindView(R.id.courseItemImage)
    ImageView courseIcon;

    @BindView(R.id.courseWidgetButton)
    TextView courseWidgetButton;

    @BindView(R.id.courseItemLearnersCount)
    TextView learnersCount;

    @BindView(R.id.courseItemMore)
    View courseItemMore;

    @ColorInt
    @BindColor(R.color.new_accent_color)
    int continueColor;

    @ColorInt
    @BindColor(R.color.join_text_color)
    int joinColor;


    private final String joinTitle;
    private final String continueTitle;
    private final DroppingPresenter droppingPresenter;
    private Drawable coursePlaceholder;
    private Table type;
    private final View itemView;
    private boolean isContinueExperimentEnabled;
    private Config config;
    private List<Course> courses;
    BitmapImageViewTarget imageViewTarget;
    private final Activity contextActivity;
    private final Analytic analytic;
    private final ContinueCoursePresenter continueCoursePresenter;
    private final ScreenManager screenManager;

    public CourseItemViewHolder(String joinTitle,
                                String continueTitle,
                                DroppingPresenter droppingCoursePresenter,
                                Drawable coursePlaceholder,
                                Table type,
                                final View itemView,
                                boolean isContinueExperimentEnabled,
                                Config config,
                                List<Course> courses,
                                final Activity contextActivity,
                                Analytic analytic,
                                ContinueCoursePresenter continueCoursePresenter,
                                ScreenManager screenManager) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        this.joinTitle = joinTitle;
        this.continueTitle = continueTitle;
        this.droppingPresenter = droppingCoursePresenter;
        this.coursePlaceholder = coursePlaceholder;
        this.type = type;
        this.itemView = itemView;
        this.isContinueExperimentEnabled = isContinueExperimentEnabled;
        this.config = config;
        this.courses = courses;
        imageViewTarget = new BitmapImageViewTarget(courseIcon) {
            @Override
            protected void setResource(Bitmap resource) {
                RoundedBitmapDrawable circularBitmapDrawable =
                        RoundedBitmapDrawableFactory.create(contextActivity.getResources(), resource);
                circularBitmapDrawable.setCornerRadius(contextActivity.getResources().getDimension(R.dimen.course_image_radius));
                courseIcon.setImageDrawable(circularBitmapDrawable);
            }
        };
        this.contextActivity = contextActivity;
        this.analytic = analytic;
        this.continueCoursePresenter = continueCoursePresenter;
        this.screenManager = screenManager;
        courseWidgetButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int adapterPosition = getAdapterPosition();
                Course course = getCourseSafety(adapterPosition);
                if (course != null) {
                    onClickWidgetButton(course, isEnrolled(course));
                }
            }
        });
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickCourse(getAdapterPosition());
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

        courseItemMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Course course = getCourseSafety(getAdapterPosition());
                if (course != null) {
                    showMore(v, course);
                }
            }
        });
    }


    private void showMore(View view, @NotNull final Course course) {
        PopupMenu morePopupMenu = new PopupMenu(contextActivity, view);
        morePopupMenu.inflate(ContextMenuCourseUtil.INSTANCE.getMenuResource(course));

        morePopupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_item_info:
                        screenManager.showCourseDescription(contextActivity, course);
                        return true;
                    case R.id.menu_item_unroll:
                        droppingPresenter.dropCourse(course);
                        return true;
                    default:
                        return false;
                }
            }
        });

        morePopupMenu.show();
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


    private void onClickWidgetButton(@NotNull Course course, boolean enrolled) {
        if (enrolled) {
            analytic.reportEvent(Analytic.Interaction.CLICK_CONTINUE_COURSE);
            analytic.reportEvent(isContinueExperimentEnabled ? Analytic.ContinueExperiment.CONTINUE_NEW : Analytic.ContinueExperiment.CONTINUE_OLD);
            continueCoursePresenter.continueCourse(course); //provide position?
        } else {
            screenManager.showCourseDescription(contextActivity, course);
        }
    }

    @Nullable
    private Course getCourseSafety(int adapterPosition) {
        if (adapterPosition >= courses.size() || adapterPosition < 0) {
            return null;
        } else {
            return courses.get(adapterPosition);
        }
    }

    @Override
    public void setDataOnView(int position) {
        final Course course = courses.get(position);

        courseName.setText(course.getTitle());
        Glide
                .with(contextActivity)
                .load(StepikLogicHelper.getPathForCourseOrEmpty(course, config))
                .asBitmap()
                .placeholder(coursePlaceholder)
                .fitCenter()
                .into(imageViewTarget);

        if (course.getLearnersCount() > 0) {
            learnersCount.setText(String.format(Locale.getDefault(), "%d", course.getLearnersCount()));
            learnersCount.setVisibility(View.VISIBLE);
        } else {
            learnersCount.setVisibility(View.GONE);
        }


        if (isEnrolled(course)) {
            showContinueButton();
        } else {
            showJoinButton();
        }

        if (type == Table.enrolled) {
            courseItemMore.setVisibility(View.VISIBLE);
        } else {
            courseItemMore.setVisibility(View.GONE);
        }
    }

    private boolean isEnrolled(Course course) {
        return course.getEnrollment() != 0 && course.isActive() && course.getLastStepId() != null;
    }

    private void showJoinButton() {
        showButton(joinTitle, joinColor, R.drawable.course_widget_join_background);
    }

    private void showContinueButton() {
        showButton(continueTitle, continueColor, R.drawable.course_widget_continue_background);
    }

    private void showButton(String title, @ColorInt int textColor, @DrawableRes int background) {
        courseWidgetButton.setText(title);
        courseWidgetButton.setTextColor(textColor);
        courseWidgetButton.setBackgroundResource(background);
    }

}