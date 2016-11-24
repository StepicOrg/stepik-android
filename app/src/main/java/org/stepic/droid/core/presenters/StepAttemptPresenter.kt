package org.stepic.droid.core.presenters

import android.support.annotation.MainThread
import android.support.annotation.WorkerThread
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.concurrency.IMainHandler
import org.stepic.droid.core.LessonSessionManager
import org.stepic.droid.core.presenters.contracts.StepAttemptView
import org.stepic.droid.model.*
import org.stepic.droid.web.IApi
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

class StepAttemptPresenter(val mainHandler: IMainHandler,
                           val threadPoolExecutor: ThreadPoolExecutor,
                           val lessonManager: LessonSessionManager,
                           val api: IApi,
                           val analytic: Analytic) : PresenterBase<StepAttemptView>() {

    companion object {
        private val FIRST_DELAY = 1000L
    }

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
        if (section?.discountingPolicy == null || section?.discountingPolicy == DiscountingPolicyType.noDiscount || numberOfSubmission < 0 || step.is_custom_passed) {
            view?.onResultHandlingDiscountPolicy(needShow = false)
            return
        }

        section?.discountingPolicy?.let {
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
    fun postSubmission(stepId: Long, reply: Reply, attemptId: Long) {
        threadPoolExecutor.execute {
            try {
                api.createNewSubmission(reply, attemptId).execute().body().submissions
                mainHandler.post {
                    getStatusOfSubmission(stepId, attemptId)
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
            if (step.actions?.do_review != null) {
                mainHandler.post {
                    view?.onNeedShowPeerReview()
                }
            }
        }
    }

    @MainThread
    fun getStatusOfSubmission(stepId: Long, attemptId: Long) {
        fun getStatusOfSubmission(stepId: Long, attemptId: Long, numberOfTry: Int) {
            worker?.schedule(
                    Runnable {
                        try {
                            val submissionsResponse = api.getSubmissions(attemptId).execute()
                            if (submissionsResponse.isSuccess) {
                                val submission = submissionsResponse.body().submissions.firstOrNull()
                                // if null ->  we do not have submissions for THIS ATTEMPT

                                if (submission?.status === Submission.Status.EVALUATION) {
                                    mainHandler.post {
                                        getStatusOfSubmission(stepId, attemptId, numberOfTry + 1)
                                    }
                                    return@Runnable
                                }

                                val numberOfSubmissions = api.getSubmissionForStep(stepId).execute().body().submissions.size
                                val isSubmissionCorrect = submission?.status == Submission.Status.CORRECT
                                mainHandler.post {
                                    if (isSubmissionCorrect) {
                                        view?.onUserPostedCorrectSubmission()
                                    }
                                    view?.onNeedFillSubmission(submission, numberOfSubmissions)
                                }

                            } else {
                                mainHandler.post {
                                    getStatusOfSubmission(stepId, attemptId, numberOfTry + 1)
                                }
                            }
                        } catch (ex: Exception) {
                            mainHandler.post {
                                view?.onConnectionFailOnSubmit()
                            }
                        }
                    }, numberOfTry * FIRST_DELAY, TimeUnit.MILLISECONDS)
        }

        getStatusOfSubmission(stepId, attemptId, 0)
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
            if (existingAttemptsResponse.isSuccess) {
                val body = existingAttemptsResponse.body()
                if (body == null) {
                    createNewAttempt(stepId)
                    return
                }

                val attemptList = body.attempts
                if (attemptList == null || attemptList.isEmpty() || attemptList[0].status != "active") {
                    createNewAttempt(stepId)
                } else {
                    val attempt = attemptList[0]
                    val numberOfSubmissionsForStep = api.getSubmissionForStep(stepId).execute().body().submissions.size //merge with outer request with RxJava
                    mainHandler.post {
                        view?.onNeedShowAttempt(attempt, false, numberOfSubmissionsForStep)
                    }
                }

            } else {
                createNewAttempt(stepId)
            }
        } catch (ex: Exception) {
            //Internet is not available
            mainHandler.post {
                view?.onConnectionFailWhenLoadAttempt()
            }
            analytic.reportError(Analytic.Error.NO_INTERNET_EXISTING_ATTEMPTS, ex)
        }
    }

    @WorkerThread
    private fun createNewAttempt(stepId: Long) {
        try {
            val createdAttempt: Attempt = api.createNewAttempt(stepId).execute().body().attempts.first()
            val numberOfSubmissions: Int = api.getSubmissionForStep(stepId).execute().body().submissions.size
            mainHandler.post { view?.onNeedShowAttempt(attempt = createdAttempt, numberOfSubmissionsForStep = numberOfSubmissions, isCreated = true) }
        } catch (ex: Exception) {
            //Internet is not available
            mainHandler.post {
                view?.onConnectionFailWhenLoadAttempt()
            }
            analytic.reportError(Analytic.Error.NO_INTERNET_EXISTING_ATTEMPTS, ex)
        }

    }

}
