package org.stepic.droid.ui.custom.progressbutton;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.ProgressBar;

import org.stepic.droid.R;

import timber.log.Timber;

public class ProgressWheel extends ProgressBar {

    //Sizes (with defaults)
    private int layout_height = 0;
    private int layout_width = 0;
    private int barWidth = 20;
    private int rimWidth = 20;
    private float contourSize = 0;
    private float centerSquareSize = 20;

    //Padding (with defaults)
    private int paddingTop = 0;
    private int paddingBottom = 0;
    private int paddingLeft = 0;
    private int paddingRight = 0;

    //Colors (with defaults)
    private int barColor = 0xAA000000;
    private int contourColorOuter = 0xAA000000;
    private int rimColor = 0xAADDDDDD;
    private int centerSquareColor = 0xAADDDDDD;

    //Paints
    private Paint barPaint = new Paint();
    private Paint rimPaint = new Paint();
    private Paint contourPaintOuter = new Paint();
    private Paint centerSquarePaint = new Paint();

    //Rectangles
    private RectF circleBounds = new RectF();
    private RectF circleOuterContour = new RectF();
    private RectF centerSquareRect = new RectF();

    //Animation
    private final int MAX_DEGREES = 360;
    private ValueAnimator animator;
    private static final Interpolator DEFAULT_INTERPOLATER = new AccelerateDecelerateInterpolator();


    /**
     * The constructor for the ProgressWheel
     */
    public ProgressWheel(Context context, AttributeSet attrs) {
        super(context, attrs);

        parseAttributes(context.obtainStyledAttributes(attrs,
                R.styleable.ProgressWheel));

        setMax(MAX_DEGREES);
        setIndeterminate(false);
    }

    //----------------------------------
    //Setting up stuff
    //----------------------------------

    /*
     * When this is called, make the view square.
     * From: http://www.jayway.com/2012/12/12/creating-custom-android-views-part-4-measuring-and-how-to-force-a-view-to-be-square/
     *
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // The first thing that happen is that we call the superclass
        // implementation of onMeasure. The reason for that is that measuring
        // can be quite a complex process and calling the super method is a
        // convenient way to get most of this complexity handled.
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // We can’t use getWidth() or getHight() here. During the measuring
        // pass the view has not gotten its final size yet (this happens first
        // at the start of the layout pass) so we have to use getMeasuredWidth()
        // and getMeasuredHeight().
        int size;
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        int widthWithoutPadding = width - getPaddingLeft() - getPaddingRight();
        int heightWithoutPadding = height - getPaddingTop() - getPaddingBottom();

        // Finally we have some simple logic that calculates the size of the view
        // and calls setMeasuredDimension() to set that size.
        // Before we compare the width and height of the view, we remove the padding,
        // and when we set the dimension we add it back again. Now the actual content
        // of the view will be square, but, depending on the padding, the total dimensions
        // of the view might not be.
        if (widthWithoutPadding > heightWithoutPadding) {
            size = heightWithoutPadding;
        } else {
            size = widthWithoutPadding;
        }

        // If you override onMeasure() you have to call setMeasuredDimension().
        // This is how you report back the measured size.  If you don’t call
        // setMeasuredDimension() the parent will throw an exception and your
        // application will crash.
        // We are calling the onMeasure() method of the superclass so we don’t
        // actually need to call setMeasuredDimension() since that takes care
        // of that. However, the purpose with overriding onMeasure() was to
        // change the default behaviour and to do that we need to call
        // setMeasuredDimension() with our own values.
        setMeasuredDimension(size + getPaddingLeft() + getPaddingRight(), size + getPaddingTop() + getPaddingBottom());
    }

    /**
     * Use onSizeChanged instead of onAttachedToWindow to get the dimensions of the view,
     * because this method is called after measuring the dimensions of MATCH_PARENT & WRAP_CONTENT.
     * Use this dimensions to setup the bounds and paints.
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // Share the dimensions
        layout_width = w;
        layout_height = h;

        setupBounds();
        setupPaints();
        invalidate();
    }

    /**
     * Set the properties of the paints we're using to
     * draw the progress wheel
     */
    private void setupPaints() {
        barPaint.setColor(barColor);
        barPaint.setAntiAlias(true);
        barPaint.setStyle(Paint.Style.STROKE);
        barPaint.setStrokeWidth(barWidth);

        rimPaint.setColor(rimColor);
        rimPaint.setAntiAlias(true);
        rimPaint.setStyle(Paint.Style.STROKE);
        rimPaint.setStrokeWidth(rimWidth);

        contourPaintOuter.setColor(contourColorOuter);
        contourPaintOuter.setAntiAlias(true);
        contourPaintOuter.setStyle(Paint.Style.STROKE);
        contourPaintOuter.setStrokeWidth(contourSize);

        centerSquarePaint.setColor(centerSquareColor);
        centerSquarePaint.setAntiAlias(true);
        centerSquarePaint.setStyle(Paint.Style.FILL);
    }

    /**
     * Set the bounds of the component
     */
    private void setupBounds() {
        // Width should equal to Height, find the min value to setup the circle
        int minValue = Math.min(layout_width, layout_height);

        // Calc the Offset if needed
        int xOffset = layout_width - minValue;
        int yOffset = layout_height - minValue;

        // Add the offset
        paddingTop = this.getPaddingTop() + (yOffset / 2);
        paddingBottom = this.getPaddingBottom() + (yOffset / 2);
        paddingLeft = this.getPaddingLeft() + (xOffset / 2);
        paddingRight = this.getPaddingRight() + (xOffset / 2);

        int width = getWidth();
        int height = getHeight();

        circleBounds.set(paddingLeft + barWidth,
                paddingTop + barWidth,
                width - paddingRight - barWidth,
                height - paddingBottom - barWidth);
        circleOuterContour.set(circleBounds.left - (rimWidth / 2.0f) - (contourSize / 2.0f), circleBounds.top - (rimWidth / 2.0f) - (contourSize / 2.0f), circleBounds.right + (rimWidth / 2.0f) + (contourSize / 2.0f), circleBounds.bottom + (rimWidth / 2.0f) + (contourSize / 2.0f));

        float halfWidth = width / 2f;
        float halfHeight = height / 2f;
        float halfSquareSize = centerSquareSize / 2f;
        centerSquareRect.set(halfWidth - halfSquareSize, halfHeight - halfSquareSize, halfWidth + halfSquareSize, halfHeight + halfSquareSize);
    }

    /**
     * Parse the attributes passed to the view from the XML
     *
     * @param a the attributes to parse
     */
    private void parseAttributes(TypedArray a) {
        try {
            barWidth = (int) a.getDimension(R.styleable.ProgressWheel_barWidth, barWidth);

            rimWidth = (int) a.getDimension(R.styleable.ProgressWheel_rimWidth, rimWidth);

            barColor = a.getColor(R.styleable.ProgressWheel_barColor, barColor);

            rimColor = a.getColor(R.styleable.ProgressWheel_rimColor, rimColor);

            contourColorOuter = a.getColor(R.styleable.ProgressWheel_contourColorOuter, contourColorOuter);

            contourSize = a.getDimension(R.styleable.ProgressWheel_contourSize, contourSize);

            centerSquareColor = a.getColor(R.styleable.ProgressWheel_squareColor, centerSquareColor);

            centerSquareSize = a.getDimension(R.styleable.ProgressWheel_squareSize, centerSquareSize);

        } finally {
            a.recycle();
        }
    }

    //----------------------------------
    //Animation stuff
    //----------------------------------

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawArc(circleBounds, MAX_DEGREES, MAX_DEGREES, false, rimPaint);
        canvas.drawArc(circleOuterContour, MAX_DEGREES, MAX_DEGREES, false, contourPaintOuter);
        //Draw square in center
        canvas.drawRect(centerSquareRect, centerSquarePaint);
        //Draw the bar
        int progress = getProgress();
        canvas.drawArc(circleBounds, -90, progress, false, barPaint);
    }

    /**
     * Sets progress percentage to given percent value.
     *
     * @param portion should be >=0 and <=1
     */
    public void setProgressPortion(float portion, boolean needAnimation) {
        int newProgress = (int) (MAX_DEGREES * portion);
        int currentProgress = getProgress();
        Timber.d("oldProgress = %s, NewProgress = %s, portion = %f, view = %s", currentProgress, newProgress, portion, this);
        if (currentProgress >= newProgress && needAnimation) {
            //our progress can't go back, when updating
            return;
        }
        if (!needAnimation) {
            super.setProgress(newProgress);
            return;
        }

        if (animator != null) {
            animator.cancel();
        }

        if (animator == null) {
            animator = ValueAnimator.ofInt(currentProgress, newProgress);
            animator.setInterpolator(DEFAULT_INTERPOLATER);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int animatedValue = (int) animation.getAnimatedValue();
                    ProgressWheel.super.setProgress(animatedValue);
                }
            });
        } else {
            animator.setIntValues(currentProgress, newProgress);
        }
        animator.start();
    }

}
