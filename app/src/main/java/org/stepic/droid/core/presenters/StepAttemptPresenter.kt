package org.stepic.droid.core.presenters

import android.os.Bundle
import android.support.annotation.MainThread
import android.support.annotation.WorkerThread
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import org.stepic.droid.analytic.AmplitudeAnalytic
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.concurrency.MainHandler
import org.stepic.droid.configuration.RemoteConfig
import org.stepic.droid.core.LessonSessionManager
import org.stepic.droid.core.presenters.contracts.StepAttemptView
import org.stepic.droid.model.*
import org.stepik.android.model.learning.replies.Reply
import org.stepik.android.model.learning.attempts.Attempt
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.util.DateTimeHelper
import org.stepic.droid.util.StepikUtil
import org.stepic.droid.util.getStepType
import org.stepic.droid.web.Api
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class StepAttemptPresenter
@Inject constructor(
        private val mainHandler: MainHandler,
        private val threadPoolExecutor: ThreadPoolExecutor,
        private val lessonManager: LessonSessionManager,
        private val api: Api,
        private var firebaseRemoteConfig: FirebaseRemoteConfig,
        private val analytic: Analytic,
        private val sharedPreferenceHelper: SharedPreferenceHelper
) : PresenterBase<StepAttemptView>() {
    companion object {
        private const val FIRST_DELAY = 1000L
        private const val MAX_RATE_TIMES = 5
    }

    private val minNumberOfSolvedStepsForRate = 5

    private var worker: ScheduledExecutorService? = null

    override fun attachView(view: StepAttemptView) {
        super.attachView(view)
        worker = Executors.newSingleThreadScheduledExecutor()
    }

    override fun detachView(view: StepAttemptView) {
        worker = null
        super.detachView(view)
    }

    @MainThread
    fun handleStepRestriction(step: Step, numberOfSubmission: Int) {
        if (!step.hasSubmissionRestriction) {
            view?.onResultHandlingSubmissionRestriction(needShow = false, numberForShow = 0)
        } else {
            val remainTries = step.maxSubmissionCount - numberOfSubmission
            view?.onResultHandlingSubmissionRestriction(needShow = true, numberForShow = remainTries)
        }
    }

    @MainThread
    fun handleDiscountingPolicy(numberOfSubmission: Int, section: Section?, step: Step) {
        if (section?.discountingPolicy == null || section.discountingPolicy == DiscountingPolicyType.noDiscount || numberOfSubmission < 0 || step.is_custom_passed) {
            view?.onResultHandlingDiscountPolicy(needShow = false)
            return
        }

        section.discountingPolicy?.let {
            when (section.discountingPolicy) {
                DiscountingPolicyType.inverse -> view?.onResultHandlingDiscountPolicy(
                        needShow = true,
                        discountingPolicyType = it,
                        remainTries = Int.MAX_VALUE)

                DiscountingPolicyType.firstOne, DiscountingPolicyType.firstThree -> view?.onResultHandlingDiscountPolicy(
                        needShow = true,
                        discountingPolicyType = it,
                        remainTries = (it.numberOfTries() - numberOfSubmission))

                else -> view?.onResultHandlingDiscountPolicy(needShow = false)
            }
        }
    }

    @MainThread
    fun postSubmission(step: Step, reply: Reply, attemptId: Long) {
        threadPoolExecutor.execute {
            try {
                api.createNewSubmission(reply, attemptId).execute().body()?.submissions
                val bundle = Bundle()
                bundle.putString(Analytic.Steps.STEP_TYPE_KEY, step.getStepType())
                reply.language?.let {
                    bundle.putString(Analytic.Steps.CODE_LANGUAGE_KEY, reply.language)
                }
                analytic.reportEvent(Analytic.Steps.SUBMISSION_CREATED, bundle)
                mainHandler.post {
                    getStatusOfSubmission(step, attemptId, fromPosting = true)
                }
            } catch (ex: Exception) {
                mainHandler.post { view?.onConnectionFailOnSubmit() }
            }
        }
    }

    @JvmOverloads
    @MainThread
    fun startLoadAttempt(step: Step, onlyFromInternet: Boolean = false) {
        startWork(step, onlyFromInternet)
    }

    @MainThread
    fun tryAgain(stepId: Long) {
        threadPoolExecutor.execute {
            createNewAttempt(stepId)
        }
    }

    private fun startWork(step: Step, onlyFromInternet: Boolean) {
        view?.onStartLoadingAttempt()
        view?.onNeedResolveActionButtonText()
        threadPoolExecutor.execute {
            if (onlyFromInternet || !tryRestoreState(step.id)) {
                getExistingAttempts(step.id)
            }
            if (step.actions?.doReview != null) {
                mainHandler.post {
                    view?.onNeedShowPeerReview()
                }
            }
        }
    }

    @JvmOverloads
    @MainThread
    fun getStatusOfSubmission(step: Step, attemptId: Long, fromPosting: Boolean = false) {

        fun getStatusOfSubmission(numberOfTry: Int) {
            worker?.schedule(
                    Runnable {
                        try {
                            var submissions = api.getSubmissionForStep(step.id).execute().body()?.submissions
                            if (submissions?.isNotEmpty() == true && submissions.firstOrNull()?.attempt != attemptId) {
                                submissions = api.getSubmissions(attemptId).execute()?.body()?.submissions // if we have another attempt id on server
                            }


                            if (submissions != null) {
                                val numberOfSubmissions = submissions.size
                                val submission = submissions.firstOrNull()
                                // if null ->  we do not have submissions for THIS ATTEMPT

                                if (submission?.status == Submission.Status.EVALUATION) {
                                    mainHandler.post {
                                        getStatusOfSubmission(numberOfTry + 1)
                                    }
                                    return@Runnable
                                }

                                val isCorrectSolution: Boolean = !step.is_custom_passed && submission?.status == Submission.Status.CORRECT
                                if (isCorrectSolution) {
                                    sharedPreferenceHelper.trackWhenUserSolved()
                                    sharedPreferenceHelper.incrementUserSolved()
                                }

                                if (fromPosting && submission != null) {
                                    sharedPreferenceHelper.incrementSubmissionsCount()

                                    val params = mutableMapOf(
                                            AmplitudeAnalytic.Steps.Params.TYPE to step.getStepType(),
                                            AmplitudeAnalytic.Steps.Params.STEP to step.id
                                    )
                                    submission.reply?.language?.let {
                                        params[AmplitudeAnalytic.Steps.Params.LANGUAGE] = it
                                    }
                                    analytic.reportAmplitudeEvent(AmplitudeAnalytic.Steps.SUBMISSION_MADE, params)
                                }

                                val needShowStreakDialog =
                                        fromPosting
                                                && isCorrectSolution
                                                && (sharedPreferenceHelper.isStreakNotificationEnabledNullable == null) // default value, user not change in profile
                                                && sharedPreferenceHelper.canShowStreakDialog()
                                                && (sharedPreferenceHelper.authResponseFromStore != null)


                                val streakDayNumber: Int =
                                        if (needShowStreakDialog) {
                                            try {
                                                val pins: ArrayList<Long> = api.getUserActivities(sharedPreferenceHelper.profile?.id ?: throw Exception("User is not auth")).execute()?.body()?.userActivities?.firstOrNull()?.pins!!
                                                val pair = StepikUtil.getCurrentStreakExtended(pins)
                                                pair.currentStreak
                                            } catch (exception: Exception) {
                                                analytic.reportError(Analytic.Error.STREAK_ON_STEP_SOLVED, exception)
                                                -1
                                            }
                                        } else {
                                            -1
                                        }


                                val needShowRateAppDialog = !needShowStreakDialog
                                        && fromPosting
                                        && isCorrectSolution
                                        && !sharedPreferenceHelper.wasRateHandled()
                                        && isUserSolveEnough()
                                        && isRateDelayGreater()
                                        && isRateWasShownFewTimes()

                                if (!needShowStreakDialog && needShowRateAppDialog) {
                                    sharedPreferenceHelper.rateShown(DateTimeHelper.nowUtc())
                                }

                                mainHandler.post {
                                    if (needShowStreakDialog) {
                                        view?.onNeedShowStreakDialog(streakDayNumber) // it can be -1, if we fail to get streaks
                                    } else if (needShowRateAppDialog) {
                                        view?.onNeedShowRateDialog()
                                    }
                                    view?.onNeedFillSubmission(submission, numberOfSubmissions)
                                }

                            } else {
                                mainHandler.post {
                                    getStatusOfSubmission(numberOfTry + 1)
                                }
                            }
                        } catch (ex: Exception) {
                            mainHandler.post {
                                view?.onConnectionFailOnSubmit()
                            }
                        }
                    }, numberOfTry * FIRST_DELAY, TimeUnit.MILLISECONDS)
        }

        getStatusOfSubmission(0)
    }

    @WorkerThread
    private fun isRateDelayGreater(): Boolean {
        val wasShownMillis = sharedPreferenceHelper.whenRateWasShown()
        if (wasShownMillis < 0) {
            return true // we can show it
        }

        val delayMillis = firebaseRemoteConfig.getLong(RemoteConfig.MIN_DELAY_RATE_DIALOG_SEC).toInt() * 1000L

        return DateTimeHelper.isBeforeNowUtc(delayMillis + wasShownMillis) //if delay is expired (before now) -> show
    }

    @WorkerThread
    private fun isRateWasShownFewTimes(): Boolean {
        val wasShown = sharedPreferenceHelper.howManyRateWasShownBefore()
        return wasShown <= MAX_RATE_TIMES
    }

    @WorkerThread
    private fun isUserSolveEnough(): Boolean {
        val numberOfSolved = sharedPreferenceHelper.numberOfSolved()
        return numberOfSolved >= minNumberOfSolvedStepsForRate
    }


    /**
     * @return false if restore was failed;
     */
    @WorkerThread
    private fun tryRestoreState(stepId: Long): Boolean {
        val lessonSession = lessonManager.restoreLessonSession(stepId) ?: return false

        val attempt = lessonSession.attempt
        val submission = lessonSession.submission
        val numberOfSubmissions = lessonSession.numberOfSubmissionsOnFirstPage
        if (submission == null || attempt == null) return false

        mainHandler.post {
            view?.onNeedShowAttempt(attempt, false, numberOfSubmissions)
            view?.onNeedFillSubmission(submission, numberOfSubmissions)
        }
        return true
    }


    @WorkerThread
    private fun getExistingAttempts(stepId: Long) {
        try {
            val existingAttemptsResponse = api.getExistingAttempts(stepId).execute()
            val firstAttempt = existingAttemptsResponse?.body()?.attempts?.firstOrNull()
            if (existingAttemptsResponse.isSuccessful && firstAttempt?.status == "active") {
                mainHandler.post {
                    // we do not need number of submissions, because it will be calculated in getSubmissions
                    view?.onNeedShowAttempt(firstAttempt, false, null)
                }
            } else {
                createNewAttempt(stepId)
            }
        } catch (ex: Exception) {
            //Internet is not available
            mainHandler.post {
                view?.onConnectionFailWhenLoadAttempt()
            }
        }
    }

    @WorkerThread
    private fun createNewAttempt(stepId: Long) {
        try {
            val createdAttempt: Attempt = api.createNewAttempt(stepId).execute().body()!!.attempts.first()
            val numberOfSubmissions: Int = api.getSubmissionForStep(stepId).execute().body()!!.submissions.size
            mainHandler.post { view?.onNeedShowAttempt(attempt = createdAttempt, numberOfSubmissionsForStep = numberOfSubmissions, isCreated = true) }
        } catch (ex: Exception) {
            //Internet is not available
            mainHandler.post {
                view?.onConnectionFailWhenLoadAttempt()
            }
        }

    }

}
