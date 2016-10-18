package org.stepic.droid.core.presenters

import android.support.annotation.MainThread
import android.support.annotation.WorkerThread
import org.stepic.droid.concurrency.IMainHandler
import org.stepic.droid.configuration.IConfig
import org.stepic.droid.core.presenters.contracts.NotificationListView
import org.stepic.droid.notifications.model.Notification
import org.stepic.droid.ui.NotificationCategory
import org.stepic.droid.util.not
import org.stepic.droid.web.IApi
import timber.log.Timber
import java.util.*
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class NotificationListPresenter(
        val threadPoolExecutor: ThreadPoolExecutor,
        val mainHandler: IMainHandler,
        val api: IApi,
        val config: IConfig
) : PresenterBase<NotificationListView>() {

    private var notificationCategory: NotificationCategory? = null
    val isLoading = AtomicBoolean(false)
    val wasShown = AtomicBoolean(false)
    val hasNextPage = AtomicBoolean(true)
    private val page = AtomicInteger(1)
    val notificationList: MutableList<Notification> = ArrayList<Notification>()

    @MainThread
    fun init(notificationCategory: NotificationCategory) {
        this.notificationCategory = notificationCategory
        if (!isLoading && !wasShown) {
            //it is not lock, it is just check, but we still can enter twice if we use it in multithreading way, but it is only for main thread.
            isLoading.set(true)
            if (notificationList.isNotEmpty()) {
                view?.onNeedShowNotifications(notificationList)
                wasShown.set(true)
                isLoading.set(false)
                return
            }

            threadPoolExecutor.execute {
                try {
                    val notifications = getNotificationFromOnePage(notificationCategory)
                    mainHandler.post {
                        notificationList.addAll(notifications)
                        wasShown.set(true)
                        view?.onNeedShowNotifications(notificationList) ?: wasShown.set(false)
                    }


                } catch (ex: Exception) {
                    mainHandler.post {
                        view?.onConnectionProblem()
                    }
                } finally {
                    isLoading.set(false)
                }
            }
        } else {
            //do nothing we loading or already loaded
            return
        }
    }

    @WorkerThread
    private fun getNotificationFromOnePage(notificationCategory: NotificationCategory): Iterable<Notification> {
        Timber.d("loading from page %d", page.get())
        val notificationResponse = api.getNotifications(notificationCategory, page.get()).execute().body()
        hasNextPage.set(notificationResponse.meta.has_next)
        page.set(notificationResponse.meta.page + 1)

        val baseUrl = config.baseUrl

        var notifications = notificationResponse.notifications
        Timber.d("before filter size is %d", notifications.size)
        notifications = notifications
                .filter {
                    it.htmlText?.isNotBlank() ?: false
                }

        notifications.forEach {
            val notificationHtmlText = it.htmlText ?: ""
            val fixedHtml = notificationHtmlText.replace("href=\"/", "href=\"$baseUrl/")
            it.htmlText = fixedHtml
        }
        Timber.d("after filter size is %d", notifications.size)
        return notifications
    }

    fun loadMore() {
        if (isLoading.get() || !hasNextPage) {
            return
        }

        //if is not loading:
        isLoading.set(true)
        view?.onNeedShowLoadingFooter()
        threadPoolExecutor.execute {
            try {
                notificationCategory?.let { category ->
                    val notifications = getNotificationFromOnePage(category)
                    mainHandler.post {
                        notificationList.addAll(notifications)
                        view?.onNeedShowNotifications(notificationList)
                    }
                }

            } catch (ex: Exception) {
                mainHandler.post {
                    view?.onConnectionProblem()
                }
            } finally {
                isLoading.set(false)
            }
        }

    }

}