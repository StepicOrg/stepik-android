package org.stepic.droid.store;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SynchronizedCancelSniffer implements ICancelSniffer {

    private final Set<Long> mCanceledStepIdsSet;
//    private final Set<Long> mCanceledLessonIdsSet;

    @Inject
    public SynchronizedCancelSniffer() {
        mCanceledStepIdsSet = new HashSet<>();
//        mCanceledLessonIdsSet = new HashSet<>();
    }

    @Override
    public synchronized void addStepIdCancel(long stepId) {
        mCanceledStepIdsSet.add(stepId);
    }

    @Override
    public synchronized void removeStepIdCancel(long stepId) {
        mCanceledStepIdsSet.remove(stepId);
    }

    @Override
    public synchronized boolean isStepIdCanceled(long stepId) {
        return mCanceledStepIdsSet.contains(stepId);
    }

//    @Override
//    public synchronized void addLessonIdCancel(long lessonId) {
//        mCanceledLessonIdsSet.add(lessonId);
//    }
//
//    @Override
//    public synchronized void removeLessonIdCancel(long lessonId) {
//        mCanceledLessonIdsSet.remove(lessonId);
//    }
//
//    @Override
//    public synchronized boolean isLessonIdCanceled(long lessonId) {
//        return mCanceledLessonIdsSet.contains(lessonId);
//    }
}
