package org.stepic.droid.ui.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.stepic.droid.R;
import org.stepic.droid.base.App;
import org.stepic.droid.configuration.Config;
import org.stepic.droid.core.presenters.NotificationListPresenter;
import org.stepic.droid.fonts.FontType;
import org.stepic.droid.fonts.FontsProvider;
import org.stepic.droid.notifications.model.Notification;
import org.stepic.droid.model.NotificationCategory;
import org.stepic.droid.util.AppConstants;
import org.stepic.droid.util.DateTimeHelper;
import org.stepic.droid.util.resolvers.text.TextResolver;

import java.util.ArrayList;
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
    private final int footerViewType = 3;
    private int countOfHeads = 1;
    private final int countOfFooter = 1;
    private final Typeface boldTypeface;
    private final Typeface regularTypeface;

    private Context context;
    private NotificationListPresenter notificationListPresenter;
    private NotificationCategory notificationCategory;
    private DateTimeZone zone;
    private Locale locale;
    private List<Notification> notifications = new ArrayList<>();
    private boolean isNeedShowFooter;
    private View.OnClickListener markAllReadListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            markAllAsRead();
        }
    };
    private boolean isNeedEnableMarkButton = true;

    public NotificationAdapter(Context context, NotificationListPresenter notificationListPresenter, NotificationCategory notificationCategory, FontsProvider fontsProvider) {
        this.context = context;
        this.notificationListPresenter = notificationListPresenter;
        this.notificationCategory = notificationCategory;
        zone = DateTimeZone.getDefault();
        locale = Locale.getDefault();
        boldTypeface = TypefaceUtils.load(context.getAssets(),  fontsProvider.provideFontPath(FontType.bold));
        regularTypeface = TypefaceUtils.load(context.getAssets(), fontsProvider.provideFontPath(FontType.regular));
        if (this.notificationCategory != NotificationCategory.all) {
            countOfHeads = 0;
        }
    }

    public int getNotificationsCount() {
        return notifications.size();
    }

    @Override
    public GenericViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Timber.d("createViewHolder of NotificationAdapter, viewType = %d", viewType);
        if (viewType == headerViewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.notification_list_header_item, parent, false);
            return new HeaderViewHolder(view);
        } else if (viewType == footerViewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.view_notification_loading_footer, parent, false);
            return new FooterViewHolder(view);
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
        if (position < countOfHeads) {
            return headerViewType;
        } else if (position == getItemCount() - 1) {
            return footerViewType;
        } else {
            return itemViewType;
        }
    }

    @Override
    public int getItemCount() {
        return notifications.size() + countOfHeads + countOfFooter;
    }

    public void setNotifications(List<Notification> notifications) {
        this.notifications = notifications;
        notifyDataSetChanged();
    }

    public void showLoadingFooter(boolean isNeedShow) {
        isNeedShowFooter = isNeedShow;
        notifyItemChanged(getItemCount() - 1);
    }

    public void onClick(int adapterPosition, boolean needOpenNotification) {
        int positionInList = adapterPosition - countOfHeads;
        if (positionInList >= 0 && positionInList < notifications.size()) {
            Notification notification = notifications.get(positionInList);
            Boolean unread = notification.is_unread();
            if (unread == null || unread) {
                Long id = notification.getId();
                if (id != null) {
                    notificationListPresenter.markAsRead(id);
                } else {
                    notificationListPresenter.notificationIdIsNull();
                }
                notification.set_unread(false);
                notifyItemChanged(adapterPosition);
            }

            notificationListPresenter.trackClickOnNotification(notification);

            if (needOpenNotification) {
                notificationListPresenter.tryToOpenNotification(notification);
            }
        }
    }

    public void markNotificationAsRead(int position, long id, boolean needRead) {
        boolean newValue = !needRead;
        if (position >= 0 && position < notifications.size()) {
            Notification notification = notifications.get(position);
            Long notificationId = notification.getId();
            if (notificationId != null && notificationId == id) {
                Boolean unread = notification.is_unread();
                if (unread != null && unread != newValue) {
                    notification.set_unread(newValue);
                    notifyItemChanged(position + countOfHeads);
                }
            }
        }
    }

    private void markAllAsRead() {
        notificationListPresenter.markAllAsRead();
    }

    public void setEnableMarkAllButton(boolean needEnable) {
        isNeedEnableMarkButton = needEnable;
        notifyItemChanged(0);
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
        Config config;

        @BindView(R.id.notification_body)
        TextView notificationBody;

        @BindView(R.id.notification_root)
        ViewGroup notificationRoot;

        @BindView(R.id.check_view)
        View checkImageView;

        @BindView(R.id.notification_time)
        TextView notificationTime;


        NotificationViewHolder(View itemView) {
            super(itemView);
            App.Companion.component().inject(this);
            notificationBody.setMovementMethod(LinkMovementMethod.getInstance());

            //for checking notification
            View.OnClickListener rootClick = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    NotificationAdapter.this.onClick(getAdapterPosition(), true);
                }
            };

            View.OnClickListener onlyCheckView = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    NotificationAdapter.this.onClick(getAdapterPosition(), false);
                }
            };

            notificationRoot.setOnClickListener(rootClick);
            notificationBody.setOnClickListener(rootClick);
            checkImageView.setOnClickListener(onlyCheckView);
        }

        public void setData(int position) {
            int positionInList = position - countOfHeads;
            Notification notification = notifications.get(positionInList);

            notificationBody.setText(textResolver.fromHtml(notification.getHtmlText()));

            String timeForView = DateTimeHelper.INSTANCE.getPresentOfDate(notification.getTime(), DateTimeFormat.forPattern(AppConstants.COMMENT_DATE_TIME_PATTERN).withZone(zone).withLocale(locale)); // // TODO: 13.10.16 save in ViewModel
            notificationTime.setText(timeForView);

            resolveViewedState(notification);
        }

        private void resolveViewedState(Notification notification) {
            boolean isViewed = true;
            Boolean unread = notification.is_unread();
            if (unread != null) {
                isViewed = !unread;
            }

            if (isViewed) {
                CalligraphyUtils.applyFontToTextView(notificationBody, regularTypeface);
            } else {
                CalligraphyUtils.applyFontToTextView(notificationBody, boldTypeface);
            }

            checkImageView.setVisibility(isViewed ? View.GONE : View.VISIBLE);
        }

    }

    class HeaderViewHolder extends GenericViewHolder {

        @BindView(R.id.mark_all_as_read_button)
        Button markAllAsViewed;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            markAllAsViewed.setOnClickListener(markAllReadListener);
        }

        @Override
        void setData(int position) {
            markAllAsViewed.setEnabled(isNeedEnableMarkButton);
        }
    }

    class FooterViewHolder extends GenericViewHolder {

        @BindView(R.id.loading_root)
        ViewGroup loadingRoot;


        public FooterViewHolder(View itemView) {
            super(itemView);
        }

        @Override
        void setData(int position) {
            loadingRoot.setVisibility(isNeedShowFooter ? View.VISIBLE : View.GONE);
        }
    }
}
