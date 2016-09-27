package org.stepic.droid.util.resolvers.text;

import android.content.Context;

import org.jetbrains.annotations.NotNull;
import org.stepic.droid.util.resolvers.CoursePropertyResolver;

public interface TextResolver {

    @NotNull
    TextResult resolveCourseProperty(CoursePropertyResolver.Type type, String content, Context context);
}