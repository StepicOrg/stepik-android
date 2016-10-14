package org.stepic.droid.ui.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.stepic.droid.R;
import org.stepic.droid.base.MainApplication;
import org.stepic.droid.notifications.model.Notification;
import org.stepic.droid.util.AppConstants;
import org.stepic.droid.util.DateTimeHelper;
import org.stepic.droid.util.resolvers.text.TextResolver;

import java.util.Locale;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.GenericViewHolder> {

    private final int itemViewType = 1;
    private final int headerViewType = 2;
    private final int countOfHeads = 1;

    private Context context;
    private DateTimeZone zone;
    private Locale locale;
    private int countOfItems;

    public NotificationAdapter(Context context) {
        this.context = context;
        zone = DateTimeZone.getDefault();
        locale = Locale.getDefault();
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
        notification.setHtmlText(position + " В курсе <a href=\"/course/Java-%D0%91%D0%B0%D0%B7%D0%BE%D0%B2%D1%8B%D0%B9-%D0%BA%D1%83%D1%80%D1%81-187/\">Java. Базовый курс</a> менее чем через 36 часов наступит совсем крайний срок сдачи заданий по модулю <a href=\"/course/Java-%D0%91%D0%B0%D0%B7%D0%BE%D0%B2%D1%8B%D0%B9-%D0%BA%D1%83%D1%80%D1%81-187/syllabus?module=4\">Обработка ошибок, исключения, отладка</a> ");
        notification.setTime("2016-10-12T19:05:00Z");
        return notification;
    }


    @Override
    public int getItemCount() {
        countOfItems = 500;
        return countOfItems + countOfHeads;
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

        @BindView(R.id.notification_body)
        TextView notificationBody;

        @BindView(R.id.check_view)
        ImageView checkImageView;

        @BindView(R.id.notification_time)
        TextView notificationTime;


        NotificationViewHolder(View itemView) {
            super(itemView);
            MainApplication.component().inject(this);
        }

        public void setData(int position) {
            Notification notification = getFakeNotification(position);
            notificationBody.setText(textResolver.fromHtml(notification.getHtmlText()));
            String timeForView = DateTimeHelper.INSTANCE.getPresentOfDate(notification.getTime(), DateTimeFormat.forPattern(AppConstants.COMMENT_DATE_TIME_PATTERN).withZone(zone).withLocale(locale)); // // TODO: 13.10.16 save in ViewModel
            notificationTime.setText(timeForView);
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
