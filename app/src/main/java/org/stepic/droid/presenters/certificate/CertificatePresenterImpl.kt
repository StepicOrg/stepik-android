package org.stepic.droid.presenters.certificate

import android.app.Activity
import org.stepic.droid.configuration.IConfig
import org.stepic.droid.core.CertificateView
import org.stepic.droid.core.IScreenManager
import org.stepic.droid.model.Certificate
import org.stepic.droid.model.CertificateViewItem
import org.stepic.droid.web.CertificateResponse
import org.stepic.droid.web.CoursesStepicResponse
import org.stepic.droid.web.IApi
import retrofit.Call
import retrofit.Callback
import retrofit.Response
import retrofit.Retrofit
import java.util.*

class CertificatePresenterImpl(val api: IApi, val config: IConfig, val screenManager: IScreenManager) : CertificatePresenter {
    override fun showShareDialogForCertificate(certificateViewItem: CertificateViewItem?) {
        view?.onNeedShowShareDialog(certificateViewItem)
    }

    override fun showCertificateAsPdf(activity: Activity, fullPath: String) {
        screenManager.showPdfInBrowserByGoogleDocs(activity,  fullPath)
    }

    override fun get(position: Int) = certificateViewItemList?.get(position)


    override fun size() = certificateViewItemList?.size ?: 0

    private var view: CertificateView? = null

    private var certificateViewItemList: ArrayList<CertificateViewItem>? = null

    override fun onCreate(certificateView: CertificateView) {
        this.view = certificateView
    }

    override fun onDestroy() {
        certificatesCall?.cancel()
        coursesCall?.cancel()
        view = null
    }

    private var certificatesCall: Call<CertificateResponse>? = null
    private var coursesCall: Call<CoursesStepicResponse>? = null

    override fun showCertificates() {
        if (certificateViewItemList == null) {
            //need load from internet
            view?.onLoading()
            loadCertificatesSilent()
        } else if (certificateViewItemList?.isEmpty() ?: false) {
            view?.showEmptyState()
        } else if (certificateViewItemList?.isNotEmpty() ?: false) {
            view?.onDataLoaded(certificateViewItemList)
        }
    }

    //if you reuse this function, you need handle view's callbacks carefully
    private fun loadCertificatesSilent() {
        certificatesCall = api.certificates
        certificatesCall?.enqueue(object : Callback<CertificateResponse> {

            override fun onResponse(response: Response<CertificateResponse>?, retrofit: Retrofit?) {
                if (response?.isSuccess ?: false) {
                    if (certificateViewItemList == null) {
                        certificateViewItemList = ArrayList()
                    }

                    val certificateList = response?.body()?.certificates
                    if (certificateList == null) {
                        view?.onInternetProblem()
                        return
                    }
                    if (certificateList.isEmpty()) {
                        view?.showEmptyState()
                        return
                    }

                    //certificate list is not empty:
                    val courseIds: LongArray = certificateList.mapNotNull { it.course }.toLongArray()
                    if (courseIds.isEmpty()) {
                        view?.onInternetProblem()
                    } else {
                        val courseIdToCertificateMap: Map<Long, Certificate> = certificateList
                                .filterNot { it.course == null }
                                .associateBy { it.course!! }
                        val baseUrl = config.baseUrl
                        api.getCourses(1, courseIds).enqueue(object : Callback<CoursesStepicResponse> {
                            override fun onFailure(t: Throwable?) {
                                view?.onInternetProblem()
                            }

                            override fun onResponse(response: Response<CoursesStepicResponse>?, retrofit: Retrofit?) {
                                if (response?.isSuccess ?: false) {
                                    val localCertificateViewItems: List<CertificateViewItem> = response
                                            ?.body()
                                            ?.courses
                                            ?.mapNotNull {
                                                val certificateRelatedToCourse = courseIdToCertificateMap[it.courseId]
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
                                                        grade = certificateRelatedToCourse?.grade
                                                )
                                            }
                                            ?.orEmpty()!!
                                    certificateViewItemList?.clear()
                                    certificateViewItemList?.addAll(localCertificateViewItems)

                                    view?.onDataLoaded(certificateViewItemList)
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

            override fun onFailure(t: Throwable?) {
                view?.onInternetProblem()
            }

        })
    }
}
