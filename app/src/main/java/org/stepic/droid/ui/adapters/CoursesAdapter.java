package org.stepic.droid.ui.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.v4.app.Fragment;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
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
import org.stepic.droid.core.presenters.DroppingPresenter;
import org.stepic.droid.model.Course;
import org.stepic.droid.storage.operations.Table;
import org.stepic.droid.util.ContextMenuCourseUtil;
import org.stepic.droid.util.StepikLogicHelper;
import org.stepic.droid.util.resolvers.text.TextResolver;

import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.BindColor;
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
    @Nullable
    private final Table type;
    private final ContinueCoursePresenter continueCoursePresenter;
    @NotNull
    private final DroppingPresenter droppingPresenter;

    private int footerViewType = 1;
    private int itemViewType = 2;
    private int NUMBER_OF_EXTRA_ITEMS = 1;
    private boolean isNeedShowFooter;
    private final String continueTitle;
    private final String joinTitle;
    private final boolean isContinueExperimentEnabled;

    public CoursesAdapter(Fragment fragment, List<Course> courses, @Nullable Table type, @NotNull ContinueCoursePresenter continueCoursePresenter, @NotNull DroppingPresenter droppingPresenter) {
        contextActivity = fragment.getActivity();
        this.courses = courses;
        this.type = type;
        this.continueCoursePresenter = continueCoursePresenter;
        this.droppingPresenter = droppingPresenter;
        inflater = (LayoutInflater) contextActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        App.Companion.component().inject(this);

        Bitmap coursePlaceholderBitmap = BitmapFactory.decodeResource(contextActivity.getResources(), R.drawable.general_placeholder);
        RoundedBitmapDrawable circularBitmapDrawable =
                RoundedBitmapDrawableFactory.create(contextActivity.getResources(), coursePlaceholderBitmap);
        circularBitmapDrawable.setCornerRadius(contextActivity.getResources().getDimension(R.dimen.course_image_radius));
        coursePlaceholder = circularBitmapDrawable;

        isContinueExperimentEnabled = firebaseRemoteConfig.getBoolean(RemoteConfig.INSTANCE.getContinueCourseExperimentEnabledKey());
        if (isContinueExperimentEnabled) {
            continueTitle = contextActivity.getString(R.string.continue_course_title_experimental);
        } else {
            continueTitle = contextActivity.getString(R.string.continue_course_title);
        }
        joinTitle = contextActivity.getString(R.string.course_item_join);
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

    private void onClickWidgetButton(@NotNull Course course, boolean enrolled) {
        if (enrolled) {
            analytic.reportEvent(Analytic.Interaction.CLICK_CONTINUE_COURSE);
            analytic.reportEvent(isContinueExperimentEnabled ? Analytic.ContinueExperiment.CONTINUE_NEW : Analytic.ContinueExperiment.CONTINUE_OLD);
            continueCoursePresenter.continueCourse(course); //provide position?
        } else {
            screenManager.showCourseDescription(contextActivity, course);
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

        @BindView(R.id.courseWidgetButton)
        TextView courseWidgetButton;

        @BindView(R.id.learnersCountText)
        TextView learnersCount;

        @BindView(R.id.learnersCountContainer)
        View learnerCountContainer;

        @BindView(R.id.courseItemMore)
        View courseItemMore;

        @ColorInt
        @BindColor(R.color.new_accent_color)
        int continueColor;

        @ColorInt
        @BindColor(R.color.join_text_color)
        int joinColor;

        BitmapImageViewTarget imageViewTarget;

        CourseViewHolderItem(final View itemView) {
            super(itemView);
            imageViewTarget = new BitmapImageViewTarget(courseIcon) {
                @Override
                protected void setResource(Bitmap resource) {
                    RoundedBitmapDrawable circularBitmapDrawable =
                            RoundedBitmapDrawableFactory.create(contextActivity.getResources(), resource);
                    circularBitmapDrawable.setCornerRadius(contextActivity.getResources().getDimension(R.dimen.course_image_radius));
                    courseIcon.setImageDrawable(circularBitmapDrawable);
                }
            };
            courseWidgetButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    int adapterPosition = getAdapterPosition();
                    Course course = getCourseSafety(adapterPosition);
                    if (course != null) {
                        CoursesAdapter.this.onClickWidgetButton(course, isEnrolled(course));
                    }
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

            courseItemMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Course course = getCourseSafety(getAdapterPosition());
                    if (course != null) {
                        CoursesAdapter.this.showMore(v, course);
                    }
                }
            });
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
        void setDataOnView(int position) {
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
                learnerCountContainer.setVisibility(View.VISIBLE);
            } else {
                learnerCountContainer.setVisibility(View.GONE);
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
