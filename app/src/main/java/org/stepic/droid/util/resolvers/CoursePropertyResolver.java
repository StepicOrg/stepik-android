package org.stepic.droid.util.resolvers;

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

    public enum CoursePropertyType {
        summary(R.string.about_course),
        workload(R.string.workload),
        certificate(R.string.certificate),
        courseFormat(R.string.course_format),
        targetAudience(R.string.target_audience),
        requirements(R.string.requirements),
        description(R.string.description);

        private int titleRes;

        CoursePropertyType(@StringRes int titleRes) {
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


        addIfNotEmpty(result, course.getSummary(), CoursePropertyType.summary);
        addIfNotEmpty(result, course.getWorkload(), CoursePropertyType.workload);
        addIfNotEmpty(result, course.getCertificate(), CoursePropertyType.certificate);
        addIfNotEmpty(result, course.getCourse_format(), CoursePropertyType.courseFormat);
        addIfNotEmpty(result, course.getTarget_audience(), CoursePropertyType.targetAudience);
        addIfNotEmpty(result, course.getRequirements(), CoursePropertyType.requirements);
        addIfNotEmpty(result, course.getDescription(), CoursePropertyType.description);

        return result;
    }

    private void addIfNotEmpty(ArrayList<CourseProperty> result, String propertyValue, CoursePropertyType type) {
        if (propertyValue != null && !propertyValue.equals("")) {
            int titleRes = type.getTitleRes();
            String title = MainApplication.getAppContext().getResources().getString(titleRes);
            CourseProperty aboutCourseProperty = new CourseProperty(title, propertyValue, type);
            result.add(aboutCourseProperty);
        }
    }
}
