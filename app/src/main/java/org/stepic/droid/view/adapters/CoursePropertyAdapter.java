package org.stepic.droid.view.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.stepic.droid.R;
import org.stepic.droid.model.CourseProperty;
import org.stepic.droid.util.HtmlHelper;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class CoursePropertyAdapter extends ArrayAdapter<CourseProperty> {
    private LayoutInflater mInflater;

    public CoursePropertyAdapter(Context context,
                                 List<CourseProperty> coursePropertyList) {
        super(context, 0, coursePropertyList);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final CourseProperty courseProperty = getItem(position);

        View view = convertView;
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolderItem viewHolderItem = null;
        if (view == null) {
            view = mInflater.inflate(R.layout.course_property_item, null);
            viewHolderItem = new ViewHolderItem(view);
            view.setTag(viewHolderItem);
        } else {
            viewHolderItem = (ViewHolderItem) convertView.getTag();
        }

        viewHolderItem.coursePropertyTitle.setText(HtmlHelper.fromHtml(courseProperty.getTitle()).toString());
        viewHolderItem.getCoursePropertyValue.setText(HtmlHelper.fromHtml(courseProperty.getText()).toString());

        return view;
    }

    static class ViewHolderItem {

        @Bind(R.id.course_property_title)
        TextView coursePropertyTitle;

        @Bind(R.id.course_property_text_value)
        TextView getCoursePropertyValue;

        public ViewHolderItem(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
