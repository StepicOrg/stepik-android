package org.stepic.droid.util;

import android.support.annotation.StringRes;

import org.jetbrains.annotations.NotNull;
import org.stepic.droid.R;
import org.stepic.droid.base.MainApplication;
import org.stepic.droid.model.Course;
import org.stepic.droid.model.CourseProperty;

import java.util.ArrayList;

import javax.inject.Singleton;

@Singleton
public class CoursePropertyResolver {

    @NotNull
    public ArrayList<CourseProperty> getSortedPropertyList(Course course) {
        ArrayList<CourseProperty> result = new ArrayList<>();
        if (course == null) return result;

        addIfNotEmpty(result, course.getSummary(), R.string.about_course);
        addIfNotEmpty(result, course.getWorkload(), R.string.workload);
        addIfNotEmpty(result, course.getCertificate(), R.string.certificate);
        addIfNotEmpty(result, course.getCourse_format(), R.string.course_format);
        addIfNotEmpty(result, course.getTarget_audience(), R.string.target_audience);
        addIfNotEmpty(result, course.getRequirements(), R.string.requirements);
        addIfNotEmpty(result, course.getDescription(), R.string.description);

        return result;
    }

    private void addIfNotEmpty(ArrayList<CourseProperty> result, String propertyValue, @StringRes int titleRes) {
        if (propertyValue != null && !propertyValue.equals("")) {
            String title = MainApplication.getAppContext().getResources().getString(titleRes);
            CourseProperty aboutCourseProperty = new CourseProperty(title, propertyValue);
            result.add(aboutCourseProperty);
        }
    }
}
