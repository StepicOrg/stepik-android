package org.stepic.droid.util.resolvers;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TextResolverImpl implements TextResolver {
    @NotNull
    @Override
    public TextResult prepareTextForView(@Nullable String content, TextType type) {
        if (content == null) {
            return new TextResult("", false);
        } else if (type == TextType.plainText) {
            return new TextResult(content.trim(), false);
        }

        //default
        return new TextResult("", false);
    }
}
