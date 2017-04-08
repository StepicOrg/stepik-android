package org.stepic.droid.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.stepic.droid.R;
import org.stepic.droid.base.App;
import org.stepic.droid.base.FragmentBase;
import org.stepic.droid.core.presenters.CertificatePresenter;
import org.stepic.droid.core.presenters.contracts.CertificateView;
import org.stepic.droid.model.CertificateViewItem;
import org.stepic.droid.ui.adapters.CertificateAdapter;
import org.stepic.droid.ui.dialogs.CertificateShareDialogFragment;
import org.stepic.droid.util.ProgressHelper;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;

public class CertificateFragment extends FragmentBase implements CertificateView, SwipeRefreshLayout.OnRefreshListener {

    private CertificateAdapter adapter;

    public static Fragment newInstance() {
        Bundle args = new Bundle();
        CertificateFragment fragment = new CertificateFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Inject
    CertificatePresenter certificatePresenter;

    @BindView(R.id.certificates_recycler_view)
    RecyclerView certificateRecyclerView;

    @BindView(R.id.load_progressbar)
    ProgressBar progressBarOnCenter;

    @BindView(R.id.report_problem)
    View reportInternetProblem;

    @BindView(R.id.report_empty)
    View reportEmpty;

    @BindView(R.id.certificate_swipe_refresh)
    SwipeRefreshLayout swipeRefreshLayout;

    @BindView(R.id.need_auth_view)
    View needAuthRootView;

    @BindView(R.id.auth_action)
    Button authUserButton;

    @Override
    protected void injectComponent() {
        App
                .component()
                .certificateComponentBuilder()
                .build()
                .inject(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_certificates, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new CertificateAdapter(certificatePresenter, getActivity());
        certificateRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        certificateRecyclerView.setAdapter(adapter);

        authUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shell.getScreenProvider().showLaunchScreen(getActivity());
            }
        });

        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(
                R.color.stepic_brand_primary,
                R.color.stepic_orange_carrot,
                R.color.stepic_blue_ribbon);

        certificatePresenter.attachView(this);

        loadAndShowCertificates();
    }

    @Override
    public void onDestroyView() {
        certificatePresenter.detachView(this);
        authUserButton.setOnClickListener(null);
        super.onDestroyView();
    }

    private void loadAndShowCertificates() {
        certificatePresenter.showCertificates(false);
    }

    @Override
    public void onLoading() {
        if (certificatePresenter.size() <= 0) {
            reportInternetProblem.setVisibility(View.GONE);
            reportEmpty.setVisibility(View.GONE);
            ProgressHelper.activate(progressBarOnCenter);
        }
    }

    @Override
    public void showEmptyState() {
        needAuthRootView.setVisibility(View.GONE);
        ProgressHelper.dismiss(swipeRefreshLayout);
        ProgressHelper.dismiss(progressBarOnCenter);
        reportInternetProblem.setVisibility(View.GONE);
        if (certificatePresenter.size() <= 0) {
            reportEmpty.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onInternetProblem() {
        ProgressHelper.dismiss(swipeRefreshLayout);
        ProgressHelper.dismiss(progressBarOnCenter);
        reportEmpty.setVisibility(View.GONE);
        needAuthRootView.setVisibility(View.GONE);
        if (certificatePresenter.size() <= 0) {
            reportInternetProblem.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(getContext(), R.string.connectionProblems, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDataLoaded(List<CertificateViewItem> certificateViewItems) {
        ProgressHelper.dismiss(progressBarOnCenter);
        ProgressHelper.dismiss(swipeRefreshLayout);
        reportEmpty.setVisibility(View.GONE);
        reportInternetProblem.setVisibility(View.GONE);
        needAuthRootView.setVisibility(View.GONE);
        swipeRefreshLayout.setVisibility(View.VISIBLE);
        certificateRecyclerView.setVisibility(View.VISIBLE);
        adapter.updateCertificates(certificateViewItems);
    }

    @Override
    public void onNeedShowShareDialog(@org.jetbrains.annotations.Nullable CertificateViewItem certificateViewItem) {
        if (certificateViewItem == null) {
            return;
        }
        DialogFragment bottomSheetDialogFragment = CertificateShareDialogFragment.newInstance(certificateViewItem);
        if (bottomSheetDialogFragment != null && !bottomSheetDialogFragment.isAdded()) {
            bottomSheetDialogFragment.show(getFragmentManager(), null);
        }

    }

    @Override
    public void onAnonymousUser() {
        ProgressHelper.dismiss(swipeRefreshLayout);
        ProgressHelper.dismiss(progressBarOnCenter);
        reportEmpty.setVisibility(View.GONE);
        reportInternetProblem.setVisibility(View.GONE);
        certificateRecyclerView.setVisibility(View.GONE);
        swipeRefreshLayout.setVisibility(View.GONE);
        needAuthRootView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onRefresh() {
        certificatePresenter.showCertificates(true);
    }
}
