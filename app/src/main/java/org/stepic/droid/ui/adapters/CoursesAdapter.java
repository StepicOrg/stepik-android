package org.stepic.droid.ui.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import org.jetbrains.annotations.NotNull;
import org.stepic.droid.R;
import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.base.App;
import org.stepic.droid.configuration.Config;
import org.stepic.droid.configuration.RemoteConfig;
import org.stepic.droid.core.ScreenManager;
import org.stepic.droid.core.presenters.ContinueCoursePresenter;
import org.stepic.droid.core.presenters.DroppingPresenter;
import org.stepic.droid.model.Course;
import org.stepic.droid.model.CoursesCarouselColorType;
import org.stepic.droid.model.CoursesDescriptionContainer;
import org.stepic.droid.ui.adapters.viewhoders.CourseItemViewHolder;
import org.stepic.droid.ui.adapters.viewhoders.FooterItemViewHolder;
import org.stepic.droid.ui.adapters.viewhoders.HeaderItemViewHolder;
import org.stepic.droid.util.resolvers.text.TextResolver;

import java.util.List;

import javax.inject.Inject;

public class CoursesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

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

    private FragmentActivity contextActivity;
    private final List<Course> courses;
    private final ContinueCoursePresenter continueCoursePresenter;
    @NotNull
    private final DroppingPresenter droppingPresenter;

    private CoursesDescriptionContainer descriptionContainer;

    private final static int HEADER_VIEW_TYPE = 3;
    private final static int ITEM_VIEW_TYPE = 2;
    private final static int FOOTER_VIEW_TYPE = 1;

    private int NUMBER_OF_PRE_ITEMS = 0;
    private final int NUMBER_OF_POST_ITEMS;
    private boolean isNeedShowFooter;
    private final String continueTitle;
    private final String joinTitle;
    private final boolean isContinueExperimentEnabled;
    private final boolean showMore;
    private final CoursesCarouselColorType colorType;

    public CoursesAdapter(FragmentActivity activity,
                          List<Course> courses,
                          @NotNull ContinueCoursePresenter continueCoursePresenter,
                          @NotNull DroppingPresenter droppingPresenter,
                          boolean withPagination,
                          boolean showMore,
                          CoursesCarouselColorType colorType
    ) {
        this.showMore = showMore;
        this.colorType = colorType;
        if (withPagination) {
            NUMBER_OF_POST_ITEMS = 1;
        } else {
            NUMBER_OF_POST_ITEMS = 0;
        }
        contextActivity = activity;
        this.courses = courses;
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
        isNeedShowFooter = false;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == HEADER_VIEW_TYPE) {
            View view = inflater.inflate(R.layout.course_collection_header_view, parent, false);
            ((RecyclerView.LayoutParams) view.getLayoutParams()).setMargins(
                    -(int) contextActivity.getResources().getDimension(R.dimen.course_list_side_padding),
                    -(int) contextActivity.getResources().getDimension(R.dimen.course_list_between_items_padding),
                    -(int) contextActivity.getResources().getDimension(R.dimen.course_list_side_right_padding),
                    0); // todo refactor layouts
            return new HeaderItemViewHolder(view);
        } else if (viewType == FOOTER_VIEW_TYPE) {
            View view = inflater.inflate(R.layout.loading_view, parent, false);
            return new FooterItemViewHolder(view);
        } else if (ITEM_VIEW_TYPE == viewType) {
            View view = inflater.inflate(R.layout.new_course_item, parent, false);
            return new CourseItemViewHolder(
                    view,
                    contextActivity,
                    showMore,
                    joinTitle,
                    continueTitle,
                    coursePlaceholder,
                    isContinueExperimentEnabled,
                    courses,
                    droppingPresenter,
                    continueCoursePresenter,
                    colorType
            );
        } else {
            throw new IllegalStateException("Not valid item type");
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case HEADER_VIEW_TYPE: {
                HeaderItemViewHolder headerItemViewHolder = (HeaderItemViewHolder) holder;
                headerItemViewHolder.bindData(descriptionContainer);
                break;
            }
            case ITEM_VIEW_TYPE: {
                CourseItemViewHolder courseItemViewHolder = (CourseItemViewHolder) holder;
                courseItemViewHolder.setDataOnView(position - NUMBER_OF_PRE_ITEMS);
                break;
            }
            case FOOTER_VIEW_TYPE: {
                FooterItemViewHolder footerItemViewHolder = (FooterItemViewHolder) holder;
                footerItemViewHolder.setLoaderVisibiluty(isNeedShowFooter);
                break;
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position < NUMBER_OF_PRE_ITEMS) {
            return HEADER_VIEW_TYPE;
        } else if (position == getItemCount() - NUMBER_OF_POST_ITEMS) {
            return FOOTER_VIEW_TYPE;
        } else {
            return ITEM_VIEW_TYPE;
        }
    }

    @Override
    public int getItemCount() {
        return NUMBER_OF_PRE_ITEMS + courses.size() + NUMBER_OF_POST_ITEMS;
    }

    public void setDescriptionContainer(CoursesDescriptionContainer descriptionContainer) {
        if (this.descriptionContainer == null && descriptionContainer != null) {
            NUMBER_OF_PRE_ITEMS++;
            this.descriptionContainer = descriptionContainer;
            notifyItemInserted(0);
        } else if (this.descriptionContainer != null && descriptionContainer == null) {
            NUMBER_OF_PRE_ITEMS--;
            this.descriptionContainer = null;
            notifyItemRemoved(0);
        } else if (this.descriptionContainer != null) {
//            this.descriptionContainer.setDescription(descriptionContainer.getDescription());
//            this.descriptionContainer.setColors(descriptionContainer.getColors());
            notifyItemChanged(0);
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
