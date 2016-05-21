package org.stepic.droid.view.adapters;

import android.content.Context;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.facebook.common.util.UriUtil;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.DraweeView;

import org.jetbrains.annotations.Nullable;
import org.stepic.droid.R;
import org.stepic.droid.base.MainApplication;
import org.stepic.droid.configuration.IConfig;
import org.stepic.droid.core.IShell;
import org.stepic.droid.model.Course;
import org.stepic.droid.store.CleanManager;
import org.stepic.droid.store.IDownloadManager;
import org.stepic.droid.store.operations.DatabaseFacade;
import org.stepic.droid.util.HtmlHelper;

import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MyCoursesAdapter extends ArrayAdapter<Course> {

    @Inject
    IShell mShell;

    @Inject
    IConfig mConfig;

    @Inject
    DatabaseFacade mDatabase;

    @Inject
    IDownloadManager mDownloadManager;

    @Inject
    CleanManager mCleaner;

    @Nullable
    private final DatabaseFacade.Table type;
    private LayoutInflater mInflater;

    public MyCoursesAdapter(Fragment fragment, List<Course> courses, @Nullable DatabaseFacade.Table type) {
        super(fragment.getActivity(), 0, courses);
        Fragment mFragment = fragment;
        this.type = type;
        mInflater = (LayoutInflater) mFragment.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        MainApplication.component().inject(this);

    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final Course course = getItem(position);

        View view = convertView;
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolderItem viewHolderItem = null;
        if (view == null) {
            view = mInflater.inflate(R.layout.course_item, null);
            viewHolderItem = new ViewHolderItem(view);
            view.setTag(viewHolderItem);
        } else {
            viewHolderItem = (ViewHolderItem) convertView.getTag();
        }
        viewHolderItem.courseName.setText(course.getTitle());
        viewHolderItem.courseSummary.setText(HtmlHelper.fromHtml(course.getSummary()));
        if (course.getCover() != null) {
            DraweeController controller = Fresco.newDraweeControllerBuilder()
                    .setUri(mConfig.getBaseUrl() + course.getCover())
                    .setAutoPlayAnimations(true)
                    .build();
            viewHolderItem.courseIcon.setController(controller);
        } else {
            //for empty cover:
            Uri uri = new Uri.Builder()
                    .scheme(UriUtil.LOCAL_RESOURCE_SCHEME) // "res"
                    .path(String.valueOf(R.drawable.ic_course_placeholder))
                    .build();

            DraweeController controller = Fresco.newDraweeControllerBuilder()
                    .setUri(uri)
                    .setAutoPlayAnimations(true)
                    .build();
            viewHolderItem.courseIcon.setController(controller);
        }
        viewHolderItem.courseDateInterval.setText(course.getDateOfCourse());

        return view;
    }

    static class ViewHolderItem {

        @Bind(R.id.course_name)
        TextView courseName;

        @Bind(R.id.course_info)
        TextView courseSummary;

        @Bind(R.id.video_icon)
        DraweeView courseIcon;

        @Bind(R.id.course_date_interval)
        TextView courseDateInterval;

        @Bind(R.id.cv)
        View cardView;

        public ViewHolderItem(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
