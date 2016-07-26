package org.stepic.droid.view.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.stepic.droid.R;
import org.stepic.droid.base.FragmentBase;
import org.stepic.droid.base.MainApplication;
import org.stepic.droid.core.CertificateView;
import org.stepic.droid.model.CertificateViewItem;
import org.stepic.droid.presenters.certificate.CertificatePresenter;
import org.stepic.droid.view.adapters.CertificateAdapter;

import java.util.ArrayList;
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

        adapter = new CertificateAdapter(getContext(), new ArrayList<CertificateViewItem>());
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
// TODO: 26.07.16 hide all, show loading
    }

    @Override
    public void showEmptyState() {
// TODO: 26.07.16 hide all show empty
    }

    @Override
    public void onInternetProblem() {
// TODO: 26.07.16 hide all show internet problem
    }

    @Override
    public void onDataLoaded(List<CertificateViewItem> certificateViewItems) {
        // TODO: 26.07.16 hide all, except recycler
        adapter.updateCertificates(certificateViewItems);
    }
}
