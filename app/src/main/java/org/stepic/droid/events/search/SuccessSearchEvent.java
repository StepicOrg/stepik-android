package org.stepic.droid.events.search;

public class SuccessSearchEvent {
    String query;

    public SuccessSearchEvent(String query) {
        this.query = query;
    }

    public String getQuery() {
        return query;
    }
}
