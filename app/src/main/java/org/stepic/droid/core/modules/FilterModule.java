package org.stepic.droid.core.modules;

import org.stepic.droid.core.presenters.FilterPresenter;
import org.stepic.droid.preferences.SharedPreferenceHelper;

import dagger.Module;
import dagger.Provides;

@Module
public class FilterModule {
    @Provides
    FilterPresenter provideFilterPresenter(SharedPreferenceHelper sharedPreferenceHelper) {
        return new FilterPresenter(sharedPreferenceHelper);
    }
}
