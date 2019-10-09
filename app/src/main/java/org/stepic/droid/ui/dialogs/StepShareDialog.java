package org.stepic.droid.ui.dialogs;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import android.view.View;
import android.view.ViewGroup;

import org.stepic.droid.R;
import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.base.App;
import org.stepic.droid.configuration.Config;
import org.stepic.droid.core.ScreenManager;
import org.stepic.droid.core.ShareHelper;
import org.stepic.droid.util.ContextExtensionsKt;
import org.stepic.droid.util.DisplayUtils;
import org.stepic.droid.util.StringUtil;
import org.stepik.android.model.Lesson;
import org.stepik.android.model.Step;
import org.stepik.android.model.Unit;

import javax.inject.Inject;

import butterknife.ButterKnife;

public class StepShareDialog extends BottomSheetDialog {

    @NonNull
    private final Context context;
    private final Step step;
    private final Lesson lesson;
    private final Unit unit;

    @Inject
    Analytic analytic;

    @Inject
    ShareHelper shareHelper;

    @Inject
    ScreenManager screenManager;

    @Inject
    Config config;

    public StepShareDialog(@NonNull Context context, Step step, Lesson lesson, Unit unit) {
        super(context);
        this.context = context;
        this.step = step;
        this.lesson = lesson;
        this.unit = unit;
        App.Companion.component().inject(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View view = View.inflate(getContext(), R.layout.step_share_view, null);
        setContentView(view);

        int screenHeight = DisplayUtils.getScreenHeight();
        int statusBarHeight = DisplayUtils.getStatusBarHeight(getContext());
        int dialogHeight = screenHeight - statusBarHeight;
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, dialogHeight == 0 ? ViewGroup.LayoutParams.MATCH_PARENT : dialogHeight);

        View openInBrowser = view.findViewById( R.id.share_open_in_browser);
        View copyLink = view.findViewById(R.id.share_certificate_copy_link);
        View shareAll = view.findViewById(R.id.share_certificate_all);

        openInBrowser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                analytic.reportEvent(Analytic.Steps.SHARE_OPEN_IN_BROWSER);
                screenManager.openStepInWeb(context, step);

            }
        });

        copyLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                analytic.reportEvent(Analytic.Steps.COPY_LINK);
                ContextExtensionsKt.copyTextToClipboard(
                        context,
                        App.Companion.getAppContext().getString(R.string.copy_link_title),
                        StringUtil.getUriForStep(config.getBaseUrl(), lesson, unit, step),
                        context.getResources().getString(R.string.link_copied_title)
                );
            }
        });

        shareAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                analytic.reportEvent(Analytic.Steps.SHARE_ALL);
                Intent shareIntent = shareHelper.getIntentForStepSharing(step, lesson, unit);
                context.startActivity(shareIntent);
            }
        });

    }
}
