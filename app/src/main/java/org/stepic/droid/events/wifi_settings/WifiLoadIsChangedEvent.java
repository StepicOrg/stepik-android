package org.stepic.droid.events.wifi_settings;

public class WifiLoadIsChangedEvent {
    final boolean newStateMobileAllowed;

    public WifiLoadIsChangedEvent(boolean newStateMobileAllowed) {
        this.newStateMobileAllowed = newStateMobileAllowed;
    }

    public boolean isNewStateMobileAllowed() {
        return newStateMobileAllowed;
    }
}
