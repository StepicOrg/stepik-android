package org.stepic.droid.util.resolvers;

import android.support.annotation.Nullable;

import org.jetbrains.annotations.NotNull;
import org.stepic.droid.model.SearchResult;

import java.util.List;

public interface SearchResolver {
    @NotNull
    long[] getCourseIdsFromSearchResults(@Nullable List<SearchResult> searchResultList);
}
