package org.stepic.droid.events.search;

import org.stepic.droid.web.SearchResultResponse;

import retrofit.Response;

public class SuccessSearchEvent {
    String query;
    private Response<SearchResultResponse> response;

    public SuccessSearchEvent(String query, Response<SearchResultResponse> response) {
        this.query = query;
        this.response = response;
    }

    public Response<SearchResultResponse> getResponse() {
        return response;
    }

    public String getQuery() {
        return query;
    }
}
