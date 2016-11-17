package org.stepic.droid.ui.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Checkable;

import org.stepic.droid.R;
import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.base.FragmentBase;
import org.stepic.droid.base.MainApplication;
import org.stepic.droid.core.modules.FilterModule;
import org.stepic.droid.core.presenters.FilterPresenter;
import org.stepic.droid.core.presenters.contracts.FilterView;
import org.stepic.droid.model.StepikFilter;
import org.stepic.droid.store.operations.Table;
import org.stepic.droid.util.AppConstants;

import java.util.EnumSet;

import javax.inject.Inject;

import butterknife.BindView;

public class FilterFragment extends FragmentBase implements FilterView {

    private static final String filterCourseTypeKey = "filter_course_type";

    public static FilterFragment newInstance(int filterCourseTypeCode) {
        Bundle args = new Bundle();
        args.putInt(filterCourseTypeKey, filterCourseTypeCode);
        FilterFragment fragment = new FilterFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.filter_accept_button)
    View acceptButton;

    @BindView(R.id.filter_cancel_button)
    View cancelButton;

    @BindView(R.id.language_ru_courses)
    CheckBox languageRuCheckBox;

    @BindView(R.id.language_en_courses)
    CheckBox languageEnCheckBox;

    @BindView(R.id.timing_upcoming)
    CheckBox upcomingCheckBox;

    @BindView(R.id.timing_active)
    CheckBox activeCheckBox;

    @BindView(R.id.timing_past)
    CheckBox pastCheckBox;

    @BindView(R.id.filter_persistent_switch)
    CheckBox persistentCheckBox;

    @Inject
    FilterPresenter filterPresenter;

    Table courseType;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);

        int filterCode = getArguments().getInt(filterCourseTypeKey);
        if (filterCode == AppConstants.ENROLLED_FILTER) {
            courseType = Table.enrolled;
        } else if (filterCode == AppConstants.FEATURED_FILTER) {
            courseType = Table.featured;
        }

        MainApplication
                .component()
                .plus(new FilterModule())
                .inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_filter, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initToolbar();

        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                analytic.reportEvent(Analytic.Interaction.CLICK_ACCEPT_FILTER_BUTTON);
                filterPresenter.acceptFilter(getCurrentFilterFromUI(), courseType);
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().finish();
            }
        });
        filterPresenter.attachView(this);
        filterPresenter.initFiltersIfNeed(courseType);
    }

    private void initToolbar() {
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_white_24dp);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.filter_accept_menu, menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Respond to the action bar's Up/Home button
                getActivity().finish();
                return true;
            case R.id.accept_action:
                filterPresenter.acceptFilter(getCurrentFilterFromUI(), courseType);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onDestroyView() {
        filterPresenter.detachView(this);
        super.onDestroyView();
    }

    private void applyFilterToView(EnumSet<StepikFilter> filters, StepikFilter stepikFilterValue, Checkable checkable) {
        checkable.setChecked(filters.contains(stepikFilterValue));
    }

    private EnumSet<StepikFilter> getCurrentFilterFromUI() {
        EnumSet<StepikFilter> filter = EnumSet.noneOf(StepikFilter.class);
        appendToFilter(filter, StepikFilter.RUSSIAN, languageRuCheckBox.isChecked());
        appendToFilter(filter, StepikFilter.ENGLISH, languageEnCheckBox.isChecked());
        appendToFilter(filter, StepikFilter.UPCOMING, upcomingCheckBox.isChecked());
        appendToFilter(filter, StepikFilter.ACTIVE, activeCheckBox.isChecked());
        appendToFilter(filter, StepikFilter.PAST, pastCheckBox.isChecked());
        appendToFilter(filter, StepikFilter.PERSISTENT, persistentCheckBox.isChecked());
        return filter;
    }

    private void appendToFilter(EnumSet<StepikFilter> filter, StepikFilter stepikFilterValue, boolean needAppend) {
        if (needAppend) {
            filter.add(stepikFilterValue);
        }
    }

    @Override
    public void onFilterAccepted() {
        //filter is accepted, close
        getActivity().setResult(Activity.RESULT_OK);
        getActivity().finish();
    }

    @Override
    public void onFiltersPreparedForView(EnumSet<StepikFilter> filters) {
        //set filter, when it is prepared:
        applyFilterToView(filters, StepikFilter.RUSSIAN, languageRuCheckBox);
        applyFilterToView(filters, StepikFilter.ENGLISH, languageEnCheckBox);
        applyFilterToView(filters, StepikFilter.UPCOMING, upcomingCheckBox);
        applyFilterToView(filters, StepikFilter.ACTIVE, activeCheckBox);
        applyFilterToView(filters, StepikFilter.PAST, pastCheckBox);
        applyFilterToView(filters, StepikFilter.PERSISTENT, persistentCheckBox);
    }
}
