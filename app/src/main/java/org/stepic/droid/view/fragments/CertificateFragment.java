package org.stepic.droid.view.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.stepic.droid.R;
import org.stepic.droid.base.FragmentBase;
import org.stepic.droid.base.MainApplication;
import org.stepic.droid.core.CertificateView;
import org.stepic.droid.model.CertificateViewItem;
import org.stepic.droid.presenters.certificate.CertificatePresenter;
import org.stepic.droid.util.ProgressHelper;
import org.stepic.droid.view.dialogs.CertificateShareDialogFragment;
import org.stepic.droid.view.adapters.CertificateAdapter;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CertificateFragment extends FragmentBase implements CertificateView {

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_certificates, null);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new CertificateAdapter(certificatePresenter, getActivity());
        certificateRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        certificateRecyclerView.setAdapter(adapter);

        certificatePresenter.onCreate(this);

        loadAndShowCertificates();
    }

    @Override
    public void onDestroyView() {
        certificatePresenter.onDestroy();
        super.onDestroyView();
    }

    private void loadAndShowCertificates() {
        certificatePresenter.showCertificates();
    }

    @Override
    protected void injectComponent() {
        MainApplication.certificateComponent().inject(this);
    }

    @Override
    public void onLoading() {
        if (certificatePresenter.size() <= 0) {
            reportInternetProblem.setVisibility(View.GONE);
            certificateRecyclerView.setVisibility(View.INVISIBLE);
            reportEmpty.setVisibility(View.GONE);
            ProgressHelper.activate(progressBarOnCenter);
        }
    }

    @Override
    public void showEmptyState() {
        ProgressHelper.dismiss(progressBarOnCenter);
        reportInternetProblem.setVisibility(View.GONE);
        if (certificatePresenter.size() <= 0) {
            reportEmpty.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onInternetProblem() {
        ProgressHelper.dismiss(progressBarOnCenter);
        reportEmpty.setVisibility(View.GONE);
        if (certificatePresenter.size() <= 0) {
            reportInternetProblem.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(getContext(), R.string.connectionProblems, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDataLoaded(List<CertificateViewItem> certificateViewItems) {
        ProgressHelper.dismiss(progressBarOnCenter);
        reportEmpty.setVisibility(View.GONE);
        reportInternetProblem.setVisibility(View.GONE);
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
}
