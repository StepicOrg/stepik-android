package org.stepic.droid.store;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SynchronizedCancelSniffer implements ICancelSniffer {

    private final Set<Long> canceledStepIdsSet;
    private final Set<Long> canceledSectionIdsSet;
    private final Set<Long> canceledUnitIdsSet;

    @Inject
    public SynchronizedCancelSniffer() {
        canceledStepIdsSet = new HashSet<>();
        canceledSectionIdsSet = new HashSet<>();
        canceledUnitIdsSet = new HashSet<>();
    }

    @Override
    public void addStepIdCancel(long stepId) {
        synchronized (canceledStepIdsSet) {
            canceledStepIdsSet.add(stepId);
        }
    }

    @Override
    public void removeStepIdCancel(long stepId) {
        synchronized (canceledStepIdsSet) {
            canceledStepIdsSet.remove(stepId);
        }
    }

    @Override
    public boolean isStepIdCanceled(long stepId) {
        synchronized (canceledStepIdsSet) {
            return canceledStepIdsSet.contains(stepId);
        }
    }

    @Override
    public void addSectionIdCancel(long sectionId) {
        synchronized (canceledSectionIdsSet) {
            canceledSectionIdsSet.add(sectionId);
        }
    }

    @Override
    public void removeSectionIdCancel(long sectionId) {
        synchronized (canceledSectionIdsSet) {
            canceledSectionIdsSet.remove(sectionId);
        }
    }

    @Override
    public boolean isSectionIdIsCanceled(long sectionId) {
        synchronized (canceledSectionIdsSet) {
            return canceledSectionIdsSet.contains(sectionId);
        }
    }

    @Override
    public void addUnitIdCancel(long unitId) {
        synchronized (canceledUnitIdsSet) {
            canceledUnitIdsSet.add(unitId);
        }
    }

    @Override
    public void removeUnitIdCancel(long unitId) {
        synchronized (canceledUnitIdsSet) {
            canceledUnitIdsSet.remove(unitId);
        }
    }

    @Override
    public boolean isUnitIdIsCanceled(long unitId) {
        synchronized (canceledUnitIdsSet) {
            return canceledUnitIdsSet.contains(unitId);
        }
    }

}