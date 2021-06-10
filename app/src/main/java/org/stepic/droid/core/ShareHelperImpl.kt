package org.stepic.droid.core

import android.content.Context
import android.content.Intent
import android.net.Uri
import org.stepic.droid.R
import org.stepic.droid.configuration.Config
import org.stepic.droid.di.AppSingleton
import org.stepic.droid.model.CertificateViewItem
import org.stepic.droid.util.StringUtil
import org.stepic.droid.util.resolvers.text.TextResolver
import org.stepik.android.model.*
import org.stepik.android.model.Unit
import org.stepik.android.model.user.User
import javax.inject.Inject

@AppSingleton
class ShareHelperImpl
@Inject
constructor(
        private val config: Config,
        private val context: Context,
        private val textResolver: TextResolver
) : ShareHelper {

    private val textPlainType = "text/plain"

    override fun getIntentForCourseSharing(course: Course): Intent {
        val stringBuilder = StringBuilder()
        with(stringBuilder) {
            if (course.title != null) {
                append(course.title)
                append("\r\n")
                append("\r\n")
            }

            if (course.summary?.isNotEmpty() == true) {
                append(textResolver.fromHtml(course.summary).toString())
                append("\r\n")
                append("\r\n")
            }

        }
        val uriForSharing = Uri.parse(StringUtil.getUriForCourse(config.baseUrl, course.slug)).toString()
        val textForSharing = textResolver.fromHtml(stringBuilder.toString()).toString() + "\r\n\r\n" + uriForSharing
        return getShareIntentBase(textForSharing)
    }

    override fun getIntentForShareCertificate(certificateViewItem: CertificateViewItem): Intent =
            getShareIntentBase(certificateViewItem.certificate.url ?: " ")

    override fun getIntentForStepSharing(step: Step, lesson: Lesson, unit: Unit?): Intent {
        val textForSharing = Uri.parse(StringUtil.getUriForStep(config.baseUrl, lesson, unit, step)).toString()
        return getShareIntentBase(textForSharing)
    }

    override fun getIntentForSectionSharing(section: Section): Intent {
        val textForSharing = Uri.parse(StringUtil.getAbsoluteUriForSection(config, section)).toString()
        return getShareIntentBase(textForSharing)
    }

    override fun getIntentForUserSharing(user: User): Intent {
        val stringBuilder = StringBuilder()
        with(stringBuilder) {
            if (!user.fullName.isNullOrBlank()) {
                append(user.fullName)
                append("\r\n")
                append("\r\n")
            }

            val uriForSharing = Uri.parse(StringUtil.getUriForProfile(config.baseUrl, user.id)).toString()
            append(uriForSharing)
        }
        val textForSharing = stringBuilder.toString()
        return getShareIntentBase(textForSharing)
    }

    override fun getIntentForCourseResultSharing(course: Course, message: String): Intent {
        val stringBuilder = StringBuilder()
        with(stringBuilder) {
            append(message)
            append("\r\n")
            append("\r\n")

            val uriForSharing = Uri.parse(StringUtil.getUriForCourse(config.baseUrl, course.slug)).toString()
            append(uriForSharing)
        }
        val textForSharing = stringBuilder.toString()
        return getShareIntentBase(textForSharing)
    }

    override fun getIntentForCourseResultCertificateSharing(certificate: Certificate, message: String): Intent {
        val stringBuilder = StringBuilder()
        with(stringBuilder) {
            append(message)
            append("\r\n")
            append("\r\n")
            append(certificate.url ?: "")
        }
        val textForSharing = stringBuilder.toString()
        return getShareIntentBase(textForSharing)
    }

    private fun getShareIntentBase(textForSharing: String): Intent {
        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND
        shareIntent.putExtra(Intent.EXTRA_TEXT, textForSharing)
        shareIntent.type = textPlainType
        return Intent.createChooser(shareIntent, context.getString(R.string.share_title))
    }

}
