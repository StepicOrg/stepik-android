package org.stepic.droid.events.joining_course;

import retrofit.Response;

public class FailJoinEvent {
    private final Response<Void> response;

    public Response<Void> getResponse() {
        return response;
    }

    public FailJoinEvent() {
        response = null;

    }

    public FailJoinEvent(Response<Void> response) {
        this.response = response;
    }
}
