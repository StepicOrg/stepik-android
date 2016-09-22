package org.stepic.droid.util.resolvers;

import org.jetbrains.annotations.NotNull;
import org.stepic.droid.model.SearchResult;

import java.util.List;

public interface SearchResolver {
    @NotNull
    long[] getCourseIdsFromSearchResults(List<SearchResult> searchResultList);
}
