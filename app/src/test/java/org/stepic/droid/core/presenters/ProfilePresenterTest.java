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
import org.stepic.droid.model.UserViewModel;
import org.stepic.droid.preferences.SharedPreferenceHelper;
import org.stepic.droid.testUtils.ConcurrencyUtilForTest;
import org.stepic.droid.testUtils.generators.FakeProfileGenerator;
import org.stepic.droid.testUtils.generators.FakeUserGenerator;
import org.stepik.android.domain.base.DataSourceType;
import org.stepik.android.domain.user.repository.UserRepository;
import org.stepik.android.domain.user_activity.repository.UserActivityRepository;
import org.stepik.android.domain.user_profile.repository.UserProfileRepository;
import org.stepik.android.model.user.Profile;
import org.stepik.android.model.user.User;

import java.io.IOException;
import java.util.concurrent.ThreadPoolExecutor;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ProfilePresenterTest {

    private ProfilePresenter profilePresenter; //we test logic of this object

    @Mock
    ThreadPoolExecutor threadPoolExecutor;

    @Mock
    Analytic analytic;

    @Mock
    MainHandler mainHandler;

    @Mock
    UserActivityRepository userActivityRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    UserProfileRepository userProfileRepository;

    @Mock
    SharedPreferenceHelper sharedPreferenceHelper;

    @Mock
    ProfileView profileView;


    private Profile preferencesProfileModel;
    private UserViewModel fromPreferencesUserViewModel;

    private User fakeUserFromApi; //it should be refined
    private UserViewModel fromApiUserViewModel;

    @Before
    public void beforeEachTest() throws IOException {
        MockitoAnnotations.initMocks(this);

        ConcurrencyUtilForTest.transformToBlockingMock(threadPoolExecutor);
        ConcurrencyUtilForTest.transformToBlockingMock(mainHandler);

        generateLocalModels();//generate some data for using in different tests
        generateInstructorApiModels();

        profilePresenter = new ProfilePresenterImpl(
                threadPoolExecutor,
                analytic,
                mainHandler,
                userActivityRepository,
                userRepository,
                userProfileRepository,
                sharedPreferenceHelper,
                Observable.empty(),
                Schedulers.io()
        );
    }

    @After
    public void afterEachTEst() throws Exception {
    }

    private void generateInstructorApiModels() {
        final String name = "Ivan";
        String lastName = "Pushkin";
        long profileId = 1889111;
        String imageLink = "https://stepik.org/users/23185159/e918ce43f1d870b3acfaf7021ef40b335c7486c6/avatar.svg";
        boolean isMyProfile = false;
        boolean isPrivate = false;
        boolean isOrganization = false;
        String shortBio = "My short bio";
        String details = " DetailedInfo";

        fakeUserFromApi = FakeUserGenerator.INSTANCE.generate(profileId, name, lastName, imageLink, shortBio, details);
        fromApiUserViewModel = new UserViewModel(name + " " + lastName, shortBio, details, imageLink, isMyProfile, isPrivate, isOrganization, profileId);
    }

    private void generateLocalModels() {
        final String name = "Johned";
        String lastName = "Doe";
        long profileId = 27222;
        String imageLink = null;
        boolean isMyProfile = true;
        boolean isPrivate = false;
        boolean isOrganization = false;
        String shortBio = "it is short bio";
        String details = " details";

        preferencesProfileModel = FakeProfileGenerator.INSTANCE.generate(profileId, name, lastName, imageLink, shortBio, details);
        fromPreferencesUserViewModel = new UserViewModel(name + " " + lastName, shortBio, details, imageLink, isMyProfile, isPrivate, isOrganization, profileId);
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
        verify(userRepository, never()).getUsers(any(long[].class), DataSourceType.REMOTE);
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
        when(userRepository.getUsers(any(long[].class), DataSourceType.REMOTE))
                .thenThrow(RuntimeException.class); //throw exception on getting from api instead of executing for simplify testing

        profilePresenter.initProfile();

        verify(sharedPreferenceHelper).getProfile();
        verify(userProfileRepository).getUserProfile(); //should request the profile of logged user (because init is called without params)

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

        verify(sharedPreferenceHelper).getProfile();

        //verify calls of view methods
        InOrder inOrder = inOrder(profileView);
        inOrder.verify(profileView).showLoadingAll();
        inOrder.verify(profileView, times(n)).showNameImageShortBio(fromPreferencesUserViewModel);
    }

    /*
    @Test
    public void initProfile_notMy_success() throws IOException {
        profilePresenter.attachView(profileView);

        UserResponse responseMock = mock(UserResponse.class);
        List<User> userListMock = (List<User>) mock(List.class);

        ResponseGeneratorKt.useMockInsteadCall(when(api.getUsers(any(long[].class))), responseMock);
        when(responseMock.getUsers()).thenReturn(userListMock);
        when(userListMock.get(0)).thenReturn(fakeUserFromApi);

        when(sharedPreferenceHelper.getProfile()).thenReturn(preferencesProfileModel);


        profilePresenter.initProfile(fromApiUserViewModel.getId());
        profilePresenter.detachView(profileView);

        verify(api).getUsers(any(long[].class));

        InOrder inOrder = inOrder(profileView);
        inOrder.verify(profileView).showLoadingAll();
        inOrder.verify(profileView).showNameImageShortBio(fromApiUserViewModel);

        verify(profileView, never()).onInternetFailed();
        verify(profileView, never()).onProfileNotFound();

    } */

}
