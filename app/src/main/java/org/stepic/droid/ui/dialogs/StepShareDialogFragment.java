package org.stepic.droid.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;

import org.stepik.android.model.structure.Lesson;
import org.stepic.droid.model.Step;
import org.stepik.android.model.structure.Unit;

public class StepShareDialogFragment extends DialogFragment {

    private static final String STEP_KEY = "stepKey";
    private static final String LESSON_KEY = "lessonKey";
    private static final String UNIT_KEY = "unitKey";

    public static DialogFragment newInstance(Step step, Lesson lesson, @Nullable Unit unit) {
        Bundle args = new Bundle();
        args.putParcelable(STEP_KEY, step);
        args.putParcelable(LESSON_KEY, lesson);
        args.putParcelable(UNIT_KEY, unit);
        DialogFragment fragment = new StepShareDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Step step = getArguments().getParcelable(STEP_KEY);
        Lesson lesson = getArguments().getParcelable(LESSON_KEY);
        Unit unit = getArguments().getParcelable(UNIT_KEY);
        return new StepShareDialog(getContext(), step, lesson, unit);
    }

}
