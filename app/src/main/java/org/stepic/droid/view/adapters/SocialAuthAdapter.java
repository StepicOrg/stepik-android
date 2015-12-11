package org.stepic.droid.view.adapters;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import org.stepic.droid.R;
import org.stepic.droid.base.MainApplication;
import org.stepic.droid.social.SocialManager;
import org.stepic.droid.view.listeners.StepicOnClickItemListener;
import org.stepic.droid.web.IApi;

import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;

public class SocialAuthAdapter extends RecyclerView.Adapter<SocialAuthAdapter.SocialViewHolder> implements StepicOnClickItemListener {

    @Inject
    SocialManager mSocialManager;

    @Inject
    IApi mApi;


    List<SocialManager.SocialType> mSocialList;
    private Activity mContext;

    public SocialAuthAdapter(Activity context) {
        MainApplication.component().inject(this);
        mContext = context;
        mSocialList = mSocialManager.getAllSocial();
    }


    @Override
    public SocialViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.social_item, null);
        return new SocialViewHolder(v, this);
    }

    @Override
    public void onBindViewHolder(SocialViewHolder holder, int position) {
        SocialManager.SocialType socialType = mSocialList.get(position);

        holder.imageView.setImageDrawable(socialType.getIcon());
    }

    @Override
    public int getItemCount() {
        return mSocialList.size();
    }

    @Override
    public void onClick(int position) {
        SocialManager.SocialType type = mSocialList.get(position);
        mApi.loginWithSocial(mContext, type);
    }

    public static class SocialViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.root_view)
        View rootView;

        @Bind(R.id.social_item)
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
