package org.stepic.droid.events.sections;

public class SectionCachedEvent {
    long sectionId;

    public SectionCachedEvent(long sectionId) {
        this.sectionId = sectionId;
    }

    public long getSectionId() {
        return sectionId;
    }
}
