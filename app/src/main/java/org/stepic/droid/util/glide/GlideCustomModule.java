package org.stepic.droid.util.glide;

import android.content.Context;
import android.graphics.drawable.PictureDrawable;
import android.support.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;
import com.caverock.androidsvg.SVG;

import org.stepic.droid.base.App;
import org.stepic.droid.configuration.Config;

import java.io.InputStream;

import javax.inject.Inject;

@GlideModule
public class GlideCustomModule extends AppGlideModule {

    @Inject
    Config config;

    public GlideCustomModule() {
        App.Companion.component().inject(this);
    }

    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide,
                                   @NonNull Registry registry) {
        registry.prepend(String.class, InputStream.class, new RelativeUrlLoader.Factory(config));
        registry.register(SVG.class, PictureDrawable.class, new SvgDrawableTranscoder())
            .append(InputStream.class, SVG.class, new SvgDecoder());
    }

    // Disable manifest parsing to avoid adding similar modules twice.
    @Override
    public boolean isManifestParsingEnabled() {
        return false;
    }
}
