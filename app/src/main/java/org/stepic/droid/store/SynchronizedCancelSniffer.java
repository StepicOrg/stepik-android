package org.stepic.droid.store;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SynchronizedCancelSniffer implements ICancelSniffer {

    private final Set<Long> mCanceledStepIdsSet;
    private final Set<Long> mCanceledSectionIdsSet;
    private final Set<Long> mCanceledUnitIdsSet;

    @Inject
    public SynchronizedCancelSniffer() {
        mCanceledStepIdsSet = new HashSet<>();
        mCanceledSectionIdsSet = new HashSet<>();
        mCanceledUnitIdsSet = new HashSet<>();
    }

    @Override
    public void addStepIdCancel(long stepId) {
        synchronized (mCanceledStepIdsSet) {
            mCanceledStepIdsSet.add(stepId);
        }
    }

    @Override
    public void removeStepIdCancel(long stepId) {
        synchronized (mCanceledStepIdsSet) {
            mCanceledStepIdsSet.remove(stepId);
        }
    }

    @Override
    public boolean isStepIdCanceled(long stepId) {
        synchronized (mCanceledStepIdsSet) {
            return mCanceledStepIdsSet.contains(stepId);
        }
    }

    @Override
    public void addSectionIdCancel(long sectionId) {
        synchronized (mCanceledSectionIdsSet) {
            mCanceledSectionIdsSet.add(sectionId);
        }
    }

    @Override
    public void removeSectionIdCancel(long sectionId) {
        synchronized (mCanceledSectionIdsSet) {
            mCanceledSectionIdsSet.remove(sectionId);
        }
    }

    @Override
    public boolean isSectionIdIsCanceled(long sectionId) {
        synchronized (mCanceledSectionIdsSet) {
            return mCanceledSectionIdsSet.contains(sectionId);
        }
    }

    @Override
    public void addUnitIdCancel(long unitId) {
        synchronized (mCanceledUnitIdsSet) {
            mCanceledUnitIdsSet.add(unitId);
        }
    }

    @Override
    public void removeUnitIdCancel(long unitId) {
        synchronized (mCanceledUnitIdsSet) {
            mCanceledUnitIdsSet.remove(unitId);
        }
    }

    @Override
    public boolean isUnitIdIsCanceled(long unitId) {
        synchronized (mCanceledUnitIdsSet) {
            return mCanceledUnitIdsSet.contains(unitId);
        }
    }

}