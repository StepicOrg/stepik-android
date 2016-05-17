package org.stepic.droid.store;

import android.util.Log;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;

public class ConcurrentCancelSniffer implements ICancelSniffer {

    private final Set<Long> mCanceledStepIdsSet;
    private final Set<Long> mCanceledSectionIdsSet;
    private final Set<Long> mCanceledUnitIdsSet;

    @Inject
    public ConcurrentCancelSniffer() {
        mCanceledStepIdsSet = Collections.newSetFromMap(new ConcurrentHashMap<Long, Boolean>());
        mCanceledSectionIdsSet = Collections.newSetFromMap(new ConcurrentHashMap<Long, Boolean>());
        mCanceledUnitIdsSet = Collections.newSetFromMap(new ConcurrentHashMap<Long, Boolean>());
    }

    @Override
    public void addStepIdCancel(long stepId) {
        mCanceledStepIdsSet.add(stepId);
    }

    @Override
    public void removeStepIdCancel(long stepId) {
        Log.d("eee", "remove stepid sniffer: " + stepId);
        mCanceledStepIdsSet.remove(stepId);
    }

    @Override
    public boolean isStepIdCanceled(long stepId) {
        return mCanceledStepIdsSet.contains(stepId);
    }

    @Override
    public void addSectionIdCancel(long sectionId) {
        mCanceledSectionIdsSet.add(sectionId);
    }

    @Override
    public void removeSectionIdCancel(long sectionId) {
        mCanceledSectionIdsSet.remove(sectionId);
    }

    @Override
    public boolean isSectionIdIsCanceled(long sectionId) {
        return mCanceledSectionIdsSet.contains(sectionId);
    }

    @Override
    public void addUnitIdCancel(long unitId) {
        mCanceledUnitIdsSet.add(unitId);
    }

    @Override
    public void removeUnitIdCancel(long unitId) {
        mCanceledUnitIdsSet.remove(unitId);
    }

    @Override
    public boolean isUnitIdIsCanceled(long unitId) {
        return mCanceledUnitIdsSet.contains(unitId);
    }
}
