package org.stepic.droid.ui.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import org.stepic.droid.ui.adapters.view_hoders.CourseItemViewHolder;
import org.stepic.droid.ui.adapters.view_hoders.CourseViewHolderBase;
import org.stepic.droid.ui.adapters.view_hoders.FooterItemViewHolder;
import org.stepic.droid.util.resolvers.text.TextResolver;

import java.util.List;

import javax.inject.Inject;

public class CoursesAdapter extends RecyclerView.Adapter<CourseViewHolderBase> {

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
    private Boolean isNeedShowFooter;
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
            return new FooterItemViewHolder(view, isNeedShowFooter);
        } else if (itemViewType == viewType) {
            View view = inflater.inflate(R.layout.new_course_item, parent, false);
            return new CourseItemViewHolder(
                    view,
                    contextActivity,
                    type,
                    joinTitle,
                    continueTitle,
                    coursePlaceholder,
                    isContinueExperimentEnabled,
                    courses,
                    droppingPresenter,
                    continueCoursePresenter
            );
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

    public void showLoadingFooter(boolean isNeedShow) {
        isNeedShowFooter = isNeedShow;
        try {
            notifyItemChanged(getItemCount() - 1);
        } catch (IllegalStateException ignored) {
            //if it is already notified
        }
    }

}
