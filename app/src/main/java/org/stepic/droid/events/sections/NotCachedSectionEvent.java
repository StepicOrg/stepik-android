package org.stepic.droid.events.sections;

public class NotCachedSectionEvent {
    long sectionId;

    public NotCachedSectionEvent(long sectionId) {
        this.sectionId = sectionId;
    }

    public long getSectionId() {
        return sectionId;
    }
}
