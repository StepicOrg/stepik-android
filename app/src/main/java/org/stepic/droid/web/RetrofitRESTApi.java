package org.stepic.droid.web;

import android.os.Looper;
import android.util.Log;

import com.squareup.okhttp.Authenticator;
import com.squareup.okhttp.Credentials;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.stepic.droid.base.MainApplication;
import org.stepic.droid.configuration.IConfig;
import org.stepic.droid.model.Course;
import org.stepic.droid.model.EnrollmentWrapper;
import org.stepic.droid.model.User;
import org.stepic.droid.util.SharedPreferenceHelper;

import java.io.IOException;
import java.net.Proxy;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit.Call;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;

@Singleton
public class RetrofitRESTApi implements IApi {

    @Inject
    SharedPreferenceHelper mSharedPreferenceHelper;

    @Inject
    IConfig mConfig;

    private StepicRestLoggedService mLoggedService;
    private StepicRestOAuthService mOAuthService;


    public RetrofitRESTApi() {
        MainApplication.component().inject(this);

        OkHttpClient okHttpClient = new OkHttpClient();
        setAuthenticatorClientIDAndPassword(okHttpClient);
        Retrofit notLogged = new Retrofit.Builder()
                .baseUrl(mConfig.getBaseUrl())
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build();
        mOAuthService = notLogged.create(StepicRestOAuthService.class);


        okHttpClient = new OkHttpClient();
        Interceptor interceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                AuthenticationStepicResponse response = mSharedPreferenceHelper.getAuthResponseFromStore();
                Log.i("Thread", Looper.myLooper() == Looper.getMainLooper() ? "main" : "other");
                response = mOAuthService.updateToken(mConfig.getRefreshGrantType(), response.getRefresh_token()).execute().body();//todo: Which Thread is it?
                mSharedPreferenceHelper.storeAuthInfo(response);
                Request newRequest = chain.request().newBuilder().addHeader("Authorization", getAuthHeaderValue()).build();
                return chain.proceed(newRequest);
            }
        };
        okHttpClient.interceptors().add(interceptor);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(mConfig.getBaseUrl())
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build();
        mLoggedService = retrofit.create(StepicRestLoggedService.class);
    }

    @Override
    public Call<AuthenticationStepicResponse> authWithLoginPassword(String login, String password) {
        return mOAuthService.authWithLoginPassword(mConfig.getGrantType(), login, password);
    }

    @Override
    public Call<IStepicResponse> signUp(String firstName, String secondName, String email, String password) {
        throw new RuntimeException("not implemented");
    }

    public Call<CoursesStepicResponse> getEnrolledCourses(int page) {
        return mLoggedService.getEnrolledCourses(true, page);
    }

    public Call<CoursesStepicResponse> getFeaturedCourses(int page) {
        return mLoggedService.getFeaturedCourses(true, page);
    }

    @Override
    public Call<StepicProfileResponse> getUserProfile() {
        return mLoggedService.getUserProfile();
    }

    @Override
    public Call<List<User>> getUsers(long[] userIds) {
        return mLoggedService.getUsers(userIds);
    }

    @Override
    public Call<Void> tryJoinCourse(Course course) {
        EnrollmentWrapper enrollmentWrapper = new EnrollmentWrapper(course.getCourseId());
        return mLoggedService.joinCourse(enrollmentWrapper);
    }

    @Override
    public Call<SectionsStepicResponse> getSections(long[] sectionsIds) {
        return mLoggedService.getSections(sectionsIds);
    }

    private void setAuthenticatorClientIDAndPassword(OkHttpClient httpClient) {
        httpClient.setAuthenticator(new Authenticator() {
            @Override
            public Request authenticate(Proxy proxy, Response response) throws IOException {
                String credential = Credentials.basic(mConfig.getOAuthClientId(), mConfig.getOAuthClientSecret());
                return response.request().newBuilder().header("Authorization", credential).build();
            }

            @Override
            public Request authenticateProxy(Proxy proxy, Response response) throws IOException {
                return null;
            }
        });
    }

    private String getAuthHeaderValue() {
        AuthenticationStepicResponse resp = mSharedPreferenceHelper.getAuthResponseFromStore();
        String access_token = resp.getAccess_token();
        String type = resp.getToken_type();
        return type + " " + access_token;
    }
}
