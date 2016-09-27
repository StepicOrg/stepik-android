package org.stepic.droid.ui.adapters;

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

import butterknife.BindView;
import butterknife.ButterKnife;

public class CoursePropertyAdapter extends ArrayAdapter<CourseProperty> {
    private LayoutInflater inflater;

    public CoursePropertyAdapter(Context context, List<CourseProperty> coursePropertyList) {
        super(context, 0, coursePropertyList);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final CourseProperty courseProperty = getItem(position);

        View view = convertView;
        ViewHolderItem viewHolderItem;
        if (view == null) {
            view = inflater.inflate(R.layout.course_property_item, null);
            viewHolderItem = new ViewHolderItem(view);
            view.setTag(viewHolderItem);
        } else {
            viewHolderItem = (ViewHolderItem) convertView.getTag();
        }

        viewHolderItem.coursePropertyTitle.setText(courseProperty.getTitle());

        viewHolderItem.getCoursePropertyValue.setText(HtmlHelper.fromHtml(courseProperty.getText()).toString());

        return view;
    }

    static class ViewHolderItem {

        @BindView(R.id.course_property_title)
        TextView coursePropertyTitle;

        @BindView(R.id.course_property_text_value)
        TextView getCoursePropertyValue;

        public ViewHolderItem(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
