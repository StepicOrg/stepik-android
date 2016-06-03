package org.stepic.droid.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import org.stepic.droid.R;
import org.stepic.droid.model.Lesson;
import org.stepic.droid.model.Step;
import org.stepic.droid.model.Unit;
import org.stepic.droid.util.AppConstants;
import org.stepic.droid.view.custom.LatexSupportableWebView;

import butterknife.Bind;

public abstract class StepBaseFragment extends FragmentBase {

    @Bind(R.id.text_header)
    protected LatexSupportableWebView headerWv;

    @Bind(R.id.open_comments_root)
    protected View openCommentViewClickable;

    @Bind(R.id.open_comments_text)
    protected TextView textForComment;

    protected Step step;
    protected Lesson lesson;
    protected Unit unit;


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        step = (Step) getArguments().getSerializable(AppConstants.KEY_STEP_BUNDLE);
        lesson = (Lesson) getArguments().getSerializable(AppConstants.KEY_LESSON_BUNDLE);
        unit = (Unit) getArguments().getSerializable(AppConstants.KEY_UNIT_BUNDLE);

        if (step != null &&
                step.getBlock() != null &&
                step.getBlock().getText() != null &&
                !step.getBlock().getText().equals("")) {
            headerWv.setText(step.getBlock().getText());
            headerWv.setVisibility(View.VISIBLE);
        } else {
            headerWv.setVisibility(View.GONE);
        }

        if (step != null && step.getDiscussion_proxy() != null) {
            showComment();
        } else {
            openCommentViewClickable.setVisibility(View.GONE);
        }

        bus.register(this);
    }

    private void showComment() {
        openCommentViewClickable.setVisibility(View.VISIBLE);
        openCommentViewClickable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mShell.getScreenProvider().openComments(getContext(), step.getDiscussion_proxy(), step.getId());
            }
        });
        textForComment.setText(MainApplication.getAppContext().getResources().getQuantityString(R.plurals.open_comments, step.getDiscussions_count(), step.getDiscussions_count()));
    }


    @Override
    public void onResume() {
        super.onResume();
        hideSoftKeypad();
    }

    @Override
    public void onDestroyView() {
        bus.unregister(this);
        openCommentViewClickable.setOnClickListener(null);
        super.onDestroyView();
    }
}
