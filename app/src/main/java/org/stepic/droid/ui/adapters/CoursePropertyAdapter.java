package org.stepic.droid.ui.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.stepic.droid.R;
import org.stepic.droid.base.MainApplication;
import org.stepic.droid.model.CourseProperty;
import org.stepic.droid.util.resolvers.text.TextResolver;
import org.stepic.droid.util.resolvers.text.TextResult;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CoursePropertyAdapter extends ArrayAdapter<CourseProperty> {
    private LayoutInflater inflater;

    @Inject
    TextResolver textResolver;

    public CoursePropertyAdapter(Context context, List<CourseProperty> coursePropertyList) {
        super(context, 0, coursePropertyList);
        MainApplication.component().inject(this);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
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

        TextResult textResult = textResolver.resolveCourseProperty(courseProperty.getCoursePropertyType(), courseProperty.getText(), getContext());

        if (!textResult.isNeedWebView()) {
            viewHolderItem.coursePropertyValue.setText(textResult.getText());
        } else {
            //todo ONLY if LaTeX is here -> show webview
        }

        return view;
    }

    static class ViewHolderItem {

        @BindView(R.id.course_property_title)
        TextView coursePropertyTitle;

        @BindView(R.id.course_property_text_value)
        TextView coursePropertyValue;

        ViewHolderItem(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
