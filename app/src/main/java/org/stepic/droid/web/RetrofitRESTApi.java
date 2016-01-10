package org.stepic.droid.web;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.squareup.okhttp.Credentials;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.yandex.metrica.YandexMetrica;

import org.jetbrains.annotations.Nullable;
import org.stepic.droid.base.MainApplication;
import org.stepic.droid.configuration.IConfig;
import org.stepic.droid.core.ScreenManager;
import org.stepic.droid.model.Course;
import org.stepic.droid.model.EnrollmentWrapper;
import org.stepic.droid.preferences.SharedPreferenceHelper;
import org.stepic.droid.preferences.UserPreferences;
import org.stepic.droid.social.SocialManager;
import org.stepic.droid.store.operations.DatabaseManager;
import org.stepic.droid.util.AppConstants;
import org.stepic.droid.util.JsonHelper;
import org.stepic.droid.util.RWLocks;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit.Call;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;

@Singleton
public class RetrofitRESTApi implements IApi {
    @Inject
    SharedPreferenceHelper mSharedPreference;
    @Inject
    ScreenManager screenManager;
    @Inject
    DatabaseManager mDbManager;
    @Inject
    IConfig mConfig;
    @Inject
    UserPreferences userPreferences;

    private StepicRestLoggedService mLoggedService;
    private StepicRestOAuthService mOAuthService;


    public RetrofitRESTApi() {
        MainApplication.component().inject(this);

        makeOauthServiceWithNewAuthHeader(mSharedPreference.isLastTokenSocial() ? TokenType.social : TokenType.loginPassword);
        makeLoggedService();

    }

    private void makeLoggedService() {
        OkHttpClient okHttpClient = new OkHttpClient();
        Interceptor interceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request newRequest = chain.request();
                try {
                    RWLocks.AuthLock.writeLock().lock();
                    AuthenticationStepicResponse response = mSharedPreference.getAuthResponseFromStore();
                    if (response != null) {
                        response = mOAuthService.updateToken(mConfig.getRefreshGrantType(), response.getRefresh_token()).execute().body();
                        mSharedPreference.storeAuthInfo(response);
                        newRequest = chain.request().newBuilder().addHeader("Authorization", getAuthHeaderValueForLogged()).build();
                        return chain.proceed(newRequest);
                    }
                } finally {
                    RWLocks.AuthLock.writeLock().unlock();
                }
                return chain.proceed(newRequest);
            }
        };
        okHttpClient.networkInterceptors().add(interceptor);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(mConfig.getBaseUrl())
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build();
        mLoggedService = retrofit.create(StepicRestLoggedService.class);
    }

    private void makeOauthServiceWithNewAuthHeader(final TokenType type) {
        mSharedPreference.storeLastTokenType(type == TokenType.social ? true : false);
        Interceptor interceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request newRequest = chain.request();
                String credential = Credentials.basic(mConfig.getOAuthClientId(type), mConfig.getOAuthClientSecret(type));
                newRequest = newRequest.newBuilder().addHeader("Authorization", credential).build();
                return chain.proceed(newRequest);
            }
        };
        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.networkInterceptors().add(interceptor);
        Retrofit notLogged = new Retrofit.Builder()
                .baseUrl(mConfig.getBaseUrl())
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build();
        mOAuthService = notLogged.create(StepicRestOAuthService.class);
    }

    @Override
    public Call<AuthenticationStepicResponse> authWithLoginPassword(String login, String password) {
        YandexMetrica.reportEvent("Api:auth with login password");
        makeOauthServiceWithNewAuthHeader(TokenType.loginPassword);
        return mOAuthService.authWithLoginPassword(mConfig.getGrantType(TokenType.loginPassword), login, password);
    }

    @Override
    public Call<AuthenticationStepicResponse> authWithCode(String code) {
        YandexMetrica.reportEvent("Api:auth with social account");
        makeOauthServiceWithNewAuthHeader(TokenType.social);
        return mOAuthService.getTokenByCode(mConfig.getGrantType(TokenType.social), code, mConfig.getRedirectUri());
    }

    @Override
    public Call<IStepicResponse> signUp(String firstName, String secondName, String email, String password) {
        throw new RuntimeException("not implemented");
    }

    public Call<CoursesStepicResponse> getEnrolledCourses(int page) {
        YandexMetrica.reportEvent("Api: get enrolled courses");
        return mLoggedService.getEnrolledCourses(true, page);
    }

    public Call<CoursesStepicResponse> getFeaturedCourses(int page) {
        YandexMetrica.reportEvent("Api:get featured courses)");
        return mLoggedService.getFeaturedCourses(true, page);
    }

    @Override
    public Call<StepicProfileResponse> getUserProfile() {
        YandexMetrica.reportEvent("Api:get user profile");
        return mLoggedService.getUserProfile();
    }

    @Override
    public Call<UserStepicResponse> getUsers(long[] userIds) {
        YandexMetrica.reportEvent("Api:get users");
        return mLoggedService.getUsers(userIds);
    }

    @Override
    public Call<Void> tryJoinCourse(Course course) {
        YandexMetrica.reportEvent("Api:try join to course", JsonHelper.toJson(course));
        EnrollmentWrapper enrollmentWrapper = new EnrollmentWrapper(course.getCourseId());
        return mLoggedService.joinCourse(enrollmentWrapper);
    }

    @Override
    public Call<SectionsStepicResponse> getSections(long[] sectionsIds) {
        YandexMetrica.reportEvent("Api:get sections", JsonHelper.toJson(sectionsIds));
        return mLoggedService.getSections(sectionsIds);
    }

    @Override
    public Call<UnitStepicResponse> getUnits(long[] units) {
        YandexMetrica.reportEvent("Api:get units", JsonHelper.toJson(units));
        return mLoggedService.getUnits(units);
    }

    @Override
    public Call<LessonStepicResponse> getLessons(long[] lessons) {
        YandexMetrica.reportEvent("Api:get lessons", JsonHelper.toJson(lessons));
        return mLoggedService.getLessons(lessons);
    }

    @Override
    public Call<StepResponse> getSteps(long[] steps) {
        YandexMetrica.reportEvent("Api:get steps", JsonHelper.toJson(steps));
        return mLoggedService.getSteps(steps);
    }

    @Override
    public Call<Void> dropCourse(long courseId) {
        YandexMetrica.reportEvent("Api: " + AppConstants.METRICA_DROP_COURSE, JsonHelper.toJson(courseId));
        return mLoggedService.dropCourse(courseId);
    }

    @Override
    public Call<ProgressesResponse> getProgresses(String[] progresses) {
        YandexMetrica.reportEvent("Api: " + AppConstants.METRICA_GET_PROGRESSES);
        return mLoggedService.getProgresses(progresses);
    }

    @Override
    public Call<AssignmentResponse> getAssignments(long[] assignmentsIds) {
        YandexMetrica.reportEvent("Api: " + AppConstants.METRICA_GET_ASSIGNMENTS);
        return mLoggedService.getAssignments(assignmentsIds);
    }

    @Override
    public Call<Void> postViewed(ViewAssignment stepAssignment) {
        return mLoggedService.postViewed(new ViewAssignmentWrapper(stepAssignment.getAssignment(), stepAssignment.getStep()));
    }

    public void loginWithSocial(Context context, SocialManager.SocialType type) {

        String socialIdentifier = type.getIdentifier();
        String url = mConfig.getBaseUrl() + "/accounts/" + socialIdentifier + "/login?next=/oauth2/authorize/?" + Uri.encode("client_id=" + mConfig.getOAuthClientId(TokenType.social) + "&response_type=code");
        Uri uri = Uri.parse(url);
        final Intent intent = new Intent(Intent.ACTION_VIEW).setData(uri);
        context.startActivity(intent);
    }

    @Override
    public Call<SearchResultResponse> getSearchResultsCourses(int page, String rawQuery) {
//        String encodedQuery = Uri.encode(rawQuery);
        String encodedQuery = rawQuery;
        String type = "course";
        return mLoggedService.getSearchResults(page, encodedQuery, type);
    }

    @Override
    public Call<CoursesStepicResponse> getCourses(int page, @Nullable long[] ids) {
        if (ids == null || ids.length == 0) {
            ids = new long[]{0};
        }
        return mLoggedService.getCourses(page, ids);
    }

//    private void setAuthenticatorClientIDAndPassword(OkHttpClient httpClient, final String client_id, final String client_password) {
//        httpClient.setAuthenticator(new Authenticator() {
//            @Override
//            public Request authenticate(Proxy proxy, Response response) throws IOException {
//                YandexMetrica.reportEvent("Never invoked auth");
//                // FIXME: 28.12.15 IT IS NEVER INVOKED. REMOVE
//                String credential = Credentials.basic(client_id, client_password);
//                return response.request().newBuilder().header("Authorization", credential).build();
//            }
//
//            @Override
//            public Request authenticateProxy(Proxy proxy, Response response) throws IOException {
//                return null;
//            }
//        });
//    }

//    private void setAuthForLoggedService(OkHttpClient httpClient) {
//        httpClient.setAuthenticator(new Authenticator() {
//            @Override
//            public Request authenticate(Proxy proxy, Response response) throws IOException {
//                //IT WILL BE INVOKED WHEN Access token will expire (should, but server doesn't handle 401.)
//                //it is not be invoked on get courses for example.
//                RWLocks.AuthLock.writeLock().lock();
//                try {
//                    AuthenticationStepicResponse authData = mSharedPreference.getAuthResponseFromStore();
//                    if (response != null) {
//                        authData = mOAuthService.updateToken(mConfig.getRefreshGrantType(), authData.getRefresh_token()).execute().body();
//                        mSharedPreference.storeAuthInfo(authData);
//                        return response.request().newBuilder().addHeader("Authorization", getAuthHeaderValueForLogged()).build();
//                    }
//                } catch (ProtocolException t) {
//                    // FIXME: 17.12.15 IT IS NOT NORMAL BEHAVIOUR, NEED TO REPAIR CODE.
//                    YandexMetrica.reportError(AppConstants.NOT_VALID_ACCESS_AND_REFRESH, t);
//                    mSharedPreference.deleteAuthInfo();
//                    AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
//                        @Override
//                        protected Void doInBackground(Void... params) {
//
//                            FileUtil.cleanDirectory(userPreferences.getDownloadFolder());
//                            mDbManager.dropDatabase();
//                            return null;
//                        }
//                    };
//                    task.execute();
//                    screenManager.showLaunchScreen(MainApplication.getAppContext(), false);
//
//                    throw t;
//                } finally {
//                    RWLocks.AuthLock.writeLock().unlock();
//                }
//                return null;
//
//            }
//
//            @Override
//            public Request authenticateProxy(Proxy proxy, Response response) throws IOException {
//                return null;
//            }
//        });
//    }

    private String getAuthHeaderValueForLogged() {
        try {
            AuthenticationStepicResponse resp = mSharedPreference.getAuthResponseFromStore();
            String access_token = resp.getAccess_token();
            String type = resp.getToken_type();
            return type + " " + access_token;
        } catch (Exception ex) {
            YandexMetrica.reportError("retrofitAuth", ex);
            Log.e("retrofitAuth", ex.getMessage());
            // FIXME: 19.11.15 It not should happen

            mSharedPreference.deleteAuthInfo();
            AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    mDbManager.clearCacheCourses(DatabaseManager.Table.enrolled);
                    return null;
                }
            };
            task.execute();
            screenManager.showLaunchScreen(MainApplication.getAppContext(), false);
            // FIXME: 19.11.15 ^^^^^^
            return "";
        }
    }
}
