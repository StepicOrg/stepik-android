package org.stepic.droid.core;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.stepic.droid.R;
import org.stepic.droid.configuration.Config;
import org.stepic.droid.model.CertificateViewItem;
import org.stepic.droid.model.Course;
import org.stepic.droid.model.Lesson;
import org.stepic.droid.model.Section;
import org.stepic.droid.model.Step;
import org.stepic.droid.model.Unit;
import org.stepic.droid.util.StringUtil;
import org.stepic.droid.util.resolvers.text.TextResolver;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ShareHelperImpl implements ShareHelper {

    Config config;

    Context context;

    private TextResolver textResolver;

    @Inject
    public ShareHelperImpl(Config config, Context context, TextResolver textResolver) {
        this.config = config;
        this.context = context;
        this.textResolver = textResolver;
    }


    @Override
    public Intent getIntentForCourseSharing(@NotNull Course course) {
        Intent shareIntent = new Intent();
        StringBuilder sb = new StringBuilder();

        if (course.getTitle() != null) {
            sb.append(course.getTitle());
            sb.append("\r\n");
            sb.append("\r\n");
        }

        if (course.getSummary() != null && !course.getSummary().isEmpty()) {
            sb.append(textResolver.fromHtml(course.getSummary()).toString());
            sb.append("\r\n");
            sb.append("\r\n");
        }

        String uriForSharing = Uri.parse(StringUtil.getUriForCourse(config.getBaseUrl(), course.getSlug())).toString();
        sb.append(uriForSharing);

        String textForSharing = textResolver.fromHtml(sb.toString()).toString();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, textForSharing);
        shareIntent.setType("text/plain");
        return Intent.createChooser(shareIntent, context.getString(R.string.share_title));
    }

    @Override
    public Intent getIntentForShareCertificate(@NotNull CertificateViewItem certificateViewItem) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, certificateViewItem.getFullPath());
        shareIntent.setType("text/plain");
        return Intent.createChooser(shareIntent, context.getString(R.string.share_title));
    }

    @Override
    public Intent getIntentForStepSharing(Step step, Lesson lesson, @Nullable Unit unit) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        String textForSharing = Uri.parse(StringUtil.getUriForStep(config.getBaseUrl(), lesson, unit, step)).toString();
        shareIntent.putExtra(Intent.EXTRA_TEXT, textForSharing);
        shareIntent.setType("text/plain");
        return Intent.createChooser(shareIntent, context.getString(R.string.share_title));
    }

    @Override
    public Intent getIntentForSectionSharing(@NotNull Section section) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        String textForSharing = Uri.parse(StringUtil.getAbsoluteUriForSection(config, section)).toString();
        shareIntent.putExtra(Intent.EXTRA_TEXT, textForSharing);
        shareIntent.setType("text/plain");
        return Intent.createChooser(shareIntent, context.getString(R.string.share_title));
    }

}
