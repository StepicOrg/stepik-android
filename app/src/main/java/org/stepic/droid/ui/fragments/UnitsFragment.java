package org.stepic.droid.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import org.jetbrains.annotations.NotNull;
import org.stepic.droid.R;
import org.stepic.droid.base.FragmentBase;
import org.stepic.droid.core.presenters.UnitsLearningProgressPresenter;
import org.stepic.droid.core.presenters.UnitsPresenter;
import org.stepic.droid.core.presenters.contracts.UnitsLearningProgressView;
import org.stepic.droid.core.presenters.contracts.UnitsView;
import org.stepic.droid.persistence.model.DownloadProgress;
import org.stepik.android.model.Section;
import org.stepic.droid.ui.util.ToolbarHelperKt;

import javax.inject.Inject;

public class UnitsFragment extends FragmentBase implements
        UnitsView,
        UnitsLearningProgressView {

    private static final int ANIMATION_DURATION = 0;
    public static final int DELETE_POSITION_REQUEST_CODE = 165;

    private final static String SECTION_KEY = "section_key";

    public static UnitsFragment newInstance(Section section) {
        Bundle args = new Bundle();
        args.putParcelable(SECTION_KEY, section);
        UnitsFragment fragment = new UnitsFragment();
        fragment.setArguments(args);
        return fragment;
    }


    private Section section;

    @Inject
    UnitsPresenter unitsPresenter;

    @Inject
    UnitsLearningProgressPresenter unitsLearningProgressPresenter;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        hideSoftKeypad();

        ToolbarHelperKt.initCenteredToolbar(this, R.string.units_lessons_title, true);



        unitsPresenter.attachView(this);
        unitsLearningProgressPresenter.attachView(this);
        getLocalProgressManager().subscribe(unitsLearningProgressPresenter);
        unitsPresenter.showUnits(section, false);
    }

    @Override
    public void onDestroyView() {
        getLocalProgressManager().unsubscribe(unitsLearningProgressPresenter);
        unitsLearningProgressPresenter.detachView(this);
        unitsPresenter.detachView(this);

        super.onDestroyView();
    }

    @Override
    public void onEmptyUnits() {
    }

    @Override
    public void onLoading() {
    }

    @Override
    public void onConnectionProblem() {
    }

    @Override
    public void determineNetworkTypeAndLoad(int position) {
    }

    @Override
    public void showOnRemoveDownloadDialog(int position) {
    }

    @Override
    public void setNewScore(long unitId, double newScore) {
    }

    @Override
    public void setUnitPassed(long unitId) {
    }

    @Override
    public void showDownloadProgress(@NotNull DownloadProgress progress) {
    }

    @Override
    public void showVideoQualityDialog(int position) {
    }
}
