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

import butterknife.BindView;
import butterknife.ButterKnife;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class SocialAuthAdapter extends RecyclerView.Adapter<SocialAuthAdapter.SocialViewHolder> implements OnItemClickListener {
    private SocialNetwork[] socialList;
    private Function1<SocialNetwork, Unit> onSocialItemClick;

    private State state;

    public enum State {
        EXPANDED(1), NORMAL(2);

        public final int multiplier;
        State(int multiplier) {
            this.multiplier = multiplier;
        }
    }

    public SocialAuthAdapter(Function1<SocialNetwork, Unit> onSocialItemClick, State state) {
        this.onSocialItemClick = onSocialItemClick;
        socialList = SocialNetwork.values();
        if (state == null) {
            this.state = State.NORMAL;
        } else {
            this.state = state;
        }
    }


    @Override
    @NonNull
    public SocialViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.social_item, parent, false);
        return new SocialViewHolder(v, this);
    }

    @Override
    public void onBindViewHolder(SocialViewHolder holder, int position) {
        SocialNetwork socialType = socialList[position];
        holder.imageView.setImageResource(socialType.getDrawableRes());
    }

    @Override
    public int getItemCount() {
        return socialList.length / state.multiplier + (socialList.length % state.multiplier == 0 ? 0 : 1);
    }

    @Override
    public void onItemClick(int position) {
        onSocialItemClick.invoke(socialList[position]);
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

    static class SocialViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.root_view)
        View rootView;

        @BindView(R.id.social_item)
        ImageView imageView;

        private SocialViewHolder(View itemView, final OnItemClickListener clickItemListener) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            rootView.setOnClickListener(v -> clickItemListener.onItemClick(getAdapterPosition()));
        }
    }
}
