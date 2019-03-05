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

    private Config config;

    public RelativeUrlLoader(ModelLoader<GlideUrl, InputStream> concreteLoader, Config config) {
        super(concreteLoader);
        this.config = config;
    }

    @Override
    protected String getUrl(String pathUrl, int width, int height, Options options) {
        return config.getBaseUrl() + pathUrl;
    }

    @Override
    public boolean handles(@NonNull String s) {
        return s.startsWith("/");
    }

    public static class Factory implements ModelLoaderFactory<String, InputStream> {

        private Config config;

        @Inject
        public Factory(Config config) {
            this.config = config;
        }

        @NonNull
        @Override
        public ModelLoader<String, InputStream> build(@NonNull MultiModelLoaderFactory multiFactory) {
            return new RelativeUrlLoader(multiFactory.build(GlideUrl.class, InputStream.class), config);
        }

        @Override
        public void teardown() {

        }
    }
}