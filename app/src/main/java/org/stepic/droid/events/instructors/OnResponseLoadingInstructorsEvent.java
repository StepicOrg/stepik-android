package org.stepic.droid.events.instructors;

import org.stepic.droid.model.Course;
import org.stepic.droid.web.UserStepicResponse;

import retrofit2.Response;

public class OnResponseLoadingInstructorsEvent extends InstructorsBaseEvent {
    private final Response<UserStepicResponse> response;

    public OnResponseLoadingInstructorsEvent(Course course, Response<UserStepicResponse> response) {
        super(course);
        this.response = response;
    }

    public Response<UserStepicResponse> getResponse() {
        return response;
    }

}
