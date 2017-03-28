package org.stepic.droid.core.presenters;

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

import java.util.concurrent.ThreadPoolExecutor;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
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

    @Before
    public void beforeEachTest() {
        MockitoAnnotations.initMocks(this);

        ConcurrencyUtilForTest.transformToBlockingMock(threadPoolExecutor);
        ConcurrencyUtilForTest.transformToBlockingMock(mainHandler);

        generateLocalModels();//generate some data for using in different tests

        profilePresenter = new ProfilePresenter(
                threadPoolExecutor,
                analytic,
                mainHandler,
                api,
                sharedPreferenceHelper
        );

    }

    private void generateLocalModels() {
        final String name = "John";
        String lastName = "Doe";
        long profileId = 272;
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

}
