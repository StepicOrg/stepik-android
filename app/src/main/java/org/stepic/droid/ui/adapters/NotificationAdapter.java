package org.stepic.droid.ui.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.stepic.droid.R;
import org.stepic.droid.base.MainApplication;
import org.stepic.droid.configuration.IConfig;
import org.stepic.droid.notifications.model.Notification;
import org.stepic.droid.util.AppConstants;
import org.stepic.droid.util.DateTimeHelper;
import org.stepic.droid.util.resolvers.text.TextResolver;

import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;
import uk.co.chrisjenx.calligraphy.CalligraphyUtils;
import uk.co.chrisjenx.calligraphy.TypefaceUtils;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.GenericViewHolder> {

    private final int itemViewType = 1;
    private final int headerViewType = 2;
    private final int countOfHeads = 1;
    private final Typeface boldTypeface;
    private final Typeface regularTypeface;

    private Context context;
    private DateTimeZone zone;
    private Locale locale;
    private int countOfItems = 500;
    private List<Notification> notifications;

    public NotificationAdapter(Context context) {
        this.context = context;
        zone = DateTimeZone.getDefault();
        locale = Locale.getDefault();
        boldTypeface = TypefaceUtils.load(context.getAssets(), "fonts/NotoSans-Bold.ttf");
        regularTypeface = TypefaceUtils.load(context.getAssets(), "fonts/NotoSans-Regular.ttf");
    }

    @Override
    public GenericViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Timber.d("createViewHolder of NotificationAdapter, viewType = %d", viewType);
        if (viewType == headerViewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.notification_list_header_item, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.notification_item, parent, false);
            return new NotificationViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(GenericViewHolder holder, int position) {
        holder.setData(position);
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? headerViewType : itemViewType;
    }

    @NonNull
    private Notification getFakeNotification(int position) {
        Notification notification = new Notification();
        String htmlTextFromNotification = position + " В курсе <a href=\"/course/Java-%D0%91%D0%B0%D0%B7%D0%BE%D0%B2%D1%8B%D0%B9-%D0%BA%D1%83%D1%80%D1%81-187/\">Java. Базовый курс</a> менее чем через 36 часов наступит совсем крайний срок сдачи заданий по модулю <a href=\"/course/Java-%D0%91%D0%B0%D0%B7%D0%BE%D0%B2%D1%8B%D0%B9-%D0%BA%D1%83%D1%80%D1%81-187/syllabus?module=4\">Обработка ошибок, исключения, отладка</a> МДА";
        notification.setHtmlText(htmlTextFromNotification);
        notification.setTime("2016-10-12T19:05:00Z");
        return notification;
    }


    @Override
    public int getItemCount() {
        return countOfItems + countOfHeads;
    }

    public void setNotifications(List<Notification> notifications) {
        this.notifications = notifications;
        notifyDataSetChanged();
    }


    abstract class GenericViewHolder extends RecyclerView.ViewHolder {

        public GenericViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        abstract void setData(int position);
    }

    public class NotificationViewHolder extends GenericViewHolder {

        @Inject
        TextResolver textResolver;

        @Inject
        IConfig config;

        @BindView(R.id.notification_body)
        TextView notificationBody;

        @BindView(R.id.notification_root)
        ViewGroup notificationRoot;

        @BindView(R.id.check_view)
        CheckBox checkImageView;

        @BindView(R.id.notification_time)
        TextView notificationTime;


        NotificationViewHolder(View itemView) {
            super(itemView);
            MainApplication.component().inject(this);
            notificationBody.setMovementMethod(LinkMovementMethod.getInstance());
            notificationRoot.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    checkImageView.dispatchTouchEvent(event);
                    return true;
                }
            });
        }

        public void setData(int position) {
            Notification notification = getFakeNotification(position);
            //<FIXME>
            // this should be parsed, when we receive notification on background thread

            String baseUrl = config.getBaseUrl();
            String notificationHtmlText = notification.getHtmlText();
            String fixedHtml = notificationHtmlText.replace("href=\"/", "href=\"" + baseUrl + "/");

            // </FIXME>

            notificationBody.setText(textResolver.fromHtml(fixedHtml));
            String timeForView = DateTimeHelper.INSTANCE.getPresentOfDate(notification.getTime(), DateTimeFormat.forPattern(AppConstants.COMMENT_DATE_TIME_PATTERN).withZone(zone).withLocale(locale)); // // TODO: 13.10.16 save in ViewModel
            notificationTime.setText(timeForView);

            makeViewed(position % 3 == 0);
        }

        private void makeViewed(boolean viewed) {
            if (viewed) {
                CalligraphyUtils.applyFontToTextView(notificationBody, regularTypeface);
            } else {
                CalligraphyUtils.applyFontToTextView(notificationBody, boldTypeface);
            }
        }
    }

    class HeaderViewHolder extends GenericViewHolder {

        @BindView(R.id.mark_all_as_read_button)
        View markAllAsViewed;

        public HeaderViewHolder(View itemView) {
            super(itemView);
        }

        @Override
        void setData(int position) {
            //it is called in onBind method
        }
    }
}
