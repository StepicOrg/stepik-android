package org.stepic.droid.web;

import org.stepic.droid.model.Course;

import java.util.List;

public interface IApi {
    IStepicResponse authWithLoginPassword (String login, String password);

    IStepicResponse signUp (String firstName, String secondName, String email, String password);

    List<Course> getEnrolledCourses ();

    List<Course> getFeaturedCourses ();

}
