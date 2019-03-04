package org.stepic.droid.util.glide;

import android.support.annotation.NonNull;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;
import com.bumptech.glide.load.model.stream.BaseGlideUrlLoader;

import org.stepic.droid.base.App;
import org.stepic.droid.configuration.Config;

import java.io.InputStream;

import javax.inject.Inject;

public class RelativeUrlLoader extends BaseGlideUrlLoader<String> {

    @Inject
    Config config;

    private RelativeUrlLoader(ModelLoader<GlideUrl, InputStream> concreteLoader) {
        super(concreteLoader);
        App.Companion.component().inject(this);
    }

    public static class Factory implements ModelLoaderFactory<String, InputStream> {

        @NonNull
        @Override
        public ModelLoader<String, InputStream> build(@NonNull MultiModelLoaderFactory multiFactory) {
            return new RelativeUrlLoader(multiFactory.build(GlideUrl.class, InputStream.class));
        }

        @Override
        public void teardown() {

        }
    }

    @Override
    protected String getUrl(String pathUrl, int width, int height, Options options) {
        return config.getBaseUrl() + pathUrl;
    }

    @Override
    public boolean handles(@NonNull String s) {
        return !s.contains("http") && !s.isEmpty();
    }
}