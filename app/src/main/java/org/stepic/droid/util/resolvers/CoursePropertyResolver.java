package org.stepic.droid.util.resolvers;

import android.support.annotation.StringRes;

import org.jetbrains.annotations.NotNull;
import org.stepic.droid.R;
import org.stepic.droid.base.App;
import org.stepic.droid.di.AppSingleton;
import org.stepik.android.model.Course;
import org.stepic.droid.model.CourseProperty;

import java.util.ArrayList;

import javax.inject.Inject;

@AppSingleton
public class CoursePropertyResolver {

    @Inject
    public CoursePropertyResolver() {
    }

    public enum Type {
        summary(R.string.about_course),
        workload(R.string.workload),
        certificate(R.string.certificate),
        courseFormat(R.string.course_format),
        targetAudience(R.string.target_audience),
        requirements(R.string.requirements),
        description(R.string.description);

        private int titleRes;

        Type(@StringRes int titleRes) {
            this.titleRes = titleRes;
        }

        @StringRes
        public int getTitleRes() {
            return this.titleRes;
        }
    }

    @NotNull
    public ArrayList<CourseProperty> getSortedPropertyList(Course course) {
        ArrayList<CourseProperty> result = new ArrayList<>();
        if (course == null) return result;


        addIfNotEmpty(result, course.getSummary(), Type.summary);
        addIfNotEmpty(result, course.getWorkload(), Type.workload);
        addIfNotEmpty(result, course.getCertificate(), Type.certificate);
        addIfNotEmpty(result, course.getCourseFormat(), Type.courseFormat);
        addIfNotEmpty(result, course.getTargetAudience(), Type.targetAudience);
        addIfNotEmpty(result, course.getRequirements(), Type.requirements);
        addIfNotEmpty(result, course.getDescription(), Type.description);

        return result;
    }

    private void addIfNotEmpty(ArrayList<CourseProperty> result, String propertyValue, Type type) {
        if (propertyValue != null && !propertyValue.equals("")) {
            int titleRes = type.getTitleRes();
            String title = App.Companion.getAppContext().getResources().getString(titleRes);
            CourseProperty aboutCourseProperty = new CourseProperty(title, propertyValue, type);
            result.add(aboutCourseProperty);
        }
    }
}
