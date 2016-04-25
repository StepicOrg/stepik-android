package org.stepic.droid.web;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.Credentials;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.yandex.metrica.YandexMetrica;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.stepic.droid.R;
import org.stepic.droid.base.MainApplication;
import org.stepic.droid.configuration.IConfig;
import org.stepic.droid.core.ScreenManager;
import org.stepic.droid.deserializers.DatasetDeserializer;
import org.stepic.droid.model.Course;
import org.stepic.droid.model.DatasetWrapper;
import org.stepic.droid.model.EnrollmentWrapper;
import org.stepic.droid.model.Profile;
import org.stepic.droid.model.RegistrationUser;
import org.stepic.droid.model.Reply;
import org.stepic.droid.notifications.model.Notification;
import org.stepic.droid.preferences.SharedPreferenceHelper;
import org.stepic.droid.preferences.UserPreferences;
import org.stepic.droid.social.ISocialType;
import org.stepic.droid.store.operations.DatabaseFacade;
import org.stepic.droid.util.AppConstants;
import org.stepic.droid.util.DeviceInfoUtil;
import org.stepic.droid.util.HtmlHelper;
import org.stepic.droid.util.JsonHelper;
import org.stepic.droid.util.RWLocks;

import java.io.IOException;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit.Call;
import retrofit.Converter;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;

@Singleton
public class RetrofitRESTApi implements IApi {
    private final int TIMEOUT_IN_SECONDS = 10;

    @Inject
    SharedPreferenceHelper mSharedPreference;
    @Inject
    ScreenManager screenManager;
    @Inject
    DatabaseFacade mDbManager;
    @Inject
    IConfig mConfig;
    @Inject
    UserPreferences mUserPreferences;

    private StepicRestLoggedService mLoggedService;
    private StepicRestOAuthService mOAuthService;
    private StepicEmptyAuthService mStepicEmptyAuthService;
    private StepicZendeskEmptyAuthService mZendeskAuthService;
    private final OkHttpClient okHttpClient = new OkHttpClient();


    public RetrofitRESTApi() {
        MainApplication.component().inject(this);

        makeOauthServiceWithNewAuthHeader(mSharedPreference.isLastTokenSocial() ? TokenType.social : TokenType.loginPassword);
        makeLoggedService();

        OkHttpClient okHttpClient = new OkHttpClient();
        setTimeout(okHttpClient, TIMEOUT_IN_SECONDS);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(mConfig.getBaseUrl())
                .addConverterFactory(generateGsonFactory())
                .client(okHttpClient)
                .build();
        mStepicEmptyAuthService = retrofit.create(StepicEmptyAuthService.class);
//        makeZendeskService();
    }

//    private void makeZendeskService() {
//        OkHttpClient okHttpClient = new OkHttpClient();
//        setTimeout(okHttpClient, TIMEOUT_IN_SECONDS);
//        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl(mConfig.getZendeskHost())
//                .client(okHttpClient)
//                .build();
//        mZendeskAuthService = retrofit.create(StepicZendeskEmptyAuthService.class);
//    }


    private void makeLoggedService() {
        OkHttpClient okHttpClient = new OkHttpClient();
        Interceptor interceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request newRequest = chain.request();
                try {
                    RWLocks.AuthLock.writeLock().lock();
                    AuthenticationStepicResponse response = mSharedPreference.getAuthResponseFromStore();
                    if (isNeededUpdate(response)) {
                        try {
                            response = mOAuthService.updateToken(mConfig.getRefreshGrantType(), response.getRefresh_token()).execute().body();
                        } catch (Exception e) {
                            YandexMetrica.reportError("cant update token", e);
                            return chain.proceed(newRequest);
                        }
                        if (response == null || !response.isSuccess()) {
                            //it is worst case:
                            YandexMetrica.reportEvent("update is failed");
                            return chain.proceed(newRequest);
                        }

                        //Update is success:
                        mSharedPreference.storeAuthInfo(response);
                    }
                    if (response != null) {
                        //it is good way
                        newRequest = chain.request().newBuilder().addHeader("Authorization", getAuthHeaderValueForLogged()).build();
                    }
                    return chain.proceed(newRequest);
                } finally {
                    RWLocks.AuthLock.writeLock().unlock();
                }

            }
        };
        okHttpClient.networkInterceptors().add(interceptor);
        setTimeout(okHttpClient, TIMEOUT_IN_SECONDS);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(mConfig.getBaseUrl())
                .addConverterFactory(generateGsonFactory())
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
        setTimeout(okHttpClient, TIMEOUT_IN_SECONDS);
        okHttpClient.networkInterceptors().add(interceptor);
        Retrofit notLogged = new Retrofit.Builder()
                .baseUrl(mConfig.getBaseUrl())
                .addConverterFactory(generateGsonFactory())
                .client(okHttpClient)
                .build();
        mOAuthService = notLogged.create(StepicRestOAuthService.class);
    }

    private Converter.Factory generateGsonFactory() {
        Gson gson = new GsonBuilder().registerTypeAdapter(DatasetWrapper.class, new DatasetDeserializer())
                .create();
        return GsonConverterFactory.create(gson);
    }

    private void setTimeout(OkHttpClient okHttpClient, int seconds) {
        okHttpClient.setConnectTimeout(seconds, TimeUnit.SECONDS);
        okHttpClient.setReadTimeout(seconds, TimeUnit.SECONDS);
    }

    @Override
    public Call<AuthenticationStepicResponse> authWithLoginPassword(String login, String password) {
        YandexMetrica.reportEvent("Api:auth with login password");
        makeOauthServiceWithNewAuthHeader(TokenType.loginPassword);
        String encodedPassword = URLEncoder.encode(password);
        String encodedLogin = URLEncoder.encode(login);
        return mOAuthService.authWithLoginPassword(mConfig.getGrantType(TokenType.loginPassword), encodedLogin, encodedPassword);
    }

    @Override
    public Call<AuthenticationStepicResponse> authWithCode(String code) {
        YandexMetrica.reportEvent("Api:auth with social account");
        makeOauthServiceWithNewAuthHeader(TokenType.social);
        return mOAuthService.getTokenByCode(mConfig.getGrantType(TokenType.social), code, mConfig.getRedirectUri());
    }

    @Override
    public Call<RegistrationResponse> signUp(String firstName, String lastName, String email, String password) {
        YandexMetrica.reportEvent("Api: try register");

        OkHttpClient okHttpClient = new OkHttpClient();
        Interceptor interceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request newRequest = chain.request();

                List<HttpCookie> cookies = getCookiesForBaseUrl();
                if (cookies == null)
                    return chain.proceed(newRequest);
                String csrftoken = null;
                String sessionId = null;
                for (HttpCookie item : cookies) {
                    if (item.getName() != null && item.getName().equals("csrftoken")) {
                        csrftoken = item.getValue();
                        continue;
                    }
                    if (item.getName() != null && item.getName().equals("sessionid")) {
                        sessionId = item.getValue();
                    }
                }

                String cookieResult = "csrftoken=" + csrftoken + "; " + "sessionid=" + sessionId;

                Request.Builder requestBuilder = chain
                        .request()
                        .newBuilder()
                        .addHeader("Referer", mConfig.getBaseUrl())
                        .addHeader("X-CSRFToken", csrftoken)
                        .addHeader("Cookie", cookieResult);
                newRequest = requestBuilder.build();
                return chain.proceed(newRequest);
            }
        };
        okHttpClient.networkInterceptors().add(interceptor);
        Retrofit notLogged = new Retrofit.Builder()
                .baseUrl(mConfig.getBaseUrl())
                .addConverterFactory(generateGsonFactory())
                .client(okHttpClient)
                .build();
        StepicRestOAuthService tempService = notLogged.create(StepicRestOAuthService.class);
        return tempService.createAccount(new UserRegistrationRequest(new RegistrationUser(firstName, lastName, email, password)));
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
        if (!mConfig.isUserCanDropCourse()) return null;
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

    public void loginWithSocial(Context context, ISocialType type) {

        String socialIdentifier = type.getIdentifier();
        String url = mConfig.getBaseUrl() + "/accounts/" + socialIdentifier + "/login?next=/oauth2/authorize/?" + Uri.encode("client_id=" + mConfig.getOAuthClientId(TokenType.social) + "&response_type=code");
        Uri uri = Uri.parse(url);
        final Intent intent = new Intent(Intent.ACTION_VIEW).setData(uri);
        context.startActivity(intent);
    }

    @Override
    public Call<SearchResultResponse> getSearchResultsCourses(int page, String rawQuery) {
        String encodedQuery = URLEncoder.encode(rawQuery);
//        String encodedQuery = rawQuery;
        YandexMetrica.reportEvent(AppConstants.SEARCH, JsonHelper.toJson(rawQuery));
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

    @Override
    public Call<AttemptResponse> createNewAttempt(long stepId) {
        AttemptRequest attemptRequest = new AttemptRequest(stepId);
        return mLoggedService.createNewAttempt(attemptRequest);
    }

    @Override
    public Call<SubmissionResponse> createNewSubmission(Reply reply, long attemptId) {
        SubmissionRequest submissionRequest = new SubmissionRequest(reply, attemptId);
        return mLoggedService.createNewSubmission(submissionRequest);
    }

    @Override
    public Call<AttemptResponse> getExistingAttempts(long stepId) {
        Profile profile = mSharedPreference.getProfile();
        long userId = 0;
        if (profile == null) {
            YandexMetrica.reportEvent("profile is null, when attempt");
        } else {
            userId = profile.getId();
        }
        return mLoggedService.getExistingAttempts(stepId, userId);
    }

    @Override
    public Call<SubmissionResponse> getSubmissions(long attemptId) {
        String order = "desc";

        return mLoggedService.getExistingSubmissions(attemptId, order);
    }

    @Override
    public Call<Void> remindPassword(String email) {
        String encodedEmail = URLEncoder.encode(email);

        OkHttpClient okHttpClient = new OkHttpClient();
        Interceptor interceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request newRequest = chain.request();

                List<HttpCookie> cookies = getCookiesForBaseUrl();
                if (cookies == null)
                    return chain.proceed(newRequest);
                String csrftoken = null;
                String sessionId = null;
                for (HttpCookie item : cookies) {
                    if (item.getName() != null && item.getName().equals("csrftoken")) {
                        csrftoken = item.getValue();
                        continue;
                    }
                    if (item.getName() != null && item.getName().equals("sessionid")) {
                        sessionId = item.getValue();
                    }
                }

                String cookieResult = "csrftoken=" + csrftoken + "; " + "sessionid=" + sessionId;
                if (csrftoken == null) return chain.proceed(newRequest);
                HttpUrl url = newRequest
                        .httpUrl()
                        .newBuilder()
                        .addQueryParameter("csrfmiddlewaretoken", csrftoken)
                        .addQueryParameter("csrfmiddlewaretoken", csrftoken)
                        .build();
                newRequest = newRequest.newBuilder()
                        .addHeader("referer", mConfig.getBaseUrl())
                        .addHeader("X-CSRFToken", csrftoken)
                        .addHeader("Cookie", cookieResult)
                        .url(url)
                        .build();
                return chain.proceed(newRequest);
            }
        };
        okHttpClient.networkInterceptors().add(interceptor);
        Retrofit notLogged = new Retrofit.Builder()
                .baseUrl(mConfig.getBaseUrl())
                .addConverterFactory(generateGsonFactory())
                .client(okHttpClient)
                .build();
        StepicEmptyAuthService tempService = notLogged.create(StepicEmptyAuthService.class);
        return tempService.remindPassword(encodedEmail);

    }

    @Override
    public Call<EmailAddressResponse> getEmailAddresses(@NotNull long[] ids) {
        return mLoggedService.getEmailAddresses(ids);
    }

    @Override
    public Call<Void> sendFeedback(String email, String rawDescription) {


        Interceptor interceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request newRequest = chain.request();

                String csrftoken = null;
                String zendesk_shared_session = null;
                String help_center_session = null;
                try {
                    Response response = getZendeskResponse();
                    String htmlText = response.body().string();
                    csrftoken = HtmlHelper.getValueOfMetaOrNull(htmlText, "csrf-token");

                    Headers headers = response.headers();
                    CookieManager cookieManager = new CookieManager();
                    URI myUri = null;
                    try {
                        myUri = new URI(mConfig.getZendeskHost());
                    } catch (URISyntaxException e) {
                        return null;
                    }
                    cookieManager.put(myUri, headers.toMultimap());
                    List<HttpCookie> cookies = cookieManager.getCookieStore().get(myUri);

                    for (HttpCookie item : cookies) {
                        if (item.getName() != null && item.getName().equals("_zendesk_shared_session")) {
                            zendesk_shared_session = item.getValue();
                            continue;
                        }
                        if (item.getName() != null && item.getName().equals("_help_center_session")) {
                            help_center_session = item.getValue();
                        }
                    }

                } catch (Exception e) {
                    return chain.proceed(newRequest);
                }
                if (csrftoken == null && zendesk_shared_session == null && help_center_session == null) {
                    return chain.proceed(newRequest);
                }


                String cookieResult = "_zendesk_shared_session=" + zendesk_shared_session + "; " + "_help_center_session=" + help_center_session;

                HttpUrl url = newRequest
                        .httpUrl()
                        .newBuilder()
                        .addQueryParameter("authenticity_token", csrftoken)
                        .build();
                newRequest = newRequest.newBuilder()
                        .addHeader("Cookie", cookieResult)
                        .url(url)
                        .build();
                return chain.proceed(newRequest);
            }
        };

        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.networkInterceptors().add(interceptor);
        Retrofit notLogged = new Retrofit.Builder()
                .baseUrl(mConfig.getZendeskHost())
                .addConverterFactory(generateGsonFactory())
                .client(okHttpClient)
                .build();
        StepicZendeskEmptyAuthService tempService = notLogged.create(StepicZendeskEmptyAuthService.class);

        String encodedEmail = URLEncoder.encode(email);
        String encodedDescription = URLEncoder.encode(rawDescription);
        String subject = MainApplication.getAppContext().getString(R.string.feedback_subject);
        String encodedSubject = URLEncoder.encode(subject);
        String aboutSystem = DeviceInfoUtil.getInfosAboutDevice(MainApplication.getAppContext());
        String encodedSystem = URLEncoder.encode(aboutSystem);
        return tempService.sendFeedback(encodedSubject, encodedEmail, encodedSystem, encodedDescription, "www.stepic.org");
    }

    @Override
    public Call<DeviceResponse> getDevices() {
        Profile profile = mSharedPreference.getProfile();
        long userId = 0;
        if (profile != null) {
            userId = profile.getId();
        }
        return mLoggedService.getDevices(userId);
    }

    @Override
    public Call<DeviceResponse> registerDevice(String token) {
        String description = DeviceInfoUtil.getShortInfo(MainApplication.getAppContext());
        DeviceRequest deviceRequest = new DeviceRequest(token, description);
        return mLoggedService.registerDevice(deviceRequest);
    }

    @Override
    public Call<CoursesStepicResponse> getCourse(long id) {
        long[] ids = new long[]{id};
        return mLoggedService.getCourses(ids);
    }

    @Override
    public Call<Void> markNotificationAsRead(long notificationId, boolean isRead) {
        Notification notification = new Notification();
        notification.set_unread(!isRead);
        return mLoggedService.putNotification(notificationId, new NotificationRequest(notification));
    }

    @Override
    public Call<Void> removeDevice(long deviceId) {
        return mLoggedService.removeDevice(deviceId);
    }

    @Override
    public UpdateResponse getInfoForUpdating() throws IOException {
        Request request = new Request.Builder()
                .url(mConfig.getBaseUrl() + "/" + mConfig.getUpdateEndpoint())
                .build();

        String jsonString = okHttpClient.newCall(request).execute().body().string();

        Gson gson = new Gson();
        UpdateResponse response = gson.fromJson(jsonString, UpdateResponse.class);
        return response;
    }

    @Nullable
    private Response getZendeskResponse() throws IOException {
        OkHttpClient client = new OkHttpClient();

        String url = mConfig.getZendeskHost() + "/hc/ru/requests/new";

        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        return response;
    }

    @Nullable
    private List<HttpCookie> getCookiesForBaseUrl() throws IOException {
        String lang = Locale.getDefault().getLanguage();
        retrofit.Response ob = mStepicEmptyAuthService.getStepicForFun(lang).execute();
        Headers headers = ob.headers();
        CookieManager cookieManager = new CookieManager();
        URI myUri = null;
        try {
            myUri = new URI(mConfig.getBaseUrl());
        } catch (URISyntaxException e) {
            return null;
        }
        cookieManager.put(myUri, headers.toMultimap());
        return cookieManager.getCookieStore().get(myUri);
    }

    private String getAuthHeaderValueForLogged() {
        try {
            AuthenticationStepicResponse resp = mSharedPreference.getAuthResponseFromStore();
            if (resp == null) {
                YandexMetrica.reportEvent("resp null");
                return "";
            }
            String access_token = resp.getAccess_token();
            String type = resp.getToken_type();
            return type + " " + access_token;
        } catch (Exception ex) {
            YandexMetrica.reportError("retrofitAuth", ex);
            // FIXME: 19.11.15 It not should happen

            mSharedPreference.deleteAuthInfo();
            AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    mDbManager.clearCacheCourses(DatabaseFacade.Table.enrolled);
                    return null;
                }
            };
            task.execute();
            screenManager.showLaunchScreen(MainApplication.getAppContext(), false);
            // FIXME: 19.11.15 ^^^^^^
            return "";
        }
    }

    private boolean isNeededUpdate(AuthenticationStepicResponse response) {
        if (response == null) return false;

        long timestampStored = mSharedPreference.getAccessTokenTimestamp();
        if (timestampStored == -1) return true;

        long nowTemp = DateTime.now(DateTimeZone.UTC).getMillis();
        long delta = nowTemp - timestampStored;
        long expiresMillis = (response.getExpires_in() - 50) * 1000;
        return delta > expiresMillis;//token expired --> need update
    }
}
