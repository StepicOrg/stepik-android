package org.stepic.droid.ui.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Checkable;

import org.stepic.droid.R;
import org.stepic.droid.base.FragmentBase;
import org.stepic.droid.model.StepikFilter;

import java.util.EnumSet;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FilterFragment extends FragmentBase {

    public static FilterFragment newInstance() {
        return new FilterFragment();
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
    SwitchCompat persistentSwitch;

    private boolean isInitiated = false; //todo move to presenter

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_filter, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        initToolbar();

        //todo Move to presenter:
        if (!isInitiated) {
            initFilter(mSharedPreferenceHelper.getFilterAndClearNotPersistent());
            isInitiated = true;
        }

        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                acceptFilter();
                getActivity().finish();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().finish();
            }
        });
    }

    private void initToolbar() {
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_white_24dp);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Respond to the action bar's Up/Home button
                getActivity().finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initFilter(EnumSet<StepikFilter> filters) {
        applyFilterToView(filters, StepikFilter.RUSSIAN, languageRuCheckBox);
        applyFilterToView(filters, StepikFilter.ENGLISH, languageEnCheckBox);
        applyFilterToView(filters, StepikFilter.UPCOMING, upcomingCheckBox);
        applyFilterToView(filters, StepikFilter.ACTIVE, activeCheckBox);
        applyFilterToView(filters, StepikFilter.PAST, pastCheckBox);
        applyFilterToView(filters, StepikFilter.PERSISTENT, persistentSwitch);
    }

    private void applyFilterToView(EnumSet<StepikFilter> filters, StepikFilter stepikFilterValue, Checkable checkable) {
        if (filters.contains(stepikFilterValue)) {
            checkable.setChecked(true);
        }
    }

    //todo move method to presenter
    private void acceptFilter() {
        mSharedPreferenceHelper.saveFilter(getCurrentFilterFromUI());//todo move to presenter
        getActivity().setResult(Activity.RESULT_OK);
    }

    private EnumSet<StepikFilter> getCurrentFilterFromUI() {
        EnumSet<StepikFilter> filter = EnumSet.noneOf(StepikFilter.class);
        appendToFilter(filter, StepikFilter.RUSSIAN, languageRuCheckBox.isChecked());
        appendToFilter(filter, StepikFilter.ENGLISH, languageEnCheckBox.isChecked());
        appendToFilter(filter, StepikFilter.UPCOMING, upcomingCheckBox.isChecked());
        appendToFilter(filter, StepikFilter.ACTIVE, activeCheckBox.isChecked());
        appendToFilter(filter, StepikFilter.PAST, pastCheckBox.isChecked());
        appendToFilter(filter, StepikFilter.PERSISTENT, persistentSwitch.isChecked());
        return filter;
    }

    private void appendToFilter(EnumSet<StepikFilter> filter, StepikFilter stepikFilterValue, boolean needAppend) {
        if (needAppend) {
            filter.add(stepikFilterValue);
        }
    }
}
