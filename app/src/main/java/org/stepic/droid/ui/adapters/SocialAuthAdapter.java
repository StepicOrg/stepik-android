package org.stepic.droid.ui.adapters;

import android.content.Intent;
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
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;

import org.stepic.droid.R;
import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.base.MainApplication;
import org.stepic.droid.social.ISocialType;
import org.stepic.droid.social.SocialManager;
import org.stepic.droid.ui.listeners.StepicOnClickItemListener;
import org.stepic.droid.util.AppConstants;
import org.stepic.droid.web.IApi;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SocialAuthAdapter extends RecyclerView.Adapter<SocialAuthAdapter.SocialViewHolder> implements StepicOnClickItemListener {

    @Inject
    SocialManager mSocialManager;

    @Inject
    IApi mApi;

    @Inject
    Analytic analytic;


    List<? extends ISocialType> mSocialList;
    private FragmentActivity activity;
    private GoogleApiClient client;
    private TwitterAuthClient twitterAuthClient;
    private Callback<TwitterSession> twitterSessionCallback;

    public SocialAuthAdapter(FragmentActivity activity, GoogleApiClient client, TwitterAuthClient twitterAuthClient, Callback<TwitterSession> twitterSessionCallback) {
        this.client = client;
        this.twitterAuthClient = twitterAuthClient;
        this.twitterSessionCallback = twitterSessionCallback;
        MainApplication.component().inject(this);
        this.activity = activity;
        mSocialList = mSocialManager.getAllSocial();
    }


    @Override
    public SocialViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(activity).inflate(R.layout.social_item, null);
        return new SocialViewHolder(v, this);
    }

    @Override
    public void onBindViewHolder(SocialViewHolder holder, int position) {
        ISocialType socialType = mSocialList.get(position);

        holder.imageView.setImageDrawable(socialType.getIcon());
    }

    @Override
    public int getItemCount() {
        return mSocialList.size();
    }

    @Override
    public void onClick(int position) {
        ISocialType type = mSocialList.get(position);
        analytic.reportEvent(Analytic.Interaction.CLICK_SIGN_IN_SOCIAL, type.getIdentifier());
        if (type == SocialManager.SocialType.google) {
            // Start the retrieval process for a server auth code.  If requested, ask for a refreshWhenOnConnectionProblem
            // token.  Otherwise, only get an access token if a refreshWhenOnConnectionProblem token has been previously
            // retrieved.  Getting a new access token for an existing grant does not require
            // user consent.
            Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(client);
            activity.startActivityForResult(signInIntent, AppConstants.REQUEST_CODE_GOOGLE_SIGN_IN);
        } else if (type == SocialManager.SocialType.facebook) {
            List<String> permissions = new ArrayList<>();
            permissions.add("email");
            Toast.makeText(activity, "facebook", Toast.LENGTH_SHORT).show();
            LoginManager.getInstance().logInWithReadPermissions(activity, permissions);
        } else if (type == SocialManager.SocialType.vk) {
            Toast.makeText(activity, "vk", Toast.LENGTH_SHORT).show();
            String[] scopes = {VKScope.EMAIL};
            VKSdk.login(activity, scopes);
        } else if (type == SocialManager.SocialType.twitter) {
            Toast.makeText(activity, "twitter", Toast.LENGTH_SHORT).show();
            twitterAuthClient.authorize(activity, twitterSessionCallback);
        } else {
            mApi.loginWithSocial(activity, type);
        }
    }

    public static class SocialViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.root_view)
        View rootView;

        @BindView(R.id.social_item)
        ImageView imageView;

        public SocialViewHolder(View itemView, final StepicOnClickItemListener clickItemListener) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickItemListener.onClick(getAdapterPosition());
                }
            });
        }
    }
}
