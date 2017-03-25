package org.stepic.droid.core.components;

import android.content.Context;

import org.stepic.droid.core.modules.StorageModule;
import org.stepic.droid.store.operations.DatabaseFacade;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;

@Singleton
@Component(modules = {StorageModule.class})
public interface StorageComponent {

    @Component.Builder
    interface Builder {
        StorageComponent build();

        @BindsInstance
        Builder context(Context context);
    }

    void inject(DatabaseFacade databaseFacade);
}
