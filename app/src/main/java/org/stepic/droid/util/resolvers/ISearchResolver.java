package org.stepic.droid.util.resolvers;

import android.support.annotation.NonNull;

import org.jetbrains.annotations.NotNull;
import org.stepic.droid.model.SearchResult;

import java.util.List;

public interface ISearchResolver {
    @NonNull
    @NotNull
    long[] getCourseIdsFromSearchResults(List<SearchResult> searchResultList);
}
