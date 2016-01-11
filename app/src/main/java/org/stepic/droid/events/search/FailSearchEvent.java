package org.stepic.droid.events.search;

public class FailSearchEvent {
    String query;

    public FailSearchEvent(String query) {
        this.query = query;
    }

    public String getQuery() {
        return query;
    }
}
