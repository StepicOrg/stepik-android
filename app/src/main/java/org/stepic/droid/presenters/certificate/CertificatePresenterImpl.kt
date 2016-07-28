package org.stepic.droid.presenters.certificate

import org.stepic.droid.configuration.IConfig
import org.stepic.droid.core.CertificateView
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

class CertificatePresenterImpl(val api: IApi, val config : IConfig) : CertificatePresenter {

    private var certificateView: CertificateView? = null

    private var certificateViewItemList: ArrayList<CertificateViewItem>? = null

    override fun onCreate(certificateView: CertificateView) {
        this.certificateView = certificateView
    }

    override fun onDestroy() {
        certificatesCall?.cancel()
        coursesCall?.cancel()
        certificateView = null
    }

    private var certificatesCall: Call<CertificateResponse>? = null
    private var coursesCall: Call<CoursesStepicResponse>? = null

    override fun showCertificates() {
        if (certificateViewItemList == null) {
            //need load from internet
            certificateView?.onLoading()
            loadCertificatesSilent()
        } else if (certificateViewItemList?.isEmpty() ?: false) {
            certificateView?.showEmptyState()
        } else if (certificateViewItemList?.isNotEmpty() ?: false) {
            certificateView?.onDataLoaded(certificateViewItemList)
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
                        certificateView?.onInternetProblem()
                        return
                    }
                    if (certificateList.isEmpty()) {
                        certificateView?.showEmptyState()
                        return
                    }

                    //certificate list is not empty:
                    val courseIds: LongArray = certificateList.mapNotNull { it.course }.toLongArray()
                    if (courseIds.isEmpty()) {
                        certificateView?.onInternetProblem()
                    } else {
                        val courseIdToCertificateMap: Map<Long, Certificate> = certificateList
                                .filterNot { it.course == null }
                                .associateBy { it.course!! }
                        val baseUrl = config.baseUrl
                        api.getCourses(1, courseIds).enqueue(object : Callback<CoursesStepicResponse> {
                            override fun onFailure(t: Throwable?) {
                                certificateView?.onInternetProblem()
                            }

                            override fun onResponse(response: Response<CoursesStepicResponse>?, retrofit: Retrofit?) {
                                if (response?.isSuccess ?: false) {
                                    val localCertificateViewItems: List<CertificateViewItem> = response
                                            ?.body()
                                            ?.courses
                                            ?.mapNotNull {
                                                val certificateRelatedToCourse = courseIdToCertificateMap[it.courseId]
                                                CertificateViewItem(
                                                        certificateRelatedToCourse?.id,
                                                        it.title,
                                                        baseUrl + it.cover,
                                                        certificateRelatedToCourse?.type,
                                                        certificateRelatedToCourse?.url
                                                )
                                            }
                                            ?.orEmpty()!!
                                    certificateViewItemList?.clear()
                                    certificateViewItemList?.addAll(localCertificateViewItems)

                                    certificateView?.onDataLoaded(certificateViewItemList)
                                } else {
                                    certificateView?.onInternetProblem()
                                }
                            }

                        })
                    }

                } else {
                    certificateView?.onInternetProblem()
                }
            }

            override fun onFailure(t: Throwable?) {
                certificateView?.onInternetProblem()
            }

        })
    }
}
