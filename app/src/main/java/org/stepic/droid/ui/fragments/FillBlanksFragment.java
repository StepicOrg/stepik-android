package org.stepic.droid.ui.fragments;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ReplacementSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import org.stepic.droid.R;
import org.stepic.droid.events.InternetIsEnabledEvent;
import org.stepic.droid.events.comments.NewCommentWasAddedOrUpdateEvent;
import org.stepic.droid.events.steps.StepWasUpdatedEvent;
import org.stepic.droid.model.Attempt;
import org.stepic.droid.model.FillBlankComponent;
import org.stepic.droid.model.Reply;
import org.stepic.droid.util.ColorUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;

public class FillBlanksFragment extends StepAttemptFragment {

    TextView textViewFillBlanks;
    private int flags = Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        View fillBlanksView = ((LayoutInflater) this.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.view_fill_blanks, attemptContainer, false);
        textViewFillBlanks = ButterKnife.findById(fillBlanksView, R.id.textViewFillBlanks);
        attemptContainer.addView(fillBlanksView);
    }

    @Override
    protected void showAttempt(Attempt attempt) {
        List<FillBlankComponent> list = attempt.getDataset().getFillBlankComponents();

        String source = "Hello it is spannable text ";
        SpannableString text = new SpannableString(source + source + source + source);
        text.setSpan(new BackgroundColorSpan(ColorUtil.INSTANCE.getColorArgb(R.color.stepic_orange_carrot, getContext())), 6, source.length() * 2 + 10, flags);
        textViewFillBlanks.setText(text);


        //// TODO: 20.01.17   show components from reply

    }

    @Override
    protected Reply generateReply() {
        //// TODO: 20.01.17 make it from user changing
        List<String> blanks = new ArrayList<>();
        blanks.add("First one");
        blanks.add("Second");
        blanks.add("    etc");
        return new Reply.Builder()
                .setBlanks(blanks)
                .build();
    }

    @Override
    protected void blockUIBeforeSubmit(boolean needBlock) {
        //// TODO: 20.01.17   block UI
    }

    @Override
    protected void onRestoreSubmission() {
        Reply reply = submission.getReply();
        //// TODO: 20.01.17   fill blanks from reply
        reply.getBlanks();
    }

    @Subscribe
    @Override
    public void onInternetEnabled(InternetIsEnabledEvent enabledEvent) {
        super.onInternetEnabled(enabledEvent);
    }

    @Subscribe
    public void onNewCommentWasAdded(NewCommentWasAddedOrUpdateEvent event) {
        super.onNewCommentWasAdded(event);
    }

    @Subscribe
    public void onStepWasUpdated(StepWasUpdatedEvent event) {
        super.onStepWasUpdated(event);
    }

    public static class LOLLL extends ReplacementSpan {
        private static final float PADDING = 150.0f;
        private int backgroundColor = 0;
        private int textColor = 0;
        private float margin = 16f;

        public LOLLL(Context context) {
            super();
            backgroundColor = context.getResources().getColor(R.color.stepic_orange_carrot);
            textColor = context.getResources().getColor(R.color.black);
        }

        @Override
        public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
            canvas.drawColor(Color.RED);
            RectF rect = new RectF(x + margin, top, x + measureText(paint, text, start, end) + PADDING - margin, bottom);
            paint.setColor(backgroundColor);
            canvas.drawRect(rect, paint);
            paint.setColor(textColor);
//            canvas.drawText(text, start, end, x, y, paint);
            int xPos = Math.round(x + (PADDING / 2));
//            int yPos = (int) ((canvas.getHeight() / 2) - ((paint.descent() + paint.ascent()) / 2));
            canvas.drawText(text, start, end, xPos, y, paint);

            paint.setStrokeWidth(10f);
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.BLACK);
            canvas.drawRect(rect, paint);
        }

        @Override
        public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
            return Math.round(paint.measureText(text, start, end) + PADDING);
        }

        private float measureText(Paint paint, CharSequence text, int start, int end) {
            return paint.measureText(text, start, end);
        }
    }

    public static class RoundedBackgroundSpan extends ReplacementSpan {

        private static int CORNER_RADIUS = 8;
        private int backgroundColor = 0;
        private int textColor = 0;

        public RoundedBackgroundSpan(Context context) {
            super();
            backgroundColor = context.getResources().getColor(R.color.stepic_orange_carrot);
            textColor = context.getResources().getColor(R.color.white);
        }

        @Override
        public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
            RectF rect = new RectF(x, top, x + measureText(paint, text, start, end), bottom);
            paint.setColor(backgroundColor);
            canvas.drawRoundRect(rect, CORNER_RADIUS, CORNER_RADIUS, paint);
            paint.setColor(textColor);
            canvas.drawText(text, start, end, x, y, paint);
        }

        @Override
        public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
            return Math.round(paint.measureText(text, start, end));
        }

        private float measureText(Paint paint, CharSequence text, int start, int end) {
            return paint.measureText(text, start, end);
        }
    }

}
