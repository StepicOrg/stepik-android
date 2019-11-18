package org.stepic.droid.core.presenters

import android.util.Patterns
import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import androidx.collection.ArraySet
import androidx.collection.LongSparseArray
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.Client
import org.stepic.droid.concurrency.MainHandler
import org.stepic.droid.configuration.Config
import org.stepic.droid.core.internetstate.contract.InternetEnabledListener
import org.stepic.droid.core.presenters.contracts.NotificationListView
import org.stepic.droid.di.notifications.NotificationsScope
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepic.droid.model.NotificationCategory
import org.stepic.droid.notifications.badges.NotificationsBadgesManager
import org.stepic.droid.notifications.model.Notification
import org.stepic.droid.notifications.model.NotificationType
import org.stepic.droid.util.DateTimeHelper
import org.stepic.droid.util.not
import org.stepic.droid.util.substringOrNull
import org.stepik.android.data.user.source.UserRemoteDataSource
import org.stepik.android.domain.notification.repository.NotificationRepository
import org.stepik.android.view.notification.FcmNotificationHandler
import timber.log.Timber
import java.util.ArrayList
import java.util.Calendar
import java.util.HashMap
import java.util.TimeZone
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

@NotificationsScope
class NotificationListPresenter
@Inject constructor(
    private val threadPoolExecutor: ThreadPoolExecutor,
    private val mainHandler: MainHandler,
    private val notificationRepository: NotificationRepository,
    private val userRemoteDataSource: UserRemoteDataSource,

    @MainScheduler
    private val mainScheduler: Scheduler,

    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,

    private val config: Config,
    private val analytic: Analytic,
    private val fcmNotificationHandler: FcmNotificationHandler,
    private val internetEnabledListenerClient: Client<InternetEnabledListener>,
    private val notificationsBadgesManager: NotificationsBadgesManager
) : PresenterBase<NotificationListView>(), InternetEnabledListener {
    private var notificationCategory: NotificationCategory? = null
    val isLoading = AtomicBoolean(false)
    val wasShown = AtomicBoolean(false)
    val hasNextPage = AtomicBoolean(true)
    private val page = AtomicInteger(1)
    val notificationList: MutableList<Notification> = ArrayList()
    val notificationMapIdToPosition: MutableMap<Long, Int> = HashMap()

    private val compositeDisposable = CompositeDisposable()

    /**
     * return false if were cancelled
     */
    @MainThread
    fun init(notificationCategory: NotificationCategory): Boolean {
        notificationsBadgesManager.syncCounter()
        this.notificationCategory = notificationCategory
        if (!isLoading && !wasShown) {
            //it is not lock, it is just check, but we still can enter twice if we use it in multithreading way, but it is only for main thread.
            isLoading.set(true)
            view?.onLoading()
            if (notificationList.isNotEmpty()) {
                view?.onNeedShowNotifications(notificationList)
                wasShown.set(true)
                isLoading.set(false)
            }

            threadPoolExecutor.execute {
                try {
                    val notifications = getNotificationFromOnePage(notificationCategory)
                    notifications.forEachIndexed { position, notification ->
                        notification.id?.let { notificationId ->
                            notificationMapIdToPosition[notificationId] = position
                        }
                    }
                    mainHandler.post {
                        notificationList.addAll(notifications)
                        resolveNotificationsDateGroup()
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
            return false
        } else {
            if (!isLoading) {
                view?.onNeedShowNotifications(notificationList)
            }
            //do nothing we loading or already loaded
            return true
        }
    }

    @WorkerThread
    private fun getNotificationFromOnePage(notificationCategory: NotificationCategory): Iterable<Notification> {
        Timber.d("loading from page %d", page.get())
        val notifications = notificationRepository.getNotifications(notificationCategory, page.get()).blockingGet() ?: throw NullPointerException("notifications null body")
        hasNextPage.set(notifications.hasNext)
        page.set(notifications.page + 1)

        val baseUrl = config.baseUrl

        Timber.d("before filter size is %d", notifications.size)
        val filteredNotifications = notifications
                .filter {
                    it.htmlText?.isNotBlank() ?: false
                }

        val userIdToNotificationsIndexes = LongSparseArray<MutableList<Int>>()  // userId -> notifications index where avatar should be set
        val userIds = ArraySet<Long>()

        filteredNotifications.forEachIndexed { index, notification ->
            val notificationHtmlText = notification.htmlText ?: ""
            val fixedHtml = notificationHtmlText.replace("href=\"/", "href=\"$baseUrl/")
            notification.htmlText = fixedHtml

            if (notification.type == NotificationType.comments) {
                extractUserAvatarUrl(notification)?.let { userId ->
                    userIdToNotificationsIndexes.putIfAbsent(userId, ArrayList())
                    userIdToNotificationsIndexes[userId]?.add(index)
                    userIds.add(userId)
                }
            }
        }

        if (userIds.isNotEmpty()) {
            userRemoteDataSource.getUsers(*userIds.toLongArray()).blockingGet().forEach {
                val avatar = it.avatar
                userIdToNotificationsIndexes[it.id]?.forEach { notificationIndex ->
                    notifications[notificationIndex].userAvatarUrl = avatar
                }
            }
        }

        Timber.d("after filter size is %d", notifications.size)
        return notifications
    }

    @WorkerThread
    private fun extractUserAvatarUrl(notification: Notification): Long? {
        val matcher = Regex(Patterns.WEB_URL.pattern()) // used kotlin Regex instead of android Pattern due to unstable work of Pattern on different Android versions
        notification.htmlText?.let { matcher.find(it) } ?.groupValues?.firstOrNull()?.let { userUrl ->
            val start = userUrl.lastIndexOf('/')
            return userUrl.substringOrNull(start + 1)?.toLongOrNull()
        }
        return null
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
                        notification.id?.let { notificationId ->
                            notificationMapIdToPosition[notificationId] = shift + oldSize
                        }
                    }
                    mainHandler.post {
                        notificationList.addAll(notifications)
                        resolveNotificationsDateGroup()
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
        compositeDisposable += notificationRepository
            .putNotifications(id, isRead = true)
            .observeOn(mainScheduler)
            .subscribeOn(backgroundScheduler)
            .subscribeBy(
                onComplete = {
                    onNotificationShouldBeRead(id)
                    notificationsBadgesManager.syncCounter()
                },
                onError = {
                    val pos = notificationMapIdToPosition[id]
                    if (pos != null) {
                        view?.notCheckNotification(pos, id)
                    }
                }
            )
    }

    fun notificationIdIsNull() {
        analytic.reportEvent(Analytic.Notification.ID_WAS_NULL)
    }

    fun onNotificationShouldBeRead(notificationId: Long) {
        val position: Int = notificationMapIdToPosition[notificationId] ?: return
        if (position >= 0 && position < notificationList.size) {
            val notificationInList = notificationList[position]
            if (notificationInList.isUnread ?: false) {
                view?.markNotificationAsRead(position, notificationId)
            }
        }

    }

    @MainThread
    fun markAllAsRead() {
        val notificationCategoryLocal = notificationCategory
        if (notificationCategoryLocal == null) {
            analytic.reportEvent(Analytic.Notification.NOTIFICATION_NULL_POINTER)
        } else {
            analytic.reportEvent(Analytic.Notification.MARK_ALL_AS_READ)
            view?.onLoadingMarkingAsRead()
            threadPoolExecutor.execute {
                try {
                    notificationRepository.markNotificationAsRead(notificationCategoryLocal).blockingAwait()
                    notificationsBadgesManager.syncCounter()
                    notificationList.forEach {
                        it.isUnread = false
                    }
                    mainHandler.post {
                        onMarkCategoryRead(notificationCategoryLocal)
                        view?.markAsReadSuccessfully()
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

    @MainThread
    fun onMarkCategoryRead(category: NotificationCategory) {
        if (category == notificationCategory) {
            //already mark
            return
        }

        if (notificationCategory == null || (notificationCategory != NotificationCategory.all && category != NotificationCategory.all)) {
            //if we update in not all and it is not all -> do not need extra check
            return
        }

        threadPoolExecutor.execute {
            val listForNotificationForUI = notificationList
                    .filter {
                        it.isUnread ?: false
                    }
                    .filter {
                        if (category == NotificationCategory.all) {
                            true
                        } else {
                            val notCategory: NotificationCategory = when (it.type) {
                                NotificationType.comments -> NotificationCategory.comments
                                NotificationType.other -> NotificationCategory.default
                                NotificationType.review -> NotificationCategory.review
                                NotificationType.teach -> NotificationCategory.teach
                                NotificationType.learn -> NotificationCategory.learn
                                null -> NotificationCategory.all
                            }
                            notCategory == category
                        }
                    }

            val list: List<Pair<Int?, Long?>> = listForNotificationForUI.map {
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

    private fun resolveNotificationsDateGroup() {
        var groupId = -1
        var groupDay = -1
        var groupYear = -1
        notificationList.forEach { notification ->
            notification.time?.let { time ->
                val date = DateTimeHelper.toCalendar(time)
                date.timeZone = TimeZone.getDefault()

                val day = date.get(Calendar.DAY_OF_YEAR)
                val year = date.get(Calendar.YEAR)
                if (day != groupDay || year != groupYear) {
                    groupDay = day
                    groupYear = year
                    groupId++
                }

                notification.dateGroup = groupId
            }
        }
    }

    override fun attachView(view: NotificationListView) {
        super.attachView(view)
        internetEnabledListenerClient.subscribe(this)
    }

    override fun detachView(view: NotificationListView) {
        super.detachView(view)
        internetEnabledListenerClient.unsubscribe(this)
        compositeDisposable.clear()
    }


    override fun onInternetEnabled() {
        val category = notificationCategory
        if (notificationList.isEmpty() && category != null) {
            init(category);
        }
    }

    fun tryToOpenNotification(notification: Notification) {
        analytic.reportEvent(Analytic.Notification.NOTIFICATION_CENTER_OPENED)
        fcmNotificationHandler.tryOpenNotificationInstantly(notification)
    }

    fun trackClickOnNotification(notification: Notification) {
        analytic.reportEventWithIdName(Analytic.Notification.NOTIFICATION_CLICKED_IN_CENTER, notification.id.toString(), notification.type?.name ?: "")
    }

}