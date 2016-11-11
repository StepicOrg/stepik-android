package org.stepic.droid.util.resolvers.text;

import android.content.Context;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.stepic.droid.util.resolvers.CoursePropertyResolver;

public interface TextResolver {

    @NotNull
    TextResult resolveCourseProperty(CoursePropertyResolver.Type type, String content, Context context);

    @NotNull
    TextResult resolveStepText(String content);

    /**
     * For SIMPLE HTML, which you can set for textview only
     */
    @NotNull
    CharSequence fromHtml(@Nullable String content);

    @NotNull
    String replaceWhitespaceToBr(@Nullable String answer);

}