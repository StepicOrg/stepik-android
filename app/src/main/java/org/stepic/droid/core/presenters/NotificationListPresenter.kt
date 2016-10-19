package org.stepic.droid.core.presenters

import android.support.annotation.MainThread
import android.support.annotation.WorkerThread
import com.squareup.otto.Bus
import com.squareup.otto.Subscribe
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.concurrency.IMainHandler
import org.stepic.droid.configuration.IConfig
import org.stepic.droid.core.presenters.contracts.NotificationListView
import org.stepic.droid.events.notify_ui.NotificationCheckedSuccessfullyEvent
import org.stepic.droid.events.notify_ui.NotificationMarkCategoryAsReadEvent
import org.stepic.droid.notifications.model.Notification
import org.stepic.droid.notifications.model.NotificationType
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
        val config: IConfig,
        val bus: Bus,
        val analytic: Analytic
) : PresenterBase<NotificationListView>() {

    private var notificationCategory: NotificationCategory? = null
    val isLoading = AtomicBoolean(false)
    val wasShown = AtomicBoolean(false)
    val hasNextPage = AtomicBoolean(true)
    private val page = AtomicInteger(1)
    val notificationList: MutableList<Notification> = ArrayList<Notification>()
    val notificationMapIdToPosition: MutableMap<Long, Int> = HashMap()

    override fun attachView(view: NotificationListView) {
        super.attachView(view)
        bus.register(this)
    }

    override fun detachView(view: NotificationListView) {
        bus.unregister(this)
        super.detachView(view)
    }

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
                    notifications.forEachIndexed { position, notification ->
                        notification.id?.let {
                            notificationId ->
                            notificationMapIdToPosition[notificationId] = position
                        }
                    }
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
                    val oldSize = notificationList.size
                    notifications.forEachIndexed { shift, notification ->
                        notification.id?.let {
                            notificationId ->
                            notificationMapIdToPosition[notificationId] = shift + oldSize
                        }
                    }
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

    fun markAsRead(id: Long) {
        threadPoolExecutor.execute {
            try {
                val isSuccess = api.setReadStatusForNotification(id, true).execute().isSuccess
                if (isSuccess) {
                    mainHandler.post {
                        bus.post(NotificationCheckedSuccessfullyEvent(id))
                    }
                } else {
                    val pos = notificationMapIdToPosition[id]
                    mainHandler.post {
                        if (pos != null) {
                            view?.notCheckNotification(pos, id)
                        }
                    }
                }

            } catch (ex: Exception) {
                val pos = notificationMapIdToPosition[id]
                mainHandler.post {
                    if (pos != null) {
                        view?.notCheckNotification(pos, id)
                    }
                }
            }
        }
    }

    fun notificationIdIsNull() {
        analytic.reportEvent(Analytic.Notification.ID_WAS_NULL)
    }

    @Subscribe
    fun onNotificationShouldBeRead(event: NotificationCheckedSuccessfullyEvent) {
        val id = event.notificationId
        val position: Int = notificationMapIdToPosition[id] ?: return
        if (position >= 0 && position < notificationList.size) {
            val notificationInList = notificationList[position]
            if (notificationInList.is_unread ?: false) {
                view?.markNotificationAsRead(position, id)
            }
        }

    }

    @MainThread
    fun markAllAsRead() {
        val notificationCategoryLocal = notificationCategory
        if (notificationCategoryLocal == null) {
            analytic.reportEvent(Analytic.Notification.NOTIFICATION_NULL_POINTER)
        } else {
            view?.onLoadingMarkingAsRead()
            threadPoolExecutor.execute {
                try {
                    val response = api.markAsReadAllType(notificationCategoryLocal).execute()
                    if (response.isSuccess) {
                        notificationList.forEach {
                            it.is_unread = false
                        }
                        mainHandler.post {
                            bus.post(NotificationMarkCategoryAsReadEvent(notificationCategoryLocal))
                            view?.markAsReadSuccessfully()
                        }
                    }
                } catch (exception: Exception) {
                    mainHandler.post {
                        view?.onConnectionProblemWhenMarkAllFail()
                    }
                } finally {
                    mainHandler.post {
                        view?.makeEnableMarkAllButton()
                    }
                }
            }
        }

    }

    @Subscribe
    @MainThread
    fun onMarkCategoryRead(event: NotificationMarkCategoryAsReadEvent) {
        if (event.category == notificationCategory) {
            //already mark
            return
        }

        if (notificationCategory == null || (notificationCategory != NotificationCategory.all && event.category != NotificationCategory.all)) {
            //if we update in not all and it is not all -> do not need extra check
            return
        }

        val category = event.category
        threadPoolExecutor.execute {
            val listForNotificationForUI = notificationList
                    .filter {
                        it.is_unread ?: false
                    }
                    .filter {
                        val notCategory: NotificationCategory = when (it.type) {
                            NotificationType.comments -> NotificationCategory.comments
                            NotificationType.default -> NotificationCategory.default
                            NotificationType.review -> NotificationCategory.review
                            NotificationType.teach -> NotificationCategory.teach
                            NotificationType.learn -> NotificationCategory.learn
                            null -> NotificationCategory.all
                        }
                        notCategory == category
                    }

            val list: List <Pair<Int?, Long?>> = listForNotificationForUI.map {
                val first = notificationMapIdToPosition[it.id]
                Pair(first, it.id)
            }
            if (list.isNotEmpty()) {
                mainHandler.post {
                    list.forEach {
                        if (it.first != null && it.second != null) {
                            view?.markNotificationAsRead(it.first!!, it.second!!)
                        }
                    }
                }
            }
        }


    }

}