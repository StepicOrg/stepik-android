package org.stepic.droid.view.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.stepic.droid.R;
import org.stepic.droid.base.MainApplication;
import org.stepic.droid.configuration.IConfig;
import org.stepic.droid.core.IShell;
import org.stepic.droid.model.Course;
import org.stepic.droid.util.HtmlHelper;

import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.BindDrawable;
import butterknife.ButterKnife;

public class MyCoursesAdapter extends ArrayAdapter<Course> {

    @Inject
    IShell mShell;

    @Inject
    IConfig mConfig;

    private Context mContext;
    private LayoutInflater mInflater;

    public MyCoursesAdapter(Context context, List<Course> courses) {
        super(context, 0, courses);
        mContext = context;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        MainApplication.component(mContext).inject(this);

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
        Picasso.with(mContext).load(mConfig.getBaseUrl() + course.getCover()).
                placeholder(viewHolderItem.placeholder).into(viewHolderItem.courseIcon);
        viewHolderItem.courseDateInterval.setText(course.getDateOfCourse());

        viewHolderItem.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (course.getEnrollment() != 0) {
                    mShell.getScreenProvider().showCourseDescriptionForEnrolled(mContext, course);
                } else {
                    mShell.getScreenProvider().showCourseDescriptionForNotEnrolled(mContext, course);
                }
            }
        });
//
//        if (position == getCount() - 1) {
//            viewHolderItem.divider.setVisibility(View.GONE);
//        } else {
//            viewHolderItem.divider.setVisibility(View.VISIBLE);
//        }

        return view;
    }


    static class ViewHolderItem {
        @Bind(R.id.course_name)
        TextView courseName;

        @Bind(R.id.first_last_name)
        TextView courseSummary;

        @Bind(R.id.course_icon)
        ImageView courseIcon;

        @Bind(R.id.course_date_interval)
        TextView courseDateInterval;

        @Bind(R.id.cv)
        View cardView;

        @BindDrawable(R.drawable.stepic_logo_black_and_white)
        Drawable placeholder;

        public ViewHolderItem(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
