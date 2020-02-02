package org.stepik.android.view.course_info.mapper

import android.content.Context
import androidx.annotation.StringRes
import org.stepic.droid.R
import org.stepik.android.domain.course_info.model.CourseInfoData
import org.stepik.android.view.course_info.model.CourseInfoItem
import org.stepik.android.view.course_info.model.CourseInfoType

private const val NEW_LINE = "<br/>"

fun CourseInfoData.toSortedItems(context: Context): List<CourseInfoItem> {
    val items = arrayListOf<CourseInfoItem>()

    if (organization != null) {
        items.add(CourseInfoItem.OrganizationBlock(organization))
    }

    if (videoMediaData != null) {
        items.add(CourseInfoItem.VideoBlock(videoMediaData))
    }

    items.addTextItem(CourseInfoType.ABOUT, about)
    items.addTextItem(CourseInfoType.REQUIREMENTS, requirements)
    items.addTextItem(CourseInfoType.TARGET_AUDIENCE, targetAudience)

    if (timeToComplete > 0) {
        val hours = (timeToComplete / 3600).toInt()
        items.addTextItem(CourseInfoType.TIME_TO_COMPLETE, context.resources.getQuantityString(R.plurals.hours, hours, hours))
    }

    if (instructors != null) {
        items.add(CourseInfoItem.WithTitle.InstructorsBlock(instructors))
    }

    items.addTextItem(CourseInfoType.LANGUAGE, mapCourseLanguage(language)?.let(context::getString))

    if (certificate != null) {
        items.addTextItem(CourseInfoType.CERTIFICATE, certificate.title.ifEmpty { context.getString(R.string.certificate_issuing) })

        val certificateConditions = mutableListOf<String>()

        if (certificate.regularThreshold > 0) {
            val regularPoints = context.resources.getQuantityString(R.plurals.points, certificate.regularThreshold.toInt(), certificate.regularThreshold)
            val regularCondition = context.getString(R.string.course_info_certificate_regular, regularPoints)
            certificateConditions.add(regularCondition)
        }

        if (certificate.distinctionThreshold > 0) {
            val distinctionPoints = context.resources.getQuantityString(R.plurals.points, certificate.distinctionThreshold.toInt(), certificate.distinctionThreshold)
            val distinctionCondition = context.getString(R.string.course_info_certificate_distinction, distinctionPoints)
            certificateConditions.add(distinctionCondition)
        }

        items.addTextItem(CourseInfoType.CERTIFICATE_DETAILS, certificateConditions.joinToString(NEW_LINE))
    } else {
        items.addTextItem(CourseInfoType.CERTIFICATE, context.getString(R.string.certificate_not_issuing))
    }

    if (learnersCount > 0) {
        items.addTextItem(CourseInfoType.LEARNERS_COUNT, learnersCount.toString())
    }

    return items
}

private fun MutableList<CourseInfoItem>.addTextItem(type: CourseInfoType, text: String?) {
    if (text != null) {
        add(CourseInfoItem.WithTitle.TextBlock(type, text))
    }
}

@StringRes
private fun mapCourseLanguage(language: String?): Int? =
    when (language) {
        "ru" -> R.string.course_info_language_ru
        "en" -> R.string.course_info_language_en
        "de" -> R.string.course_info_language_de
        "es" -> R.string.course_info_language_es
        "ua" -> R.string.course_info_language_ua
        "ch" -> R.string.course_info_language_ch
        else -> null
    }