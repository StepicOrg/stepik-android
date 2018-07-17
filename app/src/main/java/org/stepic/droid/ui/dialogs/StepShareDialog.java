package org.stepic.droid.ui.dialogs;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.stepic.droid.R;
import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.base.App;
import org.stepic.droid.configuration.Config;
import org.stepic.droid.core.ScreenManager;
import org.stepic.droid.core.ShareHelper;
import org.stepic.droid.model.Lesson;
import org.stepic.droid.model.Step;
import org.stepik.android.model.structure.Unit;
import org.stepic.droid.util.DisplayUtils;
import org.stepic.droid.util.StringUtil;

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

        View openInBrowser = ButterKnife.findById(view, R.id.share_open_in_browser);
        View copyLink = ButterKnife.findById(view, R.id.share_certificate_copy_link);
        View shareAll = ButterKnife.findById(view, R.id.share_certificate_all);

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
                ClipData clipData = ClipData.newPlainText(App.Companion.getAppContext().getString(R.string.copy_link_title), StringUtil.getUriForStep(config.getBaseUrl(), lesson, unit, step));
                ClipboardManager clipboardManager = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                clipboardManager.setPrimaryClip(clipData);
                Toast.makeText(getContext(), R.string.link_copied_title, Toast.LENGTH_SHORT).show();
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
