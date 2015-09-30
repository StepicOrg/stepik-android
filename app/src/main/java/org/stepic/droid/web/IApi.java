package org.stepic.droid.web;

import org.stepic.droid.model.Course;
import org.stepic.droid.model.Profile;
import org.stepic.droid.model.User;

import java.util.List;

public interface IApi {
    IStepicResponse authWithLoginPassword (String login, String password);

    IStepicResponse signUp (String firstName, String secondName, String email, String password);

    CoursesStepicResponse getEnrolledCourses (int page);

    CoursesStepicResponse getFeaturedCourses (int page);

    Profile getUserProfile();

    List<User> getUsers (long [] userIds);

}
