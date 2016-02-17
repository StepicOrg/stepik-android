package org.stepic.droid.view.adapters;

import android.content.Context;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.facebook.common.util.UriUtil;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.DraweeView;

import org.jetbrains.annotations.Nullable;
import org.stepic.droid.R;
import org.stepic.droid.base.MainApplication;
import org.stepic.droid.configuration.IConfig;
import org.stepic.droid.core.IShell;
import org.stepic.droid.model.Course;
import org.stepic.droid.store.CleanManager;
import org.stepic.droid.store.IDownloadManager;
import org.stepic.droid.store.operations.DatabaseFacade;
import org.stepic.droid.util.HtmlHelper;

import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MyCoursesAdapter extends ArrayAdapter<Course> {

    @Inject
    IShell mShell;

    @Inject
    IConfig mConfig;

    @Inject
    DatabaseFacade mDatabase;

    @Inject
    IDownloadManager mDownloadManager;

    @Inject
    CleanManager mCleaner;

    private Fragment mFragment;
    @Nullable
    private final DatabaseFacade.Table type;
    private LayoutInflater mInflater;

    public MyCoursesAdapter(Fragment fragment, List<Course> courses, @Nullable DatabaseFacade.Table type) {
        super(fragment.getActivity(), 0, courses);
        mFragment = fragment;
        this.type = type;
        mInflater = (LayoutInflater) mFragment.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        MainApplication.component().inject(this);

    }

    public MyCoursesAdapter(Fragment fragment, List<Course> courses) {
        this(fragment, courses, null);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final Course course = getItem(position);

        View view = convertView;
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolderItem viewHolderItem = null;
        if (view == null) {
            view = mInflater.inflate(R.layout.course_item, null);
            viewHolderItem = new ViewHolderItem(view);
            view.setTag(viewHolderItem);
        } else {
            viewHolderItem = (ViewHolderItem) convertView.getTag();
        }
        viewHolderItem.courseName.setText(course.getTitle());
        viewHolderItem.courseSummary.setText(HtmlHelper.fromHtml(course.getSummary()));
        if (course.getCover() != null) {
            DraweeController controller = Fresco.newDraweeControllerBuilder()
                    .setUri(mConfig.getBaseUrl() + course.getCover())
                    .setAutoPlayAnimations(true)
                    .build();
            viewHolderItem.courseIcon.setController(controller);
        } else {
            //for empty cover:
            Uri uri = new Uri.Builder()
                    .scheme(UriUtil.LOCAL_RESOURCE_SCHEME) // "res"
                    .path(String.valueOf(R.drawable.ic_course_placeholder))
                    .build();

            DraweeController controller = Fresco.newDraweeControllerBuilder()
                    .setUri(uri)
                    .setAutoPlayAnimations(true)
                    .build();
            viewHolderItem.courseIcon.setController(controller);
        }
        viewHolderItem.courseDateInterval.setText(course.getDateOfCourse());

//        viewHolderItem.cardView.setListenerToActionButton(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (course.getEnrollment() != 0) {
//                    mShell.getScreenProvider().showSections(mFragment.getActivity(), course);
//                } else {
//                    mShell.getScreenProvider().showCourseDescription(mFragment, course);
//                }
//            }
//        });

//        if (type == DatabaseFacade.Table.enrolled) {
//            viewHolderItem.loadButton.setVisibility(View.VISIBLE);
//            //cached/loading
//            //false/false = show sky with suggestion for cache
//            //false/true = show progress
//            //true/false = show can with suggestion for delete
//            //true/true = impossible
//            if (course.is_cached()) {
//                //cached
//                viewHolderItem.loadButton.setListenerToActionButton(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        int permissionCheck = ContextCompat.checkSelfPermission(MainApplication.getAppContext(),
//                                Manifest.permission.WRITE_EXTERNAL_STORAGE);
//
//                        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
//                            removeCourse(course);
//                        } else {
//
//                        }
//                    }
//                });
//
//                viewHolderItem.preLoadIV.setVisibility(View.GONE);
//                viewHolderItem.whenLoad.setVisibility(View.INVISIBLE);
//                viewHolderItem.afterLoad.setVisibility(View.VISIBLE); //can
//
//            } else {
//                if (course.is_loading()) {
//
//                    viewHolderItem.preLoadIV.setVisibility(View.GONE);
//                    viewHolderItem.whenLoad.setVisibility(View.VISIBLE);
//                    viewHolderItem.afterLoad.setVisibility(View.GONE);
//
//                    //todo: add cancel of downloading
//                } else {
//                    //not cached not loading
//                    viewHolderItem.preLoadIV.setVisibility(View.VISIBLE);
//                    viewHolderItem.whenLoad.setVisibility(View.INVISIBLE);
//                    viewHolderItem.afterLoad.setVisibility(View.GONE);
//
//
//                    viewHolderItem.loadButton.setListenerToActionButton(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            int permissionCheck = ContextCompat.checkSelfPermission(MainApplication.getAppContext(),
//                                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
//
//                            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
//                                cacheCourse(course);
//                            } else {
//                                if (ActivityCompat.shouldShowRequestPermissionRationale(mFragment,
//                                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
//
//                                    // Show an expanation to the user *asynchronously* -- don't block
//                                    // this thread waiting for the user's response! After the user
//                                    // sees the explanation, try again to request the permission.
//
//                                    ExplainPermissionDialog dialog = new ExplainPermissionDialog();
//                                    dialog.show(mFragment.getFragmentManager(), null);
//
//                                } else {
//
//                                    // No explanation needed, we can request the permission.
//
//                                    ActivityCompat.requestPermissions(mFragment,
//                                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
//                                            AppConstants.REQUEST_WIFI);
//
//                                    // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
//                                    // app-defined int constant. The callback method gets the
//                                    // result of the request.
//                                }
//
//                            }
//                        }
//                    });
//                }
//            }
//        } else {
//            viewHolderItem.preLoadIV.setVisibility(View.GONE);
//            viewHolderItem.whenLoad.setVisibility(View.GONE);
//            viewHolderItem.afterLoad.setVisibility(View.GONE);
//            viewHolderItem.loadButton.setVisibility(View.GONE);
//        }
        return view;
    }

//    private void removeCourse(Course course) {
//
//        YandexMetrica.reportEvent(AppConstants.METRICA_CLICK_DELETE_COURSE, JsonHelper.toJson(course));
//        mCleaner.removeCourse(course, type);
//        course.setIs_cached(false);
//        course.setIs_loading(false);
//        mDatabase.updateOnlyCachedLoadingCourse(course, type);
//        notifyDataSetChanged();
//    }

//    private void cacheCourse(Course course) {
//        // FIXME: 21.10.15 IMPLEMENTS IN BACKGROUND THREAD
//        YandexMetrica.reportEvent(AppConstants.METRICA_CLICK_CACHE_COURSE, JsonHelper.toJson(course));
//        mDownloadManager.addCourse(course, type);
//        course.setIs_loading(true);
//        course.setIs_cached(false);
//        mDatabase.updateOnlyCachedLoadingCourse(course, type);
//        notifyDataSetChanged();
//    }

    static class ViewHolderItem {


        @Bind(R.id.course_name)
        TextView courseName;

        @Bind(R.id.course_info)
        TextView courseSummary;

        @Bind(R.id.video_icon)
        DraweeView courseIcon;

        @Bind(R.id.course_date_interval)
        TextView courseDateInterval;

        @Bind(R.id.cv)
        View cardView;

        public ViewHolderItem(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
