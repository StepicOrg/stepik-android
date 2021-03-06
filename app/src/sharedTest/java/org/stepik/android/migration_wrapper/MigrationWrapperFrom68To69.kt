package org.stepik.android.migration_wrapper

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.google.gson.Gson
import org.stepic.droid.storage.migration.MigrationFrom68To69
import org.stepic.droid.storage.structure.DbStructureCourse
import org.stepic.droid.util.*
import org.stepik.android.model.Course
import org.stepik.android.model.Video
import java.util.*

class MigrationWrapperFrom68To69(migration: MigrationFrom68To69) : MigrationWrapper(migration) {

    private val gson = Gson()

    override fun beforeTest(db: SupportSQLiteDatabase) {
        val beforeCourse = generateTestCourse()
        db.insert(DbStructureCourse.TABLE_NAME, SQLiteDatabase.CONFLICT_NONE, getContentValues(beforeCourse))
    }

    override fun afterTest(db: SupportSQLiteDatabase) {
        val beforeCourse = generateTestCourse()
        val cursor = db.query("SELECT * FROM ${DbStructureCourse.TABLE_NAME}")
        cursor.moveToFirst()
        val retrievedCourse = parsePersistentObject(cursor)
        println("Before: $beforeCourse")
        println("After: $retrievedCourse")
        assert(beforeCourse == retrievedCourse) { "Objects not equal" }
    }

    private fun getContentValues(course: Course): ContentValues {
        val values = ContentValues()

        values.put(DbStructureCourse.Columns.ID, course.id)
        values.put(DbStructureCourse.Columns.TITLE, course.title)
        values.put(DbStructureCourse.Columns.DESCRIPTION, course.description)
        values.put(DbStructureCourse.Columns.COVER, course.cover)
        values.put(DbStructureCourse.Columns.CERTIFICATE, course.certificate)
        values.put(DbStructureCourse.Columns.REQUIREMENTS, course.requirements)
        values.put(DbStructureCourse.Columns.SUMMARY, course.summary)
        values.put(DbStructureCourse.Columns.WORKLOAD, course.workload)
        values.put(DbStructureCourse.Columns.INTRO, course.intro)
        values.put(DbStructureCourse.Columns.INTRO_VIDEO_ID, course.introVideo?.id ?: -1)
        values.put(DbStructureCourse.Columns.LANGUAGE, course.language)
        values.put(DbStructureCourse.Columns.AUTHORS, DbParseHelper.parseLongListToString(course.authors))
        values.put(DbStructureCourse.Columns.INSTRUCTORS, DbParseHelper.parseLongListToString(course.instructors))
        values.put(DbStructureCourse.Columns.SECTIONS, DbParseHelper.parseLongListToString(course.sections))
        values.put(DbStructureCourse.Columns.COURSE_FORMAT, course.courseFormat)
        values.put(DbStructureCourse.Columns.TARGET_AUDIENCE, course.targetAudience)
        values.put(DbStructureCourse.Columns.CERTIFICATE_FOOTER, course.certificateFooter)
        values.put(DbStructureCourse.Columns.CERTIFICATE_COVER_ORG, course.certificateCoverOrg)
        values.put(DbStructureCourse.Columns.TOTAL_UNITS, course.totalUnits)
        values.put(DbStructureCourse.Columns.ENROLLMENT, course.enrollment)
        values.put(DbStructureCourse.Columns.PROGRESS, course.progress)
        values.put(DbStructureCourse.Columns.OWNER, course.owner)
        values.put(DbStructureCourse.Columns.READINESS, course.readiness)
        values.put(DbStructureCourse.Columns.IS_CONTEST, course.isContest)
        values.put(DbStructureCourse.Columns.IS_FEATURED, course.isFeatured)
        values.put(DbStructureCourse.Columns.IS_ACTIVE, course.isActive)
        values.put(DbStructureCourse.Columns.IS_PUBLIC, course.isPublic)
        values.put(DbStructureCourse.Columns.IS_ARCHIVED, course.isArchived)
        values.put(DbStructureCourse.Columns.IS_FAVORITE, course.isFavorite)
        values.put(DbStructureCourse.Columns.IS_PROCTORED, course.isProctored)
        values.put(DbStructureCourse.Columns.CERTIFICATE_DISTINCTION_THRESHOLD, course.certificateDistinctionThreshold)
        values.put(DbStructureCourse.Columns.CERTIFICATE_REGULAR_THRESHOLD, course.certificateRegularThreshold)
        values.put(DbStructureCourse.Columns.CERTIFICATE_LINK, course.certificateLink)
        values.put(DbStructureCourse.Columns.IS_CERTIFICATE_AUTO_ISSUED, course.isCertificateAutoIssued)
        values.put(DbStructureCourse.Columns.IS_CERTIFICATE_ISSUED, course.isCertificateIssued)
        values.put(DbStructureCourse.Columns.LAST_DEADLINE, course.lastDeadline)
        values.put(DbStructureCourse.Columns.BEGIN_DATE, course.beginDate)
        values.put(DbStructureCourse.Columns.END_DATE, course.endDate)
        values.put(DbStructureCourse.Columns.SLUG, course.slug)

        values.put(DbStructureCourse.Columns.SCHEDULE_LINK, course.scheduleLink)
        values.put(DbStructureCourse.Columns.SCHEDULE_LONG_LINK, course.scheduleLongLink)
        values.put(DbStructureCourse.Columns.SCHEDULE_TYPE, course.scheduleType)

        values.put(DbStructureCourse.Columns.LAST_STEP, course.lastStepId)
        values.put(DbStructureCourse.Columns.LEARNERS_COUNT, course.learnersCount)
        values.put(DbStructureCourse.Columns.REVIEW_SUMMARY, course.reviewSummary)
        values.put(DbStructureCourse.Columns.TIME_TO_COMPLETE, course.timeToComplete)
        values.put(DbStructureCourse.Columns.OPTIONS, course.courseOptions?.let(gson::toJson))

        values.put(DbStructureCourse.Columns.IS_PAID, course.isPaid)
        values.put(DbStructureCourse.Columns.PRICE, course.price)
        values.put(DbStructureCourse.Columns.CURRENCY_CODE, course.currencyCode)
        values.put(DbStructureCourse.Columns.DISPLAY_PRICE, course.displayPrice)
        values.put(DbStructureCourse.Columns.PRICE_TIER, course.priceTier)

        return values
    }

    private fun parsePersistentObject(cursor: Cursor): Course =
        Course(
            id = cursor.getLong(DbStructureCourse.Columns.ID),
            title = cursor.getString(DbStructureCourse.Columns.TITLE),
            description = cursor.getString(DbStructureCourse.Columns.DESCRIPTION),
            cover = cursor.getString(DbStructureCourse.Columns.COVER),
            certificate = cursor.getString(DbStructureCourse.Columns.CERTIFICATE),
            requirements = cursor.getString(DbStructureCourse.Columns.REQUIREMENTS),
            summary = cursor.getString(DbStructureCourse.Columns.SUMMARY),
            workload = cursor.getString(DbStructureCourse.Columns.WORKLOAD),
            intro = cursor.getString(DbStructureCourse.Columns.INTRO),
            introVideo = Video(id = cursor.getLong(DbStructureCourse.Columns.INTRO_VIDEO_ID)),
            language = cursor.getString(DbStructureCourse.Columns.LANGUAGE),
            authors = DbParseHelper.parseStringToLongList(cursor.getString(DbStructureCourse.Columns.AUTHORS)),
            instructors = DbParseHelper.parseStringToLongList(cursor.getString(DbStructureCourse.Columns.INSTRUCTORS)),
            sections = DbParseHelper.parseStringToLongList(cursor.getString(DbStructureCourse.Columns.SECTIONS)),
            courseFormat = cursor.getString(DbStructureCourse.Columns.COURSE_FORMAT),
            targetAudience = cursor.getString(DbStructureCourse.Columns.TARGET_AUDIENCE),
            certificateFooter = cursor.getString(DbStructureCourse.Columns.CERTIFICATE_FOOTER),
            certificateCoverOrg = cursor.getString(DbStructureCourse.Columns.CERTIFICATE_COVER_ORG),
            totalUnits = cursor.getLong(DbStructureCourse.Columns.TOTAL_UNITS),
            enrollment = cursor.getLong(DbStructureCourse.Columns.ENROLLMENT),
            progress = cursor.getString(DbStructureCourse.Columns.PROGRESS),
            owner = cursor.getLong(DbStructureCourse.Columns.OWNER),
            readiness = cursor.getDouble(DbStructureCourse.Columns.READINESS),
            isContest = cursor.getBoolean(DbStructureCourse.Columns.IS_CONTEST),
            isFeatured = cursor.getBoolean(DbStructureCourse.Columns.IS_FEATURED),
            isActive = cursor.getBoolean(DbStructureCourse.Columns.IS_ACTIVE),
            isPublic = cursor.getBoolean(DbStructureCourse.Columns.IS_PUBLIC),
            isArchived = cursor.getBoolean(DbStructureCourse.Columns.IS_ARCHIVED),
            isFavorite = cursor.getBoolean(DbStructureCourse.Columns.IS_FAVORITE),
            isProctored = cursor.getBoolean(DbStructureCourse.Columns.IS_PROCTORED),
            certificateDistinctionThreshold = cursor.getLong(DbStructureCourse.Columns.CERTIFICATE_DISTINCTION_THRESHOLD),
            certificateRegularThreshold = cursor.getLong(DbStructureCourse.Columns.CERTIFICATE_REGULAR_THRESHOLD),
            certificateLink = cursor.getString(DbStructureCourse.Columns.CERTIFICATE_LINK),
            isCertificateAutoIssued = cursor.getBoolean(DbStructureCourse.Columns.IS_CERTIFICATE_AUTO_ISSUED),
            isCertificateIssued = cursor.getBoolean(DbStructureCourse.Columns.IS_CERTIFICATE_ISSUED),
            lastDeadline = cursor.getString(DbStructureCourse.Columns.LAST_DEADLINE),
            beginDate = cursor.getString(DbStructureCourse.Columns.BEGIN_DATE),
            endDate = cursor.getString(DbStructureCourse.Columns.END_DATE),
            slug = cursor.getString(DbStructureCourse.Columns.SLUG),

            scheduleLink = cursor.getString(DbStructureCourse.Columns.SCHEDULE_LINK),
            scheduleLongLink = cursor.getString(DbStructureCourse.Columns.SCHEDULE_LONG_LINK),
            scheduleType = cursor.getString(DbStructureCourse.Columns.SCHEDULE_TYPE),

            lastStepId = cursor.getString(DbStructureCourse.Columns.LAST_STEP),
            learnersCount = cursor.getLong(DbStructureCourse.Columns.LEARNERS_COUNT),
            reviewSummary = cursor.getLong(DbStructureCourse.Columns.REVIEW_SUMMARY),
            timeToComplete = cursor.getLong(DbStructureCourse.Columns.TIME_TO_COMPLETE),
            courseOptions = cursor.getString(DbStructureCourse.Columns.OPTIONS)?.toObject(gson),

            isPaid = cursor.getBoolean(DbStructureCourse.Columns.IS_PAID),
            price = cursor.getString(DbStructureCourse.Columns.PRICE),
            currencyCode = cursor.getString(DbStructureCourse.Columns.CURRENCY_CODE),
            displayPrice = cursor.getString(DbStructureCourse.Columns.DISPLAY_PRICE),
            priceTier = cursor.getString(DbStructureCourse.Columns.PRICE_TIER),
            defaultPromoCodeName = cursor.getString(DbStructureCourse.Columns.DEFAULT_PROMO_CODE_NAME),
            defaultPromoCodePrice = cursor.getString(DbStructureCourse.Columns.DEFAULT_PROMO_CODE_PRICE),
            defaultPromoCodeDiscount = cursor.getString(DbStructureCourse.Columns.DEFAULT_PROMO_CODE_DISCOUNT),
            defaultPromoCodeExpireDate = cursor.getDate(DbStructureCourse.Columns.DEFAULT_PROMO_CODE_EXPIRE_DATE)
        )

    private fun generateTestCourse(): Course =
        Course(
            id = 100,
            title = "title",
            description = "description",
            cover = "cover",
            certificate = "123",
            requirements = "567",
            summary = "summary",
            workload = null,
            intro = null,
            introVideo = Video(id = -1, thumbnail = null, urls = emptyList(), duration = 0),
            language = null,
            authors = null,
            instructors = null,
            sections = null,
            courseFormat = null,
            targetAudience = null,
            certificateFooter = null,
            certificateCoverOrg = null,
            totalUnits = 0,
            enrollment = 0,
            progress = null,
            owner = 0,
            readiness = 0.0,
            isContest = false,
            isFeatured = false,
            isActive = false,
            isPublic = false,
            isArchived = false,
            isFavorite = false,
            isProctored = false,
            certificateDistinctionThreshold = 0,
            certificateRegularThreshold = 0,
            certificateLink = null,
            isCertificateAutoIssued = false,
            isCertificateIssued = false,
            lastDeadline = null,
            beginDate = null,
            endDate = null,
            slug = null,
            scheduleLink = null,
            scheduleLongLink = null,
            scheduleType = null,
            lastStepId = null,
            learnersCount = 0,
            reviewSummary = 0,
            timeToComplete = 0,
            courseOptions = null,
            isPaid = false,
            price = null,
            currencyCode = null,
            displayPrice = null,
            priceTier = null,
            defaultPromoCodeName = null,
            defaultPromoCodePrice = null,
            defaultPromoCodeDiscount = null,
            defaultPromoCodeExpireDate = Date(0L)
        )
}