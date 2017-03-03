package org.stepic.droid.store;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;

public class ConcurrentCancelSniffer implements CancelSniffer {

    private final Set<Long> canceledStepIdsSet;
    private final Set<Long> canceledSectionIdsSet;
    private final Set<Long> canceledUnitIdsSet;

    @Inject
    public ConcurrentCancelSniffer() {
        canceledStepIdsSet = Collections.newSetFromMap(new ConcurrentHashMap<Long, Boolean>());
        canceledSectionIdsSet = Collections.newSetFromMap(new ConcurrentHashMap<Long, Boolean>());
        canceledUnitIdsSet = Collections.newSetFromMap(new ConcurrentHashMap<Long, Boolean>());
    }

    @Override
    public void addStepIdCancel(long stepId) {
        canceledStepIdsSet.add(stepId);
    }

    @Override
    public void removeStepIdCancel(long stepId) {
        canceledStepIdsSet.remove(stepId);
    }

    @Override
    public boolean isStepIdCanceled(long stepId) {
        return canceledStepIdsSet.contains(stepId);
    }

    @Override
    public void addSectionIdCancel(long sectionId) {
        canceledSectionIdsSet.add(sectionId);
    }

    @Override
    public void removeSectionIdCancel(long sectionId) {
        canceledSectionIdsSet.remove(sectionId);
    }

    @Override
    public boolean isSectionIdIsCanceled(long sectionId) {
        return canceledSectionIdsSet.contains(sectionId);
    }

    @Override
    public void addLessonToCancel(long lessonId) {
        canceledUnitIdsSet.add(lessonId);
    }

    @Override
    public void removeLessonIdToCancel(long lessonId) {
        canceledUnitIdsSet.remove(lessonId);
    }

    @Override
    public boolean isLessonIdIsCanceled(long lessonId) {
        return canceledUnitIdsSet.contains(lessonId);
    }
}
