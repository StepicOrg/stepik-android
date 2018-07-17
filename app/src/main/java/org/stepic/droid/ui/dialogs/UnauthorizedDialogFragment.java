package org.stepic.droid.ui.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import org.jetbrains.annotations.NotNull;
import org.stepic.droid.R;
import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.base.App;
import org.stepic.droid.core.ScreenManager;
import org.stepik.android.model.structure.Course;

import javax.inject.Inject;

public class UnauthorizedDialogFragment extends DialogFragment {

    private static final String courseForEnrollKey = "course_for_enroll";

    public static DialogFragment newInstance() {
        return new UnauthorizedDialogFragment();
    }

    public static DialogFragment newInstance(Course course) {
        DialogFragment fragment = new UnauthorizedDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable(courseForEnrollKey, course);
        fragment.setArguments(args);
        return fragment;
    }


    @Inject
    ScreenManager screenManager;

    @Inject
    Analytic analytic;

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        App.Companion.component().inject(this);
        Bundle args = getArguments();
        final Course nullableCourse;
        if (args != null) {
            nullableCourse = args.getParcelable(courseForEnrollKey);
        } else {
            nullableCourse = null;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.authorization)
                .setMessage(R.string.unauthorization_detail)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        analytic.reportEvent(Analytic.Interaction.AUTH_FROM_DIALOG_FOR_UNAUTHORIZED_USER);
                        if (nullableCourse == null) {
                            screenManager.showLaunchScreen(getActivity());
                        } else {
                            screenManager.showLaunchScreen(getActivity(), nullableCourse);
                        }
                    }
                })
                .setNegativeButton(R.string.no, null);

        return builder.create();
    }
}
