package org.stepic.droid.web;

import org.stepic.droid.model.EnrollmentWrapper;

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
    Call<UserStepicResponse> getUsers(@Query("ids[]") long[] userIds);

    @GET("api/stepics/1")
    Call<StepicProfileResponse> getUserProfile();

    //todo:enrolled always true
    @GET("api/courses")
    Call<CoursesStepicResponse> getEnrolledCourses(@Query("enrolled") boolean enrolled,
                                                   @Query("page") int page);

    //todo:is_featured always true
    @GET("api/courses")
    Call<CoursesStepicResponse> getFeaturedCourses(@Query("is_featured") boolean is_featured,
                                                   @Query("page") int page);


}
