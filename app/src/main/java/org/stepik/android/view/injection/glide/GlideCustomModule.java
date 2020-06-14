package org.stepik.android.view.injection.glide;

import android.content.Context;
import android.graphics.drawable.PictureDrawable;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;

import org.stepic.droid.base.App;
import org.stepik.android.view.glide.mapper.ImageResourceDecoder;
import org.stepik.android.view.glide.mapper.ImageResourceTranscoder;
import org.stepik.android.view.glide.mapper.RelativeUrlLoader;
import org.stepik.android.view.glide.model.ImageResource;

import java.io.InputStream;

import javax.inject.Inject;

@GlideModule
public class GlideCustomModule extends AppGlideModule {

    @Inject
    RelativeUrlLoader.Factory relativeUrlLoaderFactory;

    public GlideCustomModule() {
        App.Companion.component().inject(this);
    }

    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide,
                                   @NonNull Registry registry) {
        registry.prepend(String.class, InputStream.class, relativeUrlLoaderFactory);
        registry.register(ImageResource.class, PictureDrawable.class, new ImageResourceTranscoder())
                .append(InputStream.class, ImageResource.class, new ImageResourceDecoder());
    }

    // Disable manifest parsing to avoid adding similar modules twice.
    @Override
    public boolean isManifestParsingEnabled() {
        return false;
    }
}
