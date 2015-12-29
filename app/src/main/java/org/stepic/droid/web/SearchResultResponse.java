package org.stepic.droid.web;

import com.google.gson.annotations.SerializedName;

import org.stepic.droid.model.Meta;
import org.stepic.droid.model.SearchResult;

import java.util.List;

public class SearchResultResponse extends StepicResponseBase {

    @SerializedName("search-results")
    List<SearchResult> searchResultList;

    public SearchResultResponse(Meta meta, List<SearchResult> searchResultList) {
        super(meta);
        this.searchResultList = searchResultList;
    }

    public List<SearchResult> getSearchResultList() {
        return searchResultList;
    }
}
