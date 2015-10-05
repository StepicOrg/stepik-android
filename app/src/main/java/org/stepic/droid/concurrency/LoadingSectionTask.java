//package org.stepic.droid.concurrency;
//
//import android.content.Context;
//
//import org.stepic.droid.base.MainApplication;
//import org.stepic.droid.core.IShell;
//import org.stepic.droid.model.Section;
//import org.stepic.droid.store.operations.DbOperationsSections;
//import org.stepic.droid.web.IApi;
//import org.stepic.droid.web.SectionsStepicResponse;
//
//import java.sql.SQLException;
//import java.util.ArrayList;
//import java.util.List;
//
//import javax.inject.Inject;
//
//public class LoadingSectionTask extends StepicTask<Void, Void, List<Section>> {
//    private long[] mSectionIds;
//
//    @Inject
//    IShell mShell;
//
//
//    public LoadingSectionTask(Context context, long[] sectionIds) {
//        super(context);
//        mSectionIds = sectionIds;
//        MainApplication.component(mContext).inject(this);
//
//    }
//
//    @Override
//    protected List<Section> doInBackgroundBody(Void... params) throws Exception {
//        IApi api = mShell.getApi();
//        List<Section> sectionList = null;
//        SectionsStepicResponse stepicResponse = null;
//        try {
//            stepicResponse = api.getSections(mSectionIds);
//            sectionList = stepicResponse.getSections();
//        } finally {
//            if (sectionList != null) {
//                DbOperationsSections dbOperationsSections = mShell.getDbOperationsSection();
//
//                try {
//                    dbOperationsSections.open();
//                } catch (SQLException e) {
//                    e.printStackTrace();
//                    //todo: if db is not exist app will crash.
//                }
//                try {
//                    for (Section section : sectionList) {
//                        if (!dbOperationsSections.isSectionInDb(section)) {
//                            dbOperationsSections.addSection(section);//add new to persistent cache
//                        }
//                    }
//                } finally {
//                    dbOperationsSections.close();
//                }
//                //all courses are cached now
//            }
//
//            sectionList = getCachedSectionsForCourse(); //get from cache;
//        }
//        return sectionList;
//    }
//
//    private List<Section> getCachedSectionsForCourse() {
//        DbOperationsSections dbOperationSections = mShell.getDbOperationsSection();
//
//        try {
//            dbOperationSections.open();
//        } catch (SQLException e) {
//            e.printStackTrace();
//            return null;
//        }
//        List<Section> cachedCourses = dbOperationSections.getAllSections();
//
//        //section for course about 1-10. but in cache we can have more (100-1000).
//        //todo: determine cache have id for O(1), might change DbOperationsSection
//        List<Section> resultForCourse = new ArrayList<>();
//        for (Section sectionItem : cachedCourses) {
//            for (long id : mSectionIds) {
//                if (sectionItem.getId() == id) {
//                    resultForCourse.add(sectionItem);
//                    break;
//                }
//            }
//        }
//        dbOperationSections.close();
//
//        return resultForCourse;
//
//    }
//}
