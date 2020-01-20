package org.stepic.droid.util.resolvers.text;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface TextResolver {
    /**
     * For SIMPLE HTML, which you can set for textview only
     */
    @NotNull
    CharSequence fromHtml(@Nullable String content);
}