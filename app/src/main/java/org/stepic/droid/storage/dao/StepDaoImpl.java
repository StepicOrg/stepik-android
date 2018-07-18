package org.stepic.droid.storage.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import org.jetbrains.annotations.Nullable;
import org.stepik.android.model.learning.Assignment;
import org.stepic.droid.model.BlockPersistentWrapper;
import org.stepik.android.model.structure.Progress;
import org.stepic.droid.model.Step;
import org.stepic.droid.model.StepStatus;
import org.stepic.droid.storage.operations.DatabaseOperations;
import org.stepic.droid.storage.structure.DbStructureAssignment;
import org.stepic.droid.storage.structure.DbStructureBlock;
import org.stepic.droid.storage.structure.DbStructureProgress;
import org.stepic.droid.storage.structure.DbStructureStep;
import org.stepic.droid.util.DbParseHelper;
import org.stepik.android.model.actions.Actions;

import java.util.List;

import javax.inject.Inject;

public class StepDaoImpl extends DaoBase<Step> {


    private final IDao<BlockPersistentWrapper> blockWrapperDao;
    private final IDao<Assignment> assignmentDao;
    private final IDao<Progress> progressDao;

    @Inject
    public StepDaoImpl(DatabaseOperations databaseOperations,
                       IDao<BlockPersistentWrapper> blockWrapperDao,
                       IDao<Assignment> assignmentDao,
                       IDao<Progress> progressDao) {
        super(databaseOperations);
        this.blockWrapperDao = blockWrapperDao;
        this.assignmentDao = assignmentDao;
        this.progressDao = progressDao;
    }

    @Override
    public Step parsePersistentObject(Cursor cursor) {
        Step step = new Step();

        int columnIndexCreateDate = cursor.getColumnIndex(DbStructureStep.Column.CREATE_DATE);
        int columnIndexStepId = cursor.getColumnIndex(DbStructureStep.Column.STEP_ID);
        int columnIndexLessonId = cursor.getColumnIndex(DbStructureStep.Column.LESSON_ID);
        int columnIndexStatus = cursor.getColumnIndex(DbStructureStep.Column.STATUS);
        int columnIndexProgress = cursor.getColumnIndex(DbStructureStep.Column.PROGRESS);
        int columnIndexViewedBy = cursor.getColumnIndex(DbStructureStep.Column.VIEWED_BY);
        int columnIndexPassedBy = cursor.getColumnIndex(DbStructureStep.Column.PASSED_BY);
        int columnIndexUpdateDate = cursor.getColumnIndex(DbStructureStep.Column.UPDATE_DATE);
        int columnIndexSubscriptions = cursor.getColumnIndex(DbStructureStep.Column.SUBSCRIPTIONS);
        int columnIndexPosition = cursor.getColumnIndex(DbStructureStep.Column.POSITION);
        int columnIndexIsCached = cursor.getColumnIndex(DbStructureStep.Column.IS_CACHED);
        int columnIndexIsLoading = cursor.getColumnIndex(DbStructureStep.Column.IS_LOADING);
        int columnIndexDiscussionCount = cursor.getColumnIndex(DbStructureStep.Column.DISCUSSION_COUNT);
        int columnIndexDiscussionId = cursor.getColumnIndex(DbStructureStep.Column.DISCUSSION_ID);
        int columnIndexPeerReview = cursor.getColumnIndex(DbStructureStep.Column.PEER_REVIEW);
        int indexHasSubmissionRestriction = cursor.getColumnIndex(DbStructureStep.Column.HAS_SUBMISSION_RESTRICTION);
        int indexMaxSubmission = cursor.getColumnIndex(DbStructureStep.Column.MAX_SUBMISSION_COUNT);


        step.setDiscussions_count(cursor.getInt(columnIndexDiscussionCount));
        step.setDiscussion_proxy(cursor.getString(columnIndexDiscussionId));
        step.setId(cursor.getLong(columnIndexStepId));
        step.setLesson(cursor.getLong(columnIndexLessonId));
        step.setCreate_date(cursor.getString(columnIndexCreateDate));
        step.setStatus(StepStatus.Helper.INSTANCE.byName(cursor.getString(columnIndexStatus)));
        step.setProgress(cursor.getString(columnIndexProgress));
        step.setViewed_by(cursor.getLong(columnIndexViewedBy));
        step.setPassed_by(cursor.getLong(columnIndexPassedBy));
        step.setUpdate_date(cursor.getString(columnIndexUpdateDate));
        step.setSubscriptions(DbParseHelper.parseStringToStringArray(cursor.getString(columnIndexSubscriptions)));
        step.setPosition(cursor.getLong(columnIndexPosition));
        step.set_cached(cursor.getInt(columnIndexIsCached) > 0);
        step.set_loading(cursor.getInt(columnIndexIsLoading) > 0);
        step.setHasSubmissionRestriction(cursor.getInt(indexHasSubmissionRestriction) > 0);
        step.setMaxSubmissionCount(cursor.getInt(indexMaxSubmission));

        String review = cursor.getString(columnIndexPeerReview);

        Actions actions = new Actions(false, false, null, review, null);
        step.setActions(actions);

//        step.setIs_custom_passed(isAssignmentByStepViewed(step.getId()));
        return step;
    }

    @Override
    public ContentValues getContentValues(Step step) {
        ContentValues values = new ContentValues();

        values.put(DbStructureStep.Column.STEP_ID, step.getId());
        values.put(DbStructureStep.Column.LESSON_ID, step.getLesson());
        values.put(DbStructureStep.Column.STATUS, step.getStatus() == null ? null : step.getStatus().name());
        values.put(DbStructureStep.Column.PROGRESS, step.getProgress());
        values.put(DbStructureStep.Column.SUBSCRIPTIONS, DbParseHelper.parseStringArrayToString(step.getSubscriptions()));
        values.put(DbStructureStep.Column.VIEWED_BY, step.getViewed_by());
        values.put(DbStructureStep.Column.PASSED_BY, step.getPassed_by());
        values.put(DbStructureStep.Column.CREATE_DATE, step.getCreate_date());
        values.put(DbStructureStep.Column.UPDATE_DATE, step.getUpdate_date());
        values.put(DbStructureStep.Column.POSITION, step.getPosition());
        values.put(DbStructureStep.Column.DISCUSSION_COUNT, step.getDiscussions_count());
        values.put(DbStructureStep.Column.DISCUSSION_ID, step.getDiscussion_proxy());
        values.put(DbStructureStep.Column.HAS_SUBMISSION_RESTRICTION, step.getHasSubmissionRestriction());
        values.put(DbStructureStep.Column.MAX_SUBMISSION_COUNT, step.getMaxSubmissionCount());

        if (step.getActions() != null) {
            values.put(DbStructureStep.Column.PEER_REVIEW, step.getActions().getDoReview());
        }

        return values;
    }

    @Override
    public String getDbName() {
        return DbStructureStep.STEPS;
    }

    @Override
    public String getDefaultPrimaryColumn() {
        return DbStructureStep.Column.STEP_ID;
    }

    @Override
    public String getDefaultPrimaryValue(Step persistentObject) {
        return persistentObject.getId() + "";
    }

    @Nullable
    @Override
    public Step get(@NonNull String whereColumnName, @NonNull String whereValue) {
        Step step = super.get(whereColumnName, whereValue);
        addInnerObjects(step);
        return step;
    }

    @Override
    protected List<Step> getAllWithQuery(String query, String[] whereArgs) {
        List<Step> stepList = super.getAllWithQuery(query, whereArgs);
        for (Step step : stepList) {
            addInnerObjects(step);
        }
        return stepList;
    }

    private void addInnerObjects(Step step) {
        if (step == null) return;
        BlockPersistentWrapper blockPersistentWrapper =
                blockWrapperDao.get(DbStructureBlock.Column.STEP_ID, step.getId() + "");
        if (blockPersistentWrapper != null) {
            step.setBlock(blockPersistentWrapper.getBlock());
        }

        Assignment assignment = assignmentDao.get(DbStructureAssignment.Column.STEP_ID, step.getId() + "");
        if (assignment != null && assignment.getProgress() != null) {
            Progress progress = progressDao.get(DbStructureProgress.Column.ID, assignment.getProgress());
            if (progress != null) {
                step.set_custom_passed(progress.isPassed());
            }
        } else {
            if (step.getProgressId() != null) {
                Progress progress = progressDao.get(DbStructureProgress.Column.ID, step.getProgressId());
                if (progress != null) {
                    step.set_custom_passed(progress.isPassed());
                }
            }
        }

    }

    @Override
    public void insertOrUpdate(Step persistentObject) {
        super.insertOrUpdate(persistentObject);
        if (persistentObject != null && persistentObject.getBlock() != null) {
            blockWrapperDao.insertOrUpdate(new BlockPersistentWrapper(persistentObject.getBlock(), persistentObject.getId()));
        }
    }

}
