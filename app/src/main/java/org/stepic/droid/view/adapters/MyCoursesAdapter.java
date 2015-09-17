package org.stepic.droid.view.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import org.stepic.droid.model.Course;

import java.util.List;

public class MyCoursesAdapter extends ArrayAdapter<Course>{

    public MyCoursesAdapter(Context context, int resource, List<Course> courses) {
        super(context, resource, courses);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
       return null;
    }
}
