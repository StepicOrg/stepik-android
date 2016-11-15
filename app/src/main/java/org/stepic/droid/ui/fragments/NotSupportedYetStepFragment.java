package org.stepic.droid.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.squareup.otto.Subscribe;

import org.stepic.droid.R;
import org.stepic.droid.base.StepBaseFragment;
import org.stepic.droid.events.comments.NewCommentWasAddedOrUpdateEvent;
import org.stepic.droid.events.steps.StepWasUpdatedEvent;

import butterknife.BindView;

public class NotSupportedYetStepFragment extends StepBaseFragment {

    @BindView(R.id.open_web)
    Button openWebBtn;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_unsupported, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        openWebBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shell.getScreenProvider().openStepInWeb(getContext(), step);
            }
        });
    }

    @Subscribe
    public void onNewCommentWasAdded(NewCommentWasAddedOrUpdateEvent event) {
        super.onNewCommentWasAdded(event);

    }

    @Subscribe
    public void onStepWasUpdated(StepWasUpdatedEvent event) {
        super.onStepWasUpdated(event);
    }
}
