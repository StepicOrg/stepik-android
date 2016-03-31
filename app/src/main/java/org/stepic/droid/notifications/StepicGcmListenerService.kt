package org.stepic.droid.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.Bundle
import android.support.annotation.DrawableRes
import android.support.v4.app.NotificationCompat
import com.google.android.gms.gcm.GcmListenerService
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import org.stepic.droid.R
import org.stepic.droid.base.MainApplication
import org.stepic.droid.configuration.IConfig
import org.stepic.droid.model.Course
import org.stepic.droid.notifications.model.Notification
import org.stepic.droid.store.operations.DatabaseFacade
import org.stepic.droid.util.ColorUtil
import org.stepic.droid.util.HtmlHelper
import org.stepic.droid.view.activities.MainFeedActivity
import org.stepic.droid.web.IApi
import javax.inject.Inject

class StepicGcmListenerService : GcmListenerService() {

    @Inject
    lateinit var dbFacade: DatabaseFacade

    @Inject
    lateinit var api: IApi

    @Inject
    lateinit var configs: IConfig

    init {
        MainApplication.component().inject(this)
    }

    override fun onMessageReceived(from: String?, data: Bundle?) {
        val notificationRawString: String? = data?.getString("object")
        val stepicNotification = Gson().fromJson(notificationRawString, Notification::class.java)

        stepicNotification?.htmlText?.let {
            sendNotification(it)
        }
    }

    private fun sendNotification(message: String) {
        val intent = Intent(this, MainFeedActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT)

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val largeIcon = getPictureByCourseId()
        val colorArgb = ColorUtil.getColorArgb(R.color.stepic_brand_primary)

        val justText: String = HtmlHelper.fromHtml(message).toString()
        val notification = NotificationCompat.Builder(this)
                .setLargeIcon(largeIcon)
                .setSmallIcon(R.drawable.ic_matching)
                .setContentTitle("I handle this notification")
                .setContentText(justText)
                .setStyle(NotificationCompat.BigTextStyle()
                        .bigText(justText))
                .setColor(colorArgb)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)


        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.notify(0 /* ID of notification */, notification.build())
    }

    private fun getPictureByCourseId(courseId: Long = 67): Bitmap {
        var course: Course? = dbFacade.getCourseById(courseId, DatabaseFacade.Table.enrolled)
        if (course == null) {
            course = api.getCourse(courseId).execute()?.body()?.courses?.get(0)
        }

        val cover = course?.cover
        //FIXME create special icon for notification placeholder ?? dp in mdpi
        @DrawableRes val notificationPlaceholder = R.drawable.ic_course_placeholder

        if (cover == null) {
            return BitmapFactory.decodeResource(getResources(), notificationPlaceholder);
        } else {
            return Picasso.with(MainApplication.getAppContext())
                    .load(configs.baseUrl + cover)
                    .resize(200, 200) //pixels
                    .placeholder(notificationPlaceholder)
                    .error(notificationPlaceholder)
                    .get()
        }
    }
}