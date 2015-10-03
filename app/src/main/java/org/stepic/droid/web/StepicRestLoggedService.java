package org.stepic.droid.web;

import org.stepic.droid.model.EnrollmentWrapper;
import org.stepic.droid.model.Profile;
import org.stepic.droid.model.User;

import java.util.List;

import retrofit.Call;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.Headers;
import retrofit.http.POST;
import retrofit.http.Query;

public interface StepicRestLoggedService {
    @GET("api/sections")
    Call<SectionsStepicResponse> getSections(@Query("ids[]") long[] sectionIds);

    @Headers({"Content-Type : application/json"})
    @POST("api/enrollments")
    Call<Void> joinCourse(@Body EnrollmentWrapper enrollmentCourse);

    @GET("api/users")
    Call<List<User>> getUsers(@Query("ids[]") long[] userIds);

    @GET("api/stepics/1")
    Call<Profile> getUserProfile();

    @GET("api/courses")
    Call<CoursesStepicResponse> getCourses(@Query("is_featured") boolean is_featured,
                                           @Query("enrolled") boolean enrolled,
                                           @Query("page") int page);
}
