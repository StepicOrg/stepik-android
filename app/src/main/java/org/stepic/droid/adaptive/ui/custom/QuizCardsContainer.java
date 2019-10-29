package org.stepic.droid.adaptive.ui.custom;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;
import org.stepic.droid.adaptive.ui.custom.container.ContainerAdapter;
import org.stepic.droid.adaptive.ui.custom.container.ContainerView;

import java.util.ArrayList;
import java.util.List;

public class QuizCardsContainer extends FrameLayout implements ContainerView {
    private final static int BUFFER_SIZE = 3;

    private final static float CARD_OFFSET_DP = 4;
    private final static float CARD_MARGIN_DP = 8;

    public final static int CARD_OFFSET;
    private final static float SCALE;

    static {
        // approx height
        final int TOOLBAR_HEIGHT_DP = 56;

        final DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();

        // desired params of second card
        final float cardWidth = metrics.widthPixels - 2 * CARD_MARGIN_DP * metrics.density;
        final float cardHeight = metrics.heightPixels - (2 * CARD_MARGIN_DP + TOOLBAR_HEIGHT_DP) * metrics.density;

        SCALE = 2 * CARD_OFFSET_DP * metrics.density / cardWidth; // formulas to calculate correct offset of second card on different screens
        CARD_OFFSET = (int) (CARD_OFFSET_DP * metrics.density / (1f - 2 * SCALE) + cardHeight * SCALE / 2);
    }

    public QuizCardsContainer(@NonNull Context context) {
        super(context);
    }

    public QuizCardsContainer(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public QuizCardsContainer(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private float m = 0.0f;

    private final SwipeableLayout.SwipeListener swipeListener = new SwipeableLayout.SwipeListener() {
        @Override
        public void onScroll(float scrollProgress) {
            final int size = getVisibleItemCount();
            m = Math.min(Math.abs(scrollProgress), 0.5f) * 2;
            for (int j = 1; j < size; j++) {
                setViewState(cardHolders.get(j).getView(), j - m, false);
            }
        }

        @Override
        public void onSwipeLeft() {
            cardHolders.get(0).getView().setEnabled(false);
        }

        @Override
        public void onSwipeRight() {
            cardHolders.get(0).getView().setEnabled(false);
        }

        @Override
        public void onSwiped() {
            m = 0;
            poll();
        }
    };

    private List<ContainerView.ViewHolder> cardHolders = new ArrayList<>();

    @org.jetbrains.annotations.Nullable
    public View getTopCardView() {
        ContainerView.ViewHolder holder = cardHolders.get(0);
        return holder.isAttached() ? holder.getView() : null;
    }

    private void initCards() {
        cardHolders.clear();
        for (int i = 0; i < BUFFER_SIZE; i++) {
            cardHolders.add(adapter.onCreateViewHolder(this));
        }
        onDataSetChanged();
    }

    private void poll() {
        removeView(cardHolders.remove(0).getView());
        adapter.poll();
        cardHolders.add(adapter.onCreateViewHolder(this));
        onRebind();
    }

    private void setViewState(View view, float mul, boolean allowEnable) {
        if (mul < 0) return;

        view.setScaleX(1 - (SCALE * mul));
        view.setScaleY(1 - (SCALE * mul));

        view.setEnabled(mul == 0 && allowEnable);
        view.setTranslationY(CARD_OFFSET * mul);

        if (mul >= BUFFER_SIZE - 2) {
            view.setAlpha(BUFFER_SIZE - mul - 1);
        }
    }

    private CardsAdapter adapter;

    @Override
    public final void setAdapter(@NotNull ContainerAdapter adapter) {
        if (adapter instanceof CardsAdapter) {
            this.adapter = (CardsAdapter) adapter;
            this.adapter.setContainer(this);
            initCards();
        }
    }

    @Override
    public void onDataSetChanged() {
        if (adapter == null) return;
        removeAllViews();
        onRebind();
    }

    @Override
    public void onDataAdded() {
        onRebind();
    }

    @Override
    public void onRebind() {
        final int size = getVisibleItemCount();
        for (int i = 0; i < size; i++) {
            onRebind(i);
        }

        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            for (int i = size - 1; i >= 0; i--)
                cardHolders.get(i).getView().bringToFront();
        }
    }

    private int getVisibleItemCount() {
        return Math.min(adapter.getItemCount(), BUFFER_SIZE);
    }

    @Override
    public void onRebind(int i) {
        final int size = getVisibleItemCount();
        if (0 <= i && i < cardHolders.size()) {
            ContainerView.ViewHolder holder = cardHolders.get(i);
            View view = holder.getView();
            if (!holder.isAttached()) {
                adapter.onBindViewHolder(holder, i);
                holder.setAttached(true);
                addView(view);
            }

            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                view.setElevation(size + 3 - i);
            }

            adapter.onPositionChanged(holder, i);
            setViewState(view, i - m, true);

            if (i == 0) {
                if (view instanceof SwipeableLayout) {
                    ((SwipeableLayout) view).setSwipeListener(swipeListener);
                }
                adapter.onBindTopCard(holder, 0);
            }
        }
    }

    public static abstract class CardsAdapter<VH extends ContainerView.ViewHolder> extends ContainerAdapter<VH> {
        protected abstract void poll();
        protected abstract void onPositionChanged(VH holder, int pos);
        protected abstract void onBindTopCard(VH holder, int pos);
    }
}
