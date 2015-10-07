package org.stepic.droid.web;

import org.stepic.droid.model.Course;

import retrofit.Call;

public interface IApi {
    Call<AuthenticationStepicResponse> authWithLoginPassword(String login, String password);

    Call<IStepicResponse> signUp(String firstName, String secondName, String email, String password);

    Call<CoursesStepicResponse> getEnrolledCourses(int page);

    Call<CoursesStepicResponse> getFeaturedCourses(int page);

    Call<StepicProfileResponse> getUserProfile();

    Call<UserStepicResponse> getUsers(long[] userIds);

    Call<Void> tryJoinCourse(Course course);

    Call<SectionsStepicResponse> getSections (long [] sectionsIds);

    Call<UnitStepicResponse> getUnits(long[] units);

}
