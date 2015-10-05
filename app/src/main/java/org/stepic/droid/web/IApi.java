package org.stepic.droid.web;

import org.stepic.droid.model.Course;
import org.stepic.droid.model.User;

import java.util.List;

import retrofit.Call;

public interface IApi {
    Call<AuthenticationStepicResponse> authWithLoginPassword(String login, String password);

    Call<IStepicResponse> signUp(String firstName, String secondName, String email, String password);

    Call<CoursesStepicResponse> getEnrolledCourses(int page);

    Call<CoursesStepicResponse> getFeaturedCourses(int page);

    Call<StepicProfileResponse> getUserProfile();

    Call<List<User>> getUsers(long[] userIds);

    Call<Void> tryJoinCourse(Course course);

    Call<SectionsStepicResponse> getSections (long [] sectionsIds);

}
