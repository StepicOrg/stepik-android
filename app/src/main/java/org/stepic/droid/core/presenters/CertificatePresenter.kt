package org.stepic.droid.core.presenters

import android.app.Activity
import android.support.annotation.MainThread
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.concurrency.MainHandler
import org.stepic.droid.configuration.Config
import org.stepic.droid.core.ScreenManager
import org.stepic.droid.core.presenters.contracts.CertificateView
import org.stepic.droid.di.certificates.CertificateScope
import org.stepic.droid.model.CertificateViewItem
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.storage.operations.DatabaseFacade
import org.stepic.droid.web.Api
import org.stepic.droid.web.CertificateResponse
import org.stepic.droid.web.CoursesMetaResponse
import org.stepik.android.model.Certificate
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import java.util.concurrent.ThreadPoolExecutor
import javax.inject.Inject

@CertificateScope
class CertificatePresenter
@Inject constructor(
        private val api: Api,
        private val config: Config,
        private val screenManager: ScreenManager,
        private val database: DatabaseFacade,
        private val threadPoolExecutor: ThreadPoolExecutor,
        private val mainHandler: MainHandler,
        private val sharedPreferenceHelper: SharedPreferenceHelper,
        private val analytic: Analytic) : PresenterBase<CertificateView>() {

    private var certificateViewItemList: ArrayList<CertificateViewItem>? = null
    private var certificatesCall: Call<CertificateResponse>? = null
    private var coursesCall: Call<CoursesMetaResponse>? = null

    @MainThread
    fun showCertificates(isRefreshing: Boolean) {
        when {
            certificateViewItemList?.isEmpty() == true -> view?.showEmptyState()
            certificateViewItemList?.isNotEmpty() == true -> view?.onDataLoaded(certificateViewItemList)
            certificateViewItemList == null -> {
                //need load from internet
                if (!isRefreshing) {
                    view?.onLoading()
                }
                threadPoolExecutor.execute {
                    val isAnonymous = sharedPreferenceHelper.authResponseFromStore == null
                    if (isAnonymous) {
                        mainHandler.post {
                            view?.onAnonymousUser()
                        }
                        return@execute
                    }


                    certificateViewItemList = database.getAllCertificates()?.filterNotNull() as? ArrayList<CertificateViewItem>
                    mainHandler.post {
                        if (certificateViewItemList != null) {
                            view?.onDataLoaded(certificateViewItemList)
                        }

                        loadCertificatesSilent()
                    }
                }
            }
        }
    }

    //if you reuse this function, you need handle getView's callbacks carefully
    @MainThread
    private fun loadCertificatesSilent() {
        certificatesCall = api.certificates
        certificatesCall?.enqueue(object : Callback<CertificateResponse> {
            override fun onResponse(call: Call<CertificateResponse>?, response: Response<CertificateResponse>?) {
                if (response?.isSuccessful == true) {
                    if (certificateViewItemList == null) {
                        certificateViewItemList = ArrayList()
                    }

                    val certificateList = response.body()?.certificates
                    if (certificateList == null) {
                        view?.onInternetProblem()
                        return
                    }
                    if (certificateList.isEmpty()) {
                        view?.showEmptyState()
                        return
                    }

                    //certificate oldList is not empty:
                    val courseIds: LongArray = certificateList.mapNotNull { it.course }.toLongArray()
                    if (courseIds.isEmpty()) {
                        view?.onInternetProblem()
                    } else {
                        val courseIdToCertificateMap: Map<Long, Certificate> = certificateList
                                .filterNot { it.course == null }
                                .associateBy { it.course!! }
                        val baseUrl = config.baseUrl
                        coursesCall = api.getCourses(1, courseIds)
                        coursesCall?.enqueue(object : Callback<CoursesMetaResponse> {

                            override fun onFailure(call: Call<CoursesMetaResponse>?, t: Throwable?) {
                                view?.onInternetProblem()
                            }

                            override fun onResponse(call: Call<CoursesMetaResponse>?, response: Response<CoursesMetaResponse>?) {
                                if (response?.isSuccessful == true) {
                                    val localCertificateViewItems: List<CertificateViewItem> = response
                                            .body()
                                            ?.courses
                                            ?.mapNotNull {
                                                val certificateRelatedToCourse = courseIdToCertificateMap[it.id]
                                                var cover: String? = null
                                                if (it.cover != null) {
                                                    cover = baseUrl + it.cover
                                                }
                                                CertificateViewItem(
                                                        certificateRelatedToCourse?.id,
                                                        it.title,
                                                        cover,
                                                        certificateRelatedToCourse?.type,
                                                        certificateRelatedToCourse?.url,
                                                        certificateRelatedToCourse?.grade,
                                                        certificateRelatedToCourse?.issueDate
                                                )
                                            }.orEmpty()
                                    certificateViewItemList?.clear()
                                    certificateViewItemList?.addAll(localCertificateViewItems)

                                    view?.onDataLoaded(certificateViewItemList)
                                    threadPoolExecutor.execute {
                                        database.addCertificateViewItems(localCertificateViewItems)
                                    }
                                } else {
                                    view?.onInternetProblem()
                                }
                            }

                        })
                    }

                } else {
                    view?.onInternetProblem()
                }
            }

            override fun onFailure(call: Call<CertificateResponse>?, t: Throwable?) {
                view?.onInternetProblem()
            }

        })
    }

    fun size() = certificateViewItemList?.size ?: 0

    fun showShareDialogForCertificate(certificateViewItem: CertificateViewItem?) {
        analytic.reportEvent(Analytic.Certificate.CLICK_SHARE_MAIN)
        view?.onNeedShowShareDialog(certificateViewItem)
    }

    fun showCertificateAsPdf(activity: Activity, fullPath: String) {
        analytic.reportEvent(Analytic.Certificate.OPEN_IN_BROWSER)
        screenManager.showPdfInBrowserByGoogleDocs(activity, fullPath)
    }

    fun get(position: Int) = certificateViewItemList?.get(position)

    override fun detachView(view: CertificateView) {
        super.detachView(view)
        // cancel should work not on main thread for com.squareup.retrofit:retrofit:2.0.0-beta2 (look https://github.com/square/okhttp/issues/1592)
//        certificatesCall?.cancel()
//        coursesCall?.cancel()
    }
}
