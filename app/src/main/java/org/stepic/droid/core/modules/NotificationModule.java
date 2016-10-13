package org.stepic.droid.core.modules;

import android.support.v7.widget.RecyclerView;

import org.stepic.droid.core.PerFragment;
import org.stepic.droid.core.presenters.NotificationListPresenter;

import dagger.Module;
import dagger.Provides;

@Module
public class NotificationModule {

    private RecyclerView.RecycledViewPool viewPool;

    public NotificationModule(RecyclerView.RecycledViewPool viewPool) {
        this.viewPool = viewPool;
    }

    @Provides
    @PerFragment
    RecyclerView.RecycledViewPool provideRecyclerViewPool() {
        return viewPool;
    }

    @Provides
    NotificationListPresenter provideNotificationListPresenter() {
        return new NotificationListPresenter();
    }
}
