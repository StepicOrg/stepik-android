package org.stepic.droid.core.presenters;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.stepic.droid.concurrency.MainHandler;
import org.stepic.droid.core.presenters.contracts.DownloadingInteractionView;
import org.stepic.droid.preferences.UserPreferences;
import org.stepic.droid.testUtils.ConcurrencyUtilForTest;
import org.stepic.droid.util.connectivity.NetworkType;
import org.stepic.droid.util.connectivity.NetworkTypeDeterminer;

import java.io.IOException;
import java.util.concurrent.ThreadPoolExecutor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DownloadingInteractionPresenterTest {
    private DownloadingInteractionPresenter downloadingInteractionPresenter; //we test logic of this object

    @Mock
    private ThreadPoolExecutor threadPoolExecutor;

    @Mock
    private MainHandler mainHandler;

    @Mock
    private UserPreferences userPreferences;

    @Mock
    private DownloadingInteractionView downloadingInteractionView;

    @Mock
    NetworkTypeDeterminer networkTypeDeterminer;

    private int defaultPosition;

    @Before
    public void beforeEachTest() throws IOException {
        MockitoAnnotations.initMocks(this);

        ConcurrencyUtilForTest.transformToBlockingMock(threadPoolExecutor);
        ConcurrencyUtilForTest.transformToBlockingMock(mainHandler);

        defaultPosition = 5;

        downloadingInteractionPresenter = new DownloadingInteractionPresenter(
                threadPoolExecutor,
                mainHandler,
                userPreferences,
                networkTypeDeterminer
        );

    }


    @Test
    public void checkOnLoading_enabledWiFi_OnlyWiFiPref_Load() {
        setUpDependencies(NetworkType.wifi, false);


        attachAndCallChecking(defaultPosition);

        verify(userPreferences, never()).isNetworkMobileAllowed(); // we should not call, because wifi == insta load
        verify(networkTypeDeterminer).determineNetworkType();

        verify(downloadingInteractionView).onLoadingAccepted(defaultPosition);
        verify(downloadingInteractionView, never()).onShowInternetIsNotAvailableRetry(any(Integer.class));
        verify(downloadingInteractionView, never()).onShowPreferenceSuggestion();
    }

    @Test
    public void checkOnLoading_enabledWiFi_MobilePref_Load() {
        setUpDependencies(NetworkType.wifi, true);

        attachAndCallChecking(defaultPosition);

        verify(userPreferences, never()).isNetworkMobileAllowed();
        verify(networkTypeDeterminer).determineNetworkType();

        verify(downloadingInteractionView).onLoadingAccepted(defaultPosition);
        verify(downloadingInteractionView, never()).onShowInternetIsNotAvailableRetry(any(Integer.class));
        verify(downloadingInteractionView, never()).onShowPreferenceSuggestion();

    }

    @Test
    public void checkOnLoading_enabledMobile_OnlyWiFiPref_PreferenceSuggestion() {
        setUpDependencies(NetworkType.onlyMobile, false);

        attachAndCallChecking(defaultPosition);

        verify(userPreferences).isNetworkMobileAllowed();
        verify(networkTypeDeterminer).determineNetworkType();

        verify(downloadingInteractionView, never()).onLoadingAccepted(any(Integer.class));
        verify(downloadingInteractionView, never()).onShowInternetIsNotAvailableRetry(any(Integer.class));
        verify(downloadingInteractionView).onShowPreferenceSuggestion();
    }

    @Test
    public void checkOnLoading_enabledMobile_MobilePref_Load() {
        setUpDependencies(NetworkType.onlyMobile, true);

        attachAndCallChecking(defaultPosition);

        verify(userPreferences).isNetworkMobileAllowed();
        verify(networkTypeDeterminer).determineNetworkType();

        verify(downloadingInteractionView).onLoadingAccepted(defaultPosition);
        verify(downloadingInteractionView, never()).onShowInternetIsNotAvailableRetry(any(Integer.class));
        verify(downloadingInteractionView, never()).onShowPreferenceSuggestion();
    }


    @Test
    public void checkOnLoading_none_MobilePref_showRetry() {
        setUpDependencies(NetworkType.none, true);

        attachAndCallChecking(defaultPosition);

        verify(userPreferences, never()).isNetworkMobileAllowed();
        verify(networkTypeDeterminer).determineNetworkType();

        verify(downloadingInteractionView, never()).onLoadingAccepted(any(Integer.class));
        verify(downloadingInteractionView).onShowInternetIsNotAvailableRetry(defaultPosition);
        verify(downloadingInteractionView, never()).onShowPreferenceSuggestion();
    }

    @Test
    public void checkOnLoading_none_OnlyWiFiPref_showRetry() {
        setUpDependencies(NetworkType.none, false);

        attachAndCallChecking(defaultPosition);

        verify(userPreferences, never()).isNetworkMobileAllowed();
        verify(networkTypeDeterminer).determineNetworkType();

        verify(downloadingInteractionView, never()).onLoadingAccepted(any(Integer.class));
        verify(downloadingInteractionView).onShowInternetIsNotAvailableRetry(defaultPosition);
        verify(downloadingInteractionView, never()).onShowPreferenceSuggestion();
    }


    private void attachAndCallChecking(int position) {
        downloadingInteractionPresenter.attachView(downloadingInteractionView);
        downloadingInteractionPresenter.checkOnLoading(position);
        downloadingInteractionPresenter.detachView(downloadingInteractionView);
    }


    private void setUpDependencies(NetworkType type, boolean isMobileAllowed) {
        when(networkTypeDeterminer.determineNetworkType()).thenReturn(type);
        when(userPreferences.isNetworkMobileAllowed()).thenReturn(isMobileAllowed);
    }

}
