package org.stepic.droid.core;

import org.stepic.droid.core.presenters.FilterPresenter;
import org.stepic.droid.preferences.SharedPreferenceHelper;

import dagger.Module;
import dagger.Provides;

@Module
public class FilterModule {
    @Provides
    public FilterPresenter provideFilterPresenter(SharedPreferenceHelper sharedPreferenceHelper) {
        return new FilterPresenter(sharedPreferenceHelper);
    }
}
