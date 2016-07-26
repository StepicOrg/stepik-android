package org.stepic.droid.presenters.certificate

import org.stepic.droid.core.CertificateView
import org.stepic.droid.model.CertificateViewItem
import org.stepic.droid.web.CertificateResponse
import org.stepic.droid.web.CoursesStepicResponse
import org.stepic.droid.web.IApi
import retrofit.Call
import retrofit.Callback
import retrofit.Response
import retrofit.Retrofit
import java.util.*

class CertificatePresenterImpl(val api: IApi) : CertificatePresenter {

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
                        api.getCourses(1, courseIds).enqueue(object : Callback<CoursesStepicResponse> {
                            override fun onFailure(t: Throwable?) {
                                certificateView?.onInternetProblem()
                            }

                            override fun onResponse(response: Response<CoursesStepicResponse>?, retrofit: Retrofit?) {
                                if (response?.isSuccess ?: false) {
                                    val listOfCourseIdAndTitle: List<Pair<Long, String>> = response?.body()?.courses?.mapNotNull { Pair(it.courseId, it.title) }?.orEmpty()!!
                                    certificateViewItemList?.clear()
                                    certificateViewItemList?.addAll(listOfCourseIdAndTitle.map { CertificateViewItem(it.second) })

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
