package org.stepic.droid.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.stepic.droid.R;
import org.stepic.droid.ui.listeners.OnItemClickListener;
import org.stepik.android.view.auth.model.SocialNetwork;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class SocialAuthAdapter extends RecyclerView.Adapter<SocialAuthAdapter.SocialViewHolder> implements OnItemClickListener {
    private final SocialNetwork[] socialList;
    private final Function1<SocialNetwork, Unit> onSocialItemClick;

    public SocialAuthAdapter(Function1<SocialNetwork, Unit> onSocialItemClick) {
        this.onSocialItemClick = onSocialItemClick;
        socialList = SocialNetwork.values();
    }


    @Override
    @NonNull
    public SocialViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_social, parent, false);
        return new SocialViewHolder(v, this);
    }

    @Override
    public void onBindViewHolder(SocialViewHolder holder, int position) {
        SocialNetwork socialType = socialList[position];
        holder.imageView.setImageResource(socialType.getDrawableRes());
    }

    @Override
    public int getItemCount() {
        return socialList.length;
    }

    @Override
    public void onItemClick(int position) {
        onSocialItemClick.invoke(socialList[position]);
    }

    static class SocialViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageView;

        private SocialViewHolder(View itemView, final OnItemClickListener clickItemListener) {
            super(itemView);
            itemView.setOnClickListener(v -> clickItemListener.onItemClick(getAdapterPosition()));
            imageView = itemView.findViewById(R.id.social_item);
        }
    }
}
