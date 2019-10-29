package org.stepic.droid.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import org.stepik.android.model.Lesson;
import org.stepik.android.model.Step;
import org.stepik.android.model.Unit;

public class StepShareDialogFragment extends DialogFragment {
    public static final String TAG = "StepShareDialogFragment";

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
