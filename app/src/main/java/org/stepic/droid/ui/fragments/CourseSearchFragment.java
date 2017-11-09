package org.stepic.droid.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import org.stepic.droid.R;
import org.stepic.droid.base.App;
import org.stepic.droid.core.presenters.SearchCoursesPresenter;
import org.stepic.droid.storage.operations.Table;
import org.stepic.droid.ui.custom.AutoCompleteSearchView;
import org.stepic.droid.ui.util.SearchViewHelper;
import org.stepic.droid.ui.util.ToolbarHelperKt;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;

public class CourseSearchFragment extends CourseListFragmentBase {

    private final static String QUERY_KEY = "query_key";

    public static Fragment newInstance(String query) {
        Fragment fragment = new CourseSearchFragment();
        Bundle bundle = new Bundle();
        bundle.putString(QUERY_KEY, query);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    private String searchQuery;

    @Inject
    SearchCoursesPresenter searchCoursesPresenter;

    private CompositeDisposable searchSuggestionsDisposable;

    @Override
    protected void injectComponent() {
        App.Companion
                .componentManager()
                .courseGeneralComponent()
                .courseListComponentBuilder()
                .build()
                .inject(this);
    }

    @Override
    protected void onReleaseComponent() {
        App.Companion
                .componentManager()
                .releaseCourseGeneralComponent();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        searchQuery = getArguments().getString(QUERY_KEY);
        super.onViewCreated(view, savedInstanceState);
        ToolbarHelperKt.initCenteredToolbar(this, R.string.search_title, true);
        emptySearch.setClickable(false);
        emptySearch.setFocusable(false);
        searchCoursesPresenter.attachView(this);
        searchCoursesPresenter.restoreState();
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                searchCoursesPresenter.downloadData(searchQuery);
            }
        });

    }

    @Override
    public void onDestroyView() {
        searchCoursesPresenter.detachView(this);
        super.onDestroyView();
    }

    @Override
    protected Table getCourseType() {
        return null;
    }

    @Override
    public void showEmptyScreen(boolean isShowed) {
        if (isShowed) {
            emptySearch.setVisibility(View.VISIBLE);
            swipeRefreshLayout.setVisibility(View.GONE);
        } else {
            emptySearch.setVisibility(View.GONE);
            swipeRefreshLayout.setVisibility(View.VISIBLE);

        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.search_menu, menu);
        MenuItem searchMenuItem = menu.findItem(R.id.action_search);
        AutoCompleteSearchView searchView = (AutoCompleteSearchView) searchMenuItem.getActionView();

        searchView.setCloseIconDrawableRes(getCloseIconDrawableRes());
        searchView.setSearchable(getActivity());
        searchView.initSuggestions(rootView);

        searchSuggestionsDisposable = SearchViewHelper.INSTANCE.setupSearchViewSuggestionsSources(searchView, api, databaseFacade, null);

        searchMenuItem.expandActionView();
        if (searchQuery != null) {
            searchView.setQuery(searchQuery, false);
        }
        searchView.clearFocus();

        searchMenuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                getActivity().finish();
                return true;
            }
        });
    }

    @Override
    public void onDestroyOptionsMenu() {
        if (searchSuggestionsDisposable != null) {
            searchSuggestionsDisposable.dispose();
        }
        super.onDestroyOptionsMenu();
    }

    @Override
    protected void onNeedDownloadNextPage() {
        searchCoursesPresenter.downloadData(searchQuery);
    }

    @Override
    public void onRefresh() {
        searchCoursesPresenter.refreshData(searchQuery);
    }
}
