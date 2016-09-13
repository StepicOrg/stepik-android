package org.stepic.droid.store.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.jetbrains.annotations.Nullable;
import org.stepic.droid.model.ActionsContainer;
import org.stepic.droid.model.Assignment;
import org.stepic.droid.model.BlockPersistentWrapper;
import org.stepic.droid.model.Progress;
import org.stepic.droid.model.Step;
import org.stepic.droid.store.structure.DbStructureAssignment;
import org.stepic.droid.store.structure.DbStructureBlock;
import org.stepic.droid.store.structure.DbStructureProgress;
import org.stepic.droid.store.structure.DbStructureStep;
import org.stepic.droid.util.DbParseHelper;

import java.util.List;

public class StepDaoImpl extends DaoBase<Step> {


    private final IDao<BlockPersistentWrapper> mBlockWrapperDao;
    private final IDao<Assignment> mAssignmentDao;
    private final IDao<Progress> mProgressDao;

    public StepDaoImpl(SQLiteDatabase openHelper,
                       IDao<BlockPersistentWrapper> blockWrapperDao,
                       IDao<Assignment> assignmentDao,
                       IDao<Progress> progressDao) {
        super(openHelper);
        mBlockWrapperDao = blockWrapperDao;
        mAssignmentDao = assignmentDao;
        mProgressDao = progressDao;
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


        step.setDiscussions_count(cursor.getInt(columnIndexDiscussionCount));
        step.setDiscussion_proxy(cursor.getString(columnIndexDiscussionId));
        step.setId(cursor.getLong(columnIndexStepId));
        step.setLesson(cursor.getLong(columnIndexLessonId));
        step.setCreate_date(cursor.getString(columnIndexCreateDate));
        step.setCreate_date(cursor.getString(columnIndexStatus));
        step.setProgress(cursor.getString(columnIndexProgress));
        step.setViewed_by(cursor.getLong(columnIndexViewedBy));
        step.setPassed_by(cursor.getLong(columnIndexPassedBy));
        step.setUpdate_date(cursor.getString(columnIndexUpdateDate));
        step.setSubscriptions(DbParseHelper.INSTANCE.parseStringToStringArray(cursor.getString(columnIndexSubscriptions)));
        step.setPosition(cursor.getLong(columnIndexPosition));
        step.set_cached(cursor.getInt(columnIndexIsCached) > 0);
        step.set_loading(cursor.getInt(columnIndexIsLoading) > 0);

        String review = cursor.getString(columnIndexPeerReview);

        ActionsContainer actionsContainer = new ActionsContainer();
        actionsContainer.setDo_review(review);
        step.setActions(actionsContainer);

//        step.setIs_custom_passed(isAssignmentByStepViewed(step.getId()));
        return step;
    }

    @Override
    public ContentValues getContentValues(Step step) {
        ContentValues values = new ContentValues();

        values.put(DbStructureStep.Column.STEP_ID, step.getId());
        values.put(DbStructureStep.Column.LESSON_ID, step.getLesson());
        values.put(DbStructureStep.Column.STATUS, step.getStatus());
        values.put(DbStructureStep.Column.PROGRESS, step.getProgress());
        values.put(DbStructureStep.Column.SUBSCRIPTIONS, DbParseHelper.INSTANCE.parseStringArrayToString(step.getSubscriptions()));
        values.put(DbStructureStep.Column.VIEWED_BY, step.getViewed_by());
        values.put(DbStructureStep.Column.PASSED_BY, step.getPassed_by());
        values.put(DbStructureStep.Column.CREATE_DATE, step.getCreate_date());
        values.put(DbStructureStep.Column.UPDATE_DATE, step.getUpdate_date());
        values.put(DbStructureStep.Column.POSITION, step.getPosition());
        values.put(DbStructureStep.Column.DISCUSSION_COUNT, step.getDiscussions_count());
        values.put(DbStructureStep.Column.DISCUSSION_ID, step.getDiscussion_proxy());

        if (step.getActions() != null) {
            values.put(DbStructureStep.Column.PEER_REVIEW, step.getActions().getDo_review());
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
    public Step get(String whereColumn, String whereValue) {
        Step step = super.get(whereColumn, whereValue);
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
                mBlockWrapperDao.get(DbStructureBlock.Column.STEP_ID, step.getId() + "");
        if (blockPersistentWrapper != null && blockPersistentWrapper.getBlock() != null) {
            step.setBlock(blockPersistentWrapper.getBlock());
        }

        Assignment assignment = mAssignmentDao.get(DbStructureAssignment.Column.STEP_ID, step.getId() + "");
        if (assignment != null && assignment.getProgressId() != null) {
            Progress progress = mProgressDao.get(DbStructureProgress.Column.ID, assignment.getProgressId());
            if (progress != null) {
                step.set_custom_passed(progress.is_passed());
            }
        } else {
            if (step.getProgressId() != null) {
                Progress progress = mProgressDao.get(DbStructureProgress.Column.ID, step.getProgressId());
                if (progress != null) {
                    step.set_custom_passed(progress.is_passed());
                }
            }
        }

    }

    @Override
    public void insertOrUpdate(Step persistentObject) {
        super.insertOrUpdate(persistentObject);
        if (persistentObject != null) {
            mBlockWrapperDao.insertOrUpdate(new BlockPersistentWrapper(persistentObject.getBlock(), persistentObject.getId()));
        }
    }

}
