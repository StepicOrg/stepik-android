package org.stepic.droid.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.stepic.droid.R;
import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.base.FragmentBase;

import butterknife.BindView;

public class AboutAppFragment extends FragmentBase {
    public static AboutAppFragment newInstance() {
        return new AboutAppFragment();
    }

    @BindView(R.id.privacy_policy_view)
    View privacyPolicyView;

    @BindView(R.id.terms_of_service_view)
    View termsOfService;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_about_app, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        privacyPolicyView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                analytic.reportEvent(Analytic.Interaction.CLICK_PRIVACY_POLICY);
                shell.getScreenProvider().openPrivacyPolicyWeb(getActivity());
            }
        });

        termsOfService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                analytic.reportEvent(Analytic.Interaction.CLICK_TERMS_OF_SERVICE);
                shell.getScreenProvider().openTermsOfServiceWeb(getActivity());
            }
        });
    }

    @Override
    public void onDestroyView() {
        privacyPolicyView.setOnClickListener(null);
        termsOfService.setOnClickListener(null);
        super.onDestroyView();
    }
}
