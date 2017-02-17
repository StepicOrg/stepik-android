package org.stepic.droid.ui.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.squareup.otto.Subscribe;

import org.stepic.droid.R;
import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.base.FragmentBase;
import org.stepic.droid.base.MainApplication;
import org.stepic.droid.concurrency.tasks.FromDbUnitLessonTask;
import org.stepic.droid.concurrency.tasks.ToDbUnitLessonTask;
import org.stepic.droid.core.LessonSessionManager;
import org.stepic.droid.core.modules.UnitsModule;
import org.stepic.droid.core.presenters.UnitsPresenter;
import org.stepic.droid.core.presenters.contracts.UnitsView;
import org.stepic.droid.events.lessons.SuccessLoadLessonsEvent;
import org.stepic.droid.events.notify_ui.NotifyUIUnitLessonEvent;
import org.stepic.droid.events.units.FailureLoadEvent;
import org.stepic.droid.events.units.LoadedFromDbUnitsLessonsEvent;
import org.stepic.droid.events.units.NotCachedUnitEvent;
import org.stepic.droid.events.units.SuccessLoadUnitsEvent;
import org.stepic.droid.events.units.UnitCachedEvent;
import org.stepic.droid.events.units.UnitLessonSavedEvent;
import org.stepic.droid.events.units.UnitProgressUpdateEvent;
import org.stepic.droid.events.units.UnitScoreUpdateEvent;
import org.stepic.droid.model.Lesson;
import org.stepic.droid.model.Progress;
import org.stepic.droid.model.Section;
import org.stepic.droid.model.Unit;
import org.stepic.droid.ui.adapters.UnitAdapter;
import org.stepic.droid.util.AppConstants;
import org.stepic.droid.util.ProgressHelper;
import org.stepic.droid.util.ProgressUtil;
import org.stepic.droid.util.StepicLogicHelper;
import org.stepic.droid.web.LessonStepicResponse;
import org.stepic.droid.web.ProgressesResponse;
import org.stepic.droid.web.UnitStepicResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import butterknife.BindView;
import kotlin.jvm.functions.Function0;
import retrofit2.Response;

public class UnitsFragment extends FragmentBase implements SwipeRefreshLayout.OnRefreshListener, UnitsView {

    private final static String SECTION_KEY = "section_key";

    public static UnitsFragment newInstance(Section section) {

        Bundle args = new Bundle();
        args.putParcelable(SECTION_KEY, section);
        UnitsFragment fragment = new UnitsFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @BindView(R.id.swipe_refresh_layout_units)
    SwipeRefreshLayout swipeRefreshLayout;

    @BindView(R.id.units_recycler_view)
    RecyclerView unitsRecyclerView;

    @BindView(R.id.load_progressbar)
    ProgressBar progressBar;

    @BindView(R.id.toolbar)
    android.support.v7.widget.Toolbar toolbar;

    @BindView(R.id.report_problem)
    protected View reportConnectionProblem;

    @BindView(R.id.report_empty)
    protected View reportEmpty;

    private Section section;

    @Inject
    UnitsPresenter unitsPresenter;

    @Inject
    LessonSessionManager lessonManager;

    UnitAdapter adapter;


    boolean isScreenEmpty;
    boolean firstLoad;

    private List<Unit> unitList;
    private List<Lesson> lessonList;
    private Map<Long, Progress> progressMap;

    private FromDbUnitLessonTask fromDbUnitLessonTask;
    private ToDbUnitLessonTask toDbUnitLessonTask;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainApplication
                .component()
                .plus(new UnitsModule())
                .inject(this);

        setRetainInstance(true);
        section = getArguments().getParcelable(SECTION_KEY);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_units, container, false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.share_menu, menu);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        hideSoftKeypad();
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(
                R.color.stepic_brand_primary,
                R.color.stepic_orange_carrot,
                R.color.stepic_blue_ribbon);

        isScreenEmpty = true;
        firstLoad = true;


        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        unitsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        unitList = new ArrayList<>();
        lessonList = new ArrayList<>();
        progressMap = new HashMap<>();
        adapter = new UnitAdapter(section, unitList, lessonList, progressMap, (AppCompatActivity) getActivity());
        unitsRecyclerView.setAdapter(adapter);


        ProgressHelper.activate(progressBar);

        bus.register(this);
        getAndShowUnitsFromCache();

        unitsPresenter.attachView(this);
    }

    @Override
    public void onDestroyView() {
        unitsPresenter.detachView(this);

        bus.unregister(this);
        lessonManager.reset();

        super.onDestroyView();
    }

    @Override
    public void onStop() {
        super.onStop();
        ProgressHelper.dismiss(swipeRefreshLayout);
    }

    @Override
    public void onRefresh() {
        analytic.reportEvent(Analytic.Interaction.REFRESH_UNIT);
        ProgressHelper.activate(swipeRefreshLayout);
        updateUnits();
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == AppConstants.REQUEST_EXTERNAL_STORAGE && permissions.length > 0) {
            String permissionExternalStorage = permissions[0];
            if (permissionExternalStorage == null) return;

            if (permissionExternalStorage.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE) &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                int position = shell.getSharedPreferenceHelper().getTempPosition();
                if (adapter != null) {
                    adapter.requestClickLoad(position);
                }
            }
        }
    }

    private void updateState(long unitId, boolean isCached, boolean isLoading) {

        int position = -1;
        Unit unit = null;
        for (int i = 0; i < unitList.size(); i++) {
            if (unitList.get(i).getId() == unitId) {
                position = i;
                unit = unitList.get(i);
                break;
            }
        }
        if (unit == null || position == -1 || position >= unitList.size()) return;

        unit.set_cached(isCached);
        unit.set_loading(isLoading);
        adapter.notifyItemChanged(position);
    }

    @Subscribe
    public void onNotCachedSection(NotCachedUnitEvent e) {
        long unitId = e.getUnitId();
        updateState(unitId, false, false);
    }

    @org.jetbrains.annotations.Nullable
    private Pair<Unit, Integer> getUnitOnScreenAndPositionById(long unitId) {
        int position = -1;
        Unit unit = null;
        for (int i = 0; i < unitList.size(); i++) {
            if (unitList.get(i).getId() == unitId) {
                position = i;
                unit = unitList.get(i);
                break;
            }
        }
        if (unit == null || position == -1 || position >= unitList.size()) return null;
        return new Pair<>(unit, position);
    }

    @Subscribe
    public void onUnitScoreChanged(UnitScoreUpdateEvent event) {
        long unitId = event.getUnitId();

        Pair<Unit, Integer> unitPairPosition = getUnitOnScreenAndPositionById(unitId);
        if (unitPairPosition == null) return;
        Unit unit = unitPairPosition.first;
        int position = unitPairPosition.second;

        Progress progress = progressMap.get(unitId);
        if (progress != null) {
            progress.setScore(event.getNewScore() + "");
        }

        adapter.notifyItemChanged(position);
    }

    @Subscribe
    public void onNotifyUI(NotifyUIUnitLessonEvent event) {
        dismissReport();
        adapter.notifyDataSetChanged();
    }

    @Subscribe
    public void onUnitCachedEvent(UnitCachedEvent e) {
        long unitId = e.getUnitId();

        Pair<Unit, Integer> unitPairPosition = getUnitOnScreenAndPositionById(unitId);
        if (unitPairPosition == null) return;
        Unit unit = unitPairPosition.first;
        int position = unitPairPosition.second;

        //now we have not null unit and correct position at oldList
        unit.set_cached(true);
        unit.set_loading(false);
        adapter.notifyItemChanged(position);
    }

    @Subscribe
    public void onUnitProgressStateChanged(UnitProgressUpdateEvent event) {
        long unitId = event.getUnitId();

        Pair<Unit, Integer> unitPairPosition = getUnitOnScreenAndPositionById(unitId);
        if (unitPairPosition == null) return;
        Unit unit = unitPairPosition.first;
        int position = unitPairPosition.second;

        unit.set_viewed_custom(true);
        adapter.notifyItemChanged(position);
    }

    @Subscribe
    public void onSuccessLoadFromDb(LoadedFromDbUnitsLessonsEvent e) {
        if (section != e.getSection()) return;
        if (e.getUnits() != null && e.getLessons() != null && e.getUnits().size() != 0 && e.getLessons().size() != 0) {
            showUnitsLessons(e.getUnits(), e.getLessons(), e.getProgressMap());
            if (firstLoad) {
                firstLoad = false;
                updateUnits();
            }
        } else {
            //db doesn't have it, load from web with empty screen
            updateUnits();
        }
    }

    @Subscribe
    public void onFinishSaveToDb(UnitLessonSavedEvent e) {
        if (e.getSection() == section) {
            getAndShowUnitsFromCache();
        }
    }

    private void dismissReport() {
        if (lessonList != null && unitList != null && lessonList.size() != 0 && unitList.size() != 0) {
            reportConnectionProblem.setVisibility(View.GONE);
        }
    }

    private void shareSection() {
        if (section != null) {
            Intent intent = shareHelper.getIntentForSectionSharing(section);
            startActivity(intent);
        }
    }


    private void getAndShowUnitsFromCache() {
        fromDbUnitLessonTask = new FromDbUnitLessonTask(section);
        fromDbUnitLessonTask.executeOnExecutor(threadPoolExecutor);
    }

    private void updateUnits() {
        long[] units = section.getUnits();
        if (units == null || units.length == 0) {
            ProgressHelper.dismiss(progressBar);
            ProgressHelper.dismiss(swipeRefreshLayout);
            reportEmpty.setVisibility(View.VISIBLE);
        } else {
            reportEmpty.setVisibility(View.GONE);

            //todo make it in presenter

            threadPoolExecutor.execute(new Runnable() {
                final long[] unitIds = section.getUnits();

                @Override
                public void run() {
                    try {
                        final List<Unit> backgroundUnits = new ArrayList<>();
                        boolean responseIsSuccess = true;
                        if (unitIds == null) {
                            responseIsSuccess = false;
                        }
                        int pointer = 0;
                        while (responseIsSuccess && pointer < unitIds.length) {
                            int lastExclusive = Math.min(unitIds.length, pointer + AppConstants.DEFAULT_NUMBER_IDS_IN_QUERY);
                            long[] subArrayForLoading = Arrays.copyOfRange(unitIds, pointer, lastExclusive);
                            Response<UnitStepicResponse> unitResponse = shell.getApi().getUnits(subArrayForLoading).execute();
                            if (!unitResponse.isSuccessful()) {
                                responseIsSuccess = false;
                            } else {
                                backgroundUnits.addAll(unitResponse.body().getUnits());
                                pointer = lastExclusive;
                            }
                        }

                        if (responseIsSuccess) {
                            mainHandler.post(new Function0<kotlin.Unit>() {
                                @Override
                                public kotlin.Unit invoke() {
                                    bus.post(new SuccessLoadUnitsEvent(section, backgroundUnits)); // we do not use this unit in background threads => send to main without extra copy
                                    return kotlin.Unit.INSTANCE;
                                }
                            });


                        } else {
                            mainHandler.post(new Function0<kotlin.Unit>() {
                                @Override
                                public kotlin.Unit invoke() {
                                    bus.post(new FailureLoadEvent(section));
                                    return kotlin.Unit.INSTANCE;
                                }
                            });
                        }

                    } catch (Exception exception) {
                        mainHandler.post(new Function0<kotlin.Unit>() {
                            @Override
                            public kotlin.Unit invoke() {
                                bus.post(new FailureLoadEvent(section));
                                return kotlin.Unit.INSTANCE;
                            }
                        });
                    }
                }
            });
        }

    }

    @Subscribe
    public void onSuccessLoadUnits(SuccessLoadUnitsEvent e) {
        if (section == null || e.getSection() == null
                || e.getSection().getId() != section.getId())
            return;

        final List<Unit> units = e.getUnitList();

        final long[] lessonsIds = StepicLogicHelper.fromUnitsToLessonIds(units);

        //todo make it in presenter

        threadPoolExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    final List<Lesson> backgroundLessons = new ArrayList<>();
                    boolean responseIsSuccess = true;
                    if (lessonsIds == null) {
                        responseIsSuccess = false;
                    }
                    int pointer = 0;
                    while (responseIsSuccess && pointer < lessonsIds.length) {
                        int lastExclusive = Math.min(lessonsIds.length, pointer + AppConstants.DEFAULT_NUMBER_IDS_IN_QUERY);
                        long[] subArrayForLoading = Arrays.copyOfRange(lessonsIds, pointer, lastExclusive);
                        Response<LessonStepicResponse> lessonsResponse = shell.getApi().getLessons(subArrayForLoading).execute();
                        if (!lessonsResponse.isSuccessful()) {
                            responseIsSuccess = false;
                        } else {
                            backgroundLessons.addAll(lessonsResponse.body().getLessons());
                            pointer = lastExclusive;
                        }
                    }

                    if (responseIsSuccess) {
                        mainHandler.post(new Function0<kotlin.Unit>() {
                            @Override
                            public kotlin.Unit invoke() {
                                bus.post(new SuccessLoadLessonsEvent(section, backgroundLessons, units)); // we do not use this unit in background threads => send to main without extra copy
                                return kotlin.Unit.INSTANCE;
                            }
                        });


                    } else {
                        mainHandler.post(new Function0<kotlin.Unit>() {
                            @Override
                            public kotlin.Unit invoke() {
                                bus.post(new FailureLoadEvent(section));
                                return kotlin.Unit.INSTANCE;
                            }
                        });
                    }

                } catch (Exception exception) {
                    mainHandler.post(new Function0<kotlin.Unit>() {
                        @Override
                        public kotlin.Unit invoke() {
                            bus.post(new FailureLoadEvent(section));
                            return kotlin.Unit.INSTANCE;
                        }
                    });
                }
            }
        });
    }

    @Subscribe
    public void onFinalSuccessDownloadFromWeb(final SuccessLoadLessonsEvent e) {
        if (section == null || e.getSection() == null
                || e.getSection().getId() != section.getId())
            return;

        final String[] progressIds = ProgressUtil.getAllProgresses(e.getUnits());

        threadPoolExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    final List<Progress> backgroundProgress = new ArrayList<>();
                    boolean responseIsSuccess = true;
                    if (progressIds == null) {
                        responseIsSuccess = false;
                    }
                    int pointer = 0;
                    while (responseIsSuccess && pointer < progressIds.length) {
                        int lastExclusive = Math.min(progressIds.length, pointer + AppConstants.DEFAULT_NUMBER_IDS_IN_QUERY);
                        String[] subArrayForLoading = Arrays.copyOfRange(progressIds, pointer, lastExclusive);
                        Response<ProgressesResponse> progressesResponse = shell.getApi().getProgresses(subArrayForLoading).execute();
                        if (!progressesResponse.isSuccessful()) {
                            responseIsSuccess = false;
                        } else {
                            backgroundProgress.addAll(progressesResponse.body().getProgresses());
                            pointer = lastExclusive;
                        }
                    }

                    if (responseIsSuccess) {

                        mainHandler.post(new Function0<kotlin.Unit>() {
                            @Override
                            public kotlin.Unit invoke() {
                                saveToDb(e.getUnits(), e.getLessons(), backgroundProgress);
                                return kotlin.Unit.INSTANCE;
                            }
                        });


                    } else {
                        mainHandler.post(new Function0<kotlin.Unit>() {
                            @Override
                            public kotlin.Unit invoke() {
                                bus.post(new FailureLoadEvent(section));
                                return kotlin.Unit.INSTANCE;
                            }
                        });
                    }

                } catch (Exception exception) {
                    mainHandler.post(new Function0<kotlin.Unit>() {
                        @Override
                        public kotlin.Unit invoke() {
                            bus.post(new FailureLoadEvent(section));
                            return kotlin.Unit.INSTANCE;
                        }
                    });
                }
            }
        });
    }

    private void saveToDb(List<Unit> unitList, List<Lesson> lessonList, List<Progress> progresses) {
        toDbUnitLessonTask = new ToDbUnitLessonTask(section, unitList, lessonList, progresses);
        toDbUnitLessonTask.executeOnExecutor(threadPoolExecutor);
    }

    private void showUnitsLessons(List<Unit> units, List<Lesson> lessons, Map<Long, Progress> longProgressMap) {

        lessonList.clear();
        lessonList.addAll(lessons);

        unitList.clear();
        unitList.addAll(units);

        progressMap.clear();
        progressMap.putAll(longProgressMap);

        dismissReport();
        adapter.notifyDataSetChanged();

        dismiss();
    }


    @Subscribe
    public void onFailLoad(FailureLoadEvent e) {
        if (section == null || e.getSection() == null
                || e.getSection().getId() != section.getId())
            return;

        if (unitList != null && unitList.size() == 0) {
            reportConnectionProblem.setVisibility(View.VISIBLE);
        }
        dismiss();
    }

    private void dismiss() {
        if (isScreenEmpty) {
            ProgressHelper.dismiss(progressBar);
            isScreenEmpty = false;
        } else {
            ProgressHelper.dismiss(swipeRefreshLayout);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_share:
                shareSection();
                return true;
        }
        return false;
    }
}
