package org.stepic.droid.util.svg;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PictureDrawable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;

public class GlideSvgRequestFactory {
    public static RequestBuilder<PictureDrawable> create(Context context, Drawable placeholder) {
        return Glide.with(context.getApplicationContext())
            .as(PictureDrawable.class)
            .placeholder(placeholder)
            .listener(new SvgSoftwareLayerSetter());
    }
}
