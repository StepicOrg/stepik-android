package org.stepic.droid.util.resolvers;

import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;
import org.stepik.android.model.SearchResult;

import java.util.List;

public interface SearchResolver {
    @NotNull
    long[] getCourseIdsFromSearchResults(@Nullable List<SearchResult> searchResultList);
}
