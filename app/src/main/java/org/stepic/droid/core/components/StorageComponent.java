package org.stepic.droid.core.components;

import org.stepic.droid.core.modules.StorageModule;
import org.stepic.droid.store.operations.DatabaseFacade;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {StorageModule.class})
public interface StorageComponent {
    void inject(DatabaseFacade databaseFacade);
}
