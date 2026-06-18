package org.stepik.android.view.profile.ui.delegate

import android.text.Spannable
import android.text.SpannableString
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.view.isVisible
import org.stepic.droid.R
import org.stepic.droid.analytic.AmplitudeAnalytic
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.databinding.HeaderProfileBinding
import org.stepic.droid.ui.util.hideAllChildren
import org.stepik.android.model.user.User
import org.stepik.android.view.base.ui.span.TypefaceSpanCompat

class ProfileStatsDelegate(
    headerBinding: HeaderProfileBinding,
    private val analytic: Analytic
) {
    companion object {
        private const val MIN_REPUTATION_RANK = 1000
        private const val MIN_KNOWLEDGE = 10
    }

    private val context = headerBinding.root.context
    private val resources = context.resources

    private val profileStats = headerBinding.profileStats

    private val profileCertificatesIssued = headerBinding.profileCertificatesIssued
    private val profileCoursesPublished = headerBinding.profileCoursesPublished
    private val profileKnowledgeRank = headerBinding.profileKnowledgeRank
    private val profileReputationRank = headerBinding.profileReputationRank

    init {
        profileKnowledgeRank.setOnClickListener {
            analytic.reportAmplitudeEvent(AmplitudeAnalytic.Profile.PROFILE_STAT_CLICKED, mapOf(AmplitudeAnalytic.Profile.Params.TYPE to AmplitudeAnalytic.Profile.Values.KNOWLEDGE))
        }

        profileReputationRank.setOnClickListener {
            analytic.reportAmplitudeEvent(AmplitudeAnalytic.Profile.PROFILE_STAT_CLICKED, mapOf(AmplitudeAnalytic.Profile.Params.TYPE to AmplitudeAnalytic.Profile.Values.REPUTATION))
        }
    }

    fun setProfileStats(user: User) {
        profileStats.hideAllChildren()
        val typefaceSpan = TypefaceSpanCompat(ResourcesCompat.getFont(context, R.font.roboto_bold))

        if (user.isOrganization) {
            val certificatesIssued = resources
                .getQuantityString(R.plurals.certificates_issued, user.issuedCertificatesCount.toInt(), user.issuedCertificatesCount)
                .let(::SpannableString)
                .apply {
                    setSpan(typefaceSpan, 0, user.issuedCertificatesCount.toString().length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }

            profileCertificatesIssued.text = certificatesIssued
            profileCertificatesIssued.isVisible = true

            val coursesPublished = resources
                .getQuantityString(R.plurals.courses_published, user.createdCoursesCount.toInt(), user.createdCoursesCount)
                .let(::SpannableString)
                .apply {
                    setSpan(typefaceSpan, 0, user.createdCoursesCount.toString().length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }

            profileCoursesPublished.text = coursesPublished
            profileCoursesPublished.isVisible = true
        } else {
            if (user.knowledge > MIN_KNOWLEDGE) {
                profileKnowledgeRank.text =
                    buildSpannedString {
                        append(context.getString(R.string.profile_stat_knowledge))
                        bold { append(context.getString(R.string.profile_stat_top, user.knowledge, user.knowledgeRank)) }
                    }
                profileKnowledgeRank.isVisible = true
            }

            if (user.reputationRank in 1 until MIN_REPUTATION_RANK) {
                profileReputationRank.text =
                    buildSpannedString {
                        append(context.getString(R.string.profile_stat_reputation))
                        bold { append(context.getString(R.string.profile_stat_top, user.reputation, user.reputationRank)) }
                    }
                profileReputationRank.isVisible = true
            }
        }
    }
}
