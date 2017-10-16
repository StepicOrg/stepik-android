package org.stepic.droid.ui.adapters;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.api.GoogleApiClient;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;

import org.stepic.droid.R;
import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.base.App;
import org.stepic.droid.social.ISocialType;
import org.stepic.droid.social.SocialManager;
import org.stepic.droid.ui.listeners.OnItemClickListener;
import org.stepic.droid.util.AppConstants;
import org.stepic.droid.web.Api;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SocialAuthAdapter extends RecyclerView.Adapter<SocialAuthAdapter.SocialViewHolder> implements OnItemClickListener {

    @Inject
    SocialManager socialManager;

    @Inject
    Api api;

    @Inject
    Analytic analytic;


    private List<? extends ISocialType> socialList;
    private FragmentActivity activity;
    @Nullable
    private GoogleApiClient client;

    private State state;

    public enum State {
        EXPANDED(1), NORMAL(2);

        public final int multiplier;
        State(int multiplier) {
            this.multiplier = multiplier;
        }
    }

    public SocialAuthAdapter(FragmentActivity activity, @Nullable GoogleApiClient client, State state) {
        this.client = client;
        App.Companion.component().inject(this);
        this.activity = activity;
        socialList = socialManager.getAllSocial();
        if (state == null) {
            this.state = State.NORMAL;
        } else {
            this.state = state;
        }
    }


    @Override
    public SocialViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(activity).inflate(R.layout.social_item, parent, false);
        return new SocialViewHolder(v, this);
    }

    @Override
    public void onBindViewHolder(SocialViewHolder holder, int position) {
        ISocialType socialType = socialList.get(position);
        holder.imageView.setImageDrawable(socialType.getIcon());
    }

    @Override
    public int getItemCount() {
        return socialList.size() / state.multiplier;
    }

    @Override
    public void onItemClick(int position) {
        ISocialType type = socialList.get(position);
        analytic.reportEvent(Analytic.Interaction.CLICK_SIGN_IN_SOCIAL, type.getIdentifier());
        if (type == SocialManager.SocialType.google) {
            if (client == null) {
                analytic.reportEvent(Analytic.Interaction.GOOGLE_SOCIAL_IS_NOT_ENABLED);
                Toast.makeText(App.Companion.getAppContext(), R.string.google_services_late, Toast.LENGTH_SHORT).show();
            } else {
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(client);
                activity.startActivityForResult(signInIntent, AppConstants.REQUEST_CODE_GOOGLE_SIGN_IN);
            }
        } else if (type == SocialManager.SocialType.facebook) {
            List<String> permissions = new ArrayList<>();
            permissions.add("email");
            LoginManager.getInstance().logInWithReadPermissions(activity, permissions);
        } else if (type == SocialManager.SocialType.vk) {
            String[] scopes = {VKScope.EMAIL};
            VKSdk.login(activity, scopes);
        } else {
            api.loginWithSocial(activity, type);
        }
    }

    public void showMore() {
        int start = getItemCount();
        state = State.EXPANDED;
        int end = getItemCount();
        notifyItemRangeInserted(start, end - start);
    }

    public void showLess() {
        int end = getItemCount();
        state = State.NORMAL;
        int start = getItemCount();
        notifyItemRangeRemoved(start, end - start);
    }

    public State getState() {
        return state;
    }

    public static class SocialViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.root_view)
        View rootView;

        @BindView(R.id.social_item)
        ImageView imageView;

        public SocialViewHolder(View itemView, final OnItemClickListener clickItemListener) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickItemListener.onItemClick(getAdapterPosition());
                }
            });
        }
    }
}
