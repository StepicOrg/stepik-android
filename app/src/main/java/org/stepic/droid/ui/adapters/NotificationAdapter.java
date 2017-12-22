package org.stepic.droid.ui.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PictureDrawable;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.GenericRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.caverock.androidsvg.SVG;

import org.stepic.droid.R;
import org.stepic.droid.base.App;
import org.stepic.droid.core.presenters.NotificationListPresenter;
import org.stepic.droid.model.NotificationCategory;
import org.stepic.droid.notifications.model.Notification;
import org.stepic.droid.util.AppConstants;
import org.stepic.droid.util.DateTimeHelper;
import org.stepic.droid.util.resolvers.text.NotificationTextResolver;
import org.stepic.droid.util.svg.GlideSvgRequestFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

import static org.stepic.droid.ui.util.ViewExtensionsKt.changeVisibility;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.GenericViewHolder> {

    private static final int ITEM_VIEW_TYPE = 1;
    private static final int HEADER_VIEW_TYPE = 2;
    private static final int FOOTER_VIEW_TYPE = 3;
    private static final int FOOTER_COUNT = 1;

    private final int headerCount;


    private Context context;
    private NotificationListPresenter notificationListPresenter;
    private List<Notification> notifications = new ArrayList<>();
    private boolean isNeedShowFooter;
    private View.OnClickListener markAllReadListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            markAllAsRead();
        }
    };
    private boolean isNeedEnableMarkButton = true;

    private final Drawable placeholderUserIcon;

    public NotificationAdapter(Context context, NotificationListPresenter notificationListPresenter, NotificationCategory notificationCategory) {
        this.context = context;
        this.notificationListPresenter = notificationListPresenter;
        if (notificationCategory != NotificationCategory.all) {
            headerCount = 0;
        } else {
            headerCount = 1;
        }
        this.placeholderUserIcon = ContextCompat.getDrawable(context, R.drawable.general_placeholder);
    }

    public int getNotificationsCount() {
        return notifications.size();
    }

    @Override
    public GenericViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Timber.d("createViewHolder of NotificationAdapter, viewType = %d", viewType);
        if (viewType == HEADER_VIEW_TYPE) {
            View view = LayoutInflater.from(context).inflate(R.layout.notification_list_header_item, parent, false);
            return new HeaderViewHolder(view);
        } else if (viewType == FOOTER_VIEW_TYPE) {
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
        if (position < headerCount) {
            return HEADER_VIEW_TYPE;
        } else if (position == getItemCount() - 1) {
            return FOOTER_VIEW_TYPE;
        } else {
            return ITEM_VIEW_TYPE;
        }
    }

    @Override
    public int getItemCount() {
        return notifications.size() + headerCount + FOOTER_COUNT;
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
        int positionInList = adapterPosition - headerCount;
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
                    notifyItemChanged(position + headerCount);
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
        NotificationTextResolver notificationTextResolver;

        @BindView(R.id.notification_body)
        TextView notificationBody;

        @BindView(R.id.notification_root)
        ViewGroup notificationRoot;

        @BindView(R.id.notification_time)
        TextView notificationTime;

        @BindView(R.id.notification_icon)
        ImageView notificationIcon;

        @BindView(R.id.check_view_read)
        View checkViewRead;

        @BindView(R.id.check_view_unread)
        View checkViewUnread;

        private final GenericRequestBuilder<Uri, InputStream, SVG, PictureDrawable> svgRequestBuilder =
                GlideSvgRequestFactory.create(context, placeholderUserIcon);

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
            checkViewUnread.setOnClickListener(onlyCheckView);
        }

        public void setData(int position) {
            int positionInList = position - headerCount;
            Notification notification = notifications.get(positionInList);

            resolveNotificationText(notification);

            String timeForView = DateTimeHelper.INSTANCE.getPrintableOfIsoDate(notification.getTime(), AppConstants.COMMENT_DATE_TIME_PATTERN, TimeZone.getDefault());
            notificationTime.setText(timeForView);

            resolveViewedState(notification);
            resolveNotificationIcon(notification);
        }

        private void resolveNotificationText(Notification notification) {
            if (notification.getNotificationText() == null) {
                notification.setNotificationText(notificationTextResolver.resolveNotificationText(context, notification.getHtmlText()));
            }

            notificationBody.setText(notification.getNotificationText());
        }

        private void resolveNotificationIcon(Notification notification) {
            switch (notification.getType()) {
                case learn:
                    notificationIcon.setImageResource(R.drawable.ic_notification_type_learning);
                    break;
                case teach:
                    notificationIcon.setImageResource(R.drawable.ic_notification_type_teaching);
                    break;
                case review:
                    notificationIcon.setImageResource(R.drawable.ic_notification_type_review);
                    break;
                case other:
                    notificationIcon.setImageResource(R.drawable.ic_notification_type_other);
                    break;
                case comments:
                    setCommentNotificationIcon(notification);
                    break;
            }
        }

        private void setCommentNotificationIcon(Notification notification) {
            final String userAvatarUrl = notification.getUserAvatarUrl();
            if (userAvatarUrl != null) {
                if (userAvatarUrl.endsWith(AppConstants.SVG_EXTENSION)) {
                    final Uri avatarUri =  Uri.parse(userAvatarUrl);
                    svgRequestBuilder
                            .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                            .load(avatarUri)
                            .into(notificationIcon);
                } else {
                    Glide.with(context)
                            .load(userAvatarUrl)
                            .asBitmap()
                            .placeholder(placeholderUserIcon)
                            .into(notificationIcon);
                }
            } else {
                notificationIcon.setImageDrawable(placeholderUserIcon);
            }
        }

        private void resolveViewedState(Notification notification) {
            boolean isViewed = true;
            Boolean unread = notification.is_unread();
            if (unread != null) {
                isViewed = !unread;
            }

            changeVisibility(checkViewRead, isViewed);
            changeVisibility(checkViewUnread, !isViewed);
        }

    }

    class HeaderViewHolder extends GenericViewHolder {

        @BindView(R.id.mark_all_as_read_button)
        TextView markAllAsViewed;

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

        @BindView(R.id.loadingRoot)
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
