package org.stepic.droid.util.resolvers;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface TextResolver {
    enum TextType {
        plainText,
        htmlText,
        htmlStepTextWithCss
    }

    @NotNull
    TextResult prepareTextForView(@Nullable String content, TextType type);
}
