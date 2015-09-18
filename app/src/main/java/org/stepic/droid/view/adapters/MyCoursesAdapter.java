package org.stepic.droid.view.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.stepic.droid.R;
import org.stepic.droid.model.Course;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MyCoursesAdapter extends ArrayAdapter<Course> {

    private List<Course> mCourses;
    private Context mContext;

    private LayoutInflater mInflater;

    public MyCoursesAdapter(Context context, List<Course> courses) {
        super(context, 0, courses);
        mCourses = courses;
        mContext = context;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Course course = getItem(position);

        View view = convertView;
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolderItem viewHolderItem = null;
        if (view == null) {
            view = mInflater.inflate(R.layout.course_item, null);
            viewHolderItem = new ViewHolderItem(view);
            view.setTag(viewHolderItem);

//            view = LayoutInflater.from(getContext()).inflate(R.layout.course_item, parent, false);
        } else {
            viewHolderItem = (ViewHolderItem) convertView.getTag();
        }
        viewHolderItem.courseName.setText(course.getTitle());

        return view;
    }


    static class ViewHolderItem {
        @Bind(R.id.course_name)
        TextView courseName;

        public ViewHolderItem(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
