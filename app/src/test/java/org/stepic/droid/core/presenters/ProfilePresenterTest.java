package org.stepic.droid.core.presenters;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.concurrency.MainHandler;
import org.stepic.droid.core.ProfilePresenter;
import org.stepic.droid.core.presenters.contracts.ProfileView;
import org.stepic.droid.model.Profile;
import org.stepic.droid.model.UserViewModel;
import org.stepic.droid.preferences.SharedPreferenceHelper;
import org.stepic.droid.test_utils.ConcurrencyUtilForTest;
import org.stepic.droid.test_utils.FakeProfileGenerator;
import org.stepic.droid.util.ProfileExtensionKt;
import org.stepic.droid.web.Api;

import java.io.IOException;
import java.util.concurrent.ThreadPoolExecutor;

import okhttp3.mockwebserver.MockWebServer;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class ProfilePresenterTest {

    private ProfilePresenter profilePresenter; //we test logic of this object

    @Mock
    private ThreadPoolExecutor threadPoolExecutor;

    @Mock
    Analytic analytic;

    @Mock
    MainHandler mainHandler;

    @Mock
    Api api;

    @Mock
    SharedPreferenceHelper sharedPreferenceHelper;

    @Mock
    ProfileView profileView;


    private Profile preferencesProfileModel;
    private UserViewModel fromPreferencesUserViewModel;

    private String responseBodySuccess; //it should be refined
    private UserViewModel fromApiUserViewModel;

    MockWebServer mockWebServer;

    @Before
    public void beforeEachTest() throws IOException {
        MockitoAnnotations.initMocks(this);

        ConcurrencyUtilForTest.transformToBlockingMock(threadPoolExecutor);
        ConcurrencyUtilForTest.transformToBlockingMock(mainHandler);

        generateLocalModels();//generate some data for using in different tests
        generateInstructorApiModels();

        profilePresenter = new ProfilePresenter(
                threadPoolExecutor,
                analytic,
                mainHandler,
                api,
                sharedPreferenceHelper
        );

        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @After
    public void afterEachTEst() throws Exception {
        mockWebServer.shutdown();
    }

    private void generateInstructorApiModels() {
        final String name = "Ivan";
        String lastName = "Pushkin";
        long profileId = 1889111;
        String imageLink = "https://stepik.org/users/23185159/e918ce43f1d870b3acfaf7021ef40b335c7486c6/avatar.svg";
        boolean isMyProfile = false;
        String shortBio = "My short bio";
        String details = " DetailedInfo";

        fromApiUserViewModel = new UserViewModel(ProfileExtensionKt.getFirstAndLastName(preferencesProfileModel), shortBio, details, imageLink, isMyProfile, profileId);
        responseBodySuccess = "{\n" +
                "meta: {\n" +
                "page: 1,\n" +
                "has_next: false,\n" +
                "has_previous: false\n" +
                "},\n" +
                "users: [\n" +
                "{\n" +
                "id: " + profileId + ",\n" +
                "profile: " + profileId + ",\n" +
                "is_private: false,\n" +
                "is_active: true,\n" +
                "is_guest: true,\n" +
                "is_organization: false,\n" +
                "short_bio: \"" + shortBio + "\",\n" +
                "details: \"" + details + "\",\n" +
                "first_name: \"" + name + "\",\n" +
                "last_name: \"" + lastName + "\",\n" +
                "alias: null,\n" +
                "avatar: \"" + imageLink + "\",\n" +
                "cover: null,\n" +
                "level: 0,\n" +
                "level_title: \"guest\",\n" +
                "tag_progresses: [ ],\n" +
                "knowledge: 0,\n" +
                "knowledge_rank: null,\n" +
                "reputation: 0,\n" +
                "reputation_rank: null,\n" +
                "join_date: null,\n" +
                "social_profiles: [ ],\n" +
                "created_courses_count: 0,\n" +
                "created_lessons_count: 0,\n" +
                "issued_certificates_count: 0\n" +
                "}\n" +
                "]\n" +
                "}";
    }

    private void generateLocalModels() {
        final String name = "Johned";
        String lastName = "Doe";
        long profileId = 27222;
        String imageLink = null;
        boolean isMyProfile = true;
        String shortBio = "it is short bio";
        String details = " details";

        preferencesProfileModel = FakeProfileGenerator.INSTANCE.generateFakeProfile(profileId, name, lastName, imageLink, shortBio, details);
        fromPreferencesUserViewModel = new UserViewModel(ProfileExtensionKt.getFirstAndLastName(preferencesProfileModel), shortBio, details, imageLink, isMyProfile, profileId);
    }

    @Test
    public void initProfile_stored_success() {
        profilePresenter.attachView(profileView);

        //verify that view does not show loading
        verify(profileView, never()).showLoadingAll();

        //prepare data for local profile
        when(sharedPreferenceHelper.getProfile())
                .thenReturn(preferencesProfileModel);

        profilePresenter.initProfile();
        verify(api, never()).getUsers(any(long[].class));
        verify(threadPoolExecutor).execute(any(Runnable.class));

        //verify that errors was not called
        verify(profileView, never()).onInternetFailed();
        verify(profileView, never()).onProfileNotFound();

        //verify calls of view methods
        InOrder inOrder = inOrder(profileView);
        inOrder.verify(profileView).showLoadingAll();
        inOrder.verify(profileView).showNameImageShortBio(fromPreferencesUserViewModel);
    }

    @Test
    public void initProfile_notStoredNoInternet_internetFailed() throws IOException {
        profilePresenter.attachView(profileView);

        when(sharedPreferenceHelper.getProfile())
                .thenReturn(null);
        when(api.getUsers(any(long[].class)))
                .thenThrow(RuntimeException.class); //throw exception on getting from api instead of executing for simplify testing

        profilePresenter.initProfile();

        verify(sharedPreferenceHelper, times(1)).getProfile();
        verify(api, times(1)).getUsers(any(long[].class));

        InOrder inOrder = inOrder(profileView);
        inOrder.verify(profileView).showLoadingAll();
        inOrder.verify(profileView).onInternetFailed();

        verify(profileView, never()).onProfileNotFound();
        verify(profileView, never()).showNameImageShortBio(any(UserViewModel.class));

    }

    @Test
    public void initProfile_storedCachingLocally_successNotRepeat() {
        profilePresenter.attachView(profileView);

        when(sharedPreferenceHelper.getProfile())
                .thenReturn(preferencesProfileModel);

        int n = 10;
        for (int i = 0; i < n; i++) {
            profilePresenter.initProfile();
        }

        verify(sharedPreferenceHelper, times(1)).getProfile();

        //verify calls of view methods
        InOrder inOrder = inOrder(profileView);
        inOrder.verify(profileView).showLoadingAll();
        inOrder.verify(profileView, times(n)).showNameImageShortBio(fromPreferencesUserViewModel);
    }


}
