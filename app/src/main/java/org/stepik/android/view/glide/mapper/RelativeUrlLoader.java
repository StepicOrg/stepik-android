package org.stepik.android.view.glide.mapper;

import androidx.annotation.NonNull;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;
import com.bumptech.glide.load.model.stream.BaseGlideUrlLoader;

import org.stepic.droid.configuration.EndpointResolver;

import java.io.InputStream;

import javax.inject.Inject;

public class RelativeUrlLoader extends BaseGlideUrlLoader<String> {

    private EndpointResolver endpointResolver;

    public RelativeUrlLoader(ModelLoader<GlideUrl, InputStream> concreteLoader, EndpointResolver endpointResolver) {
        super(concreteLoader);
        this.endpointResolver = endpointResolver;
    }

    @Override
    protected String getUrl(String pathUrl, int width, int height, Options options) {
        return endpointResolver.getBaseUrl() + pathUrl;
    }

    @Override
    public boolean handles(@NonNull String s) {
        return s.startsWith("/");
    }

    public static class Factory implements ModelLoaderFactory<String, InputStream> {

        private EndpointResolver endpointResolver;

        @Inject
        public Factory(EndpointResolver endpointResolver) {
            this.endpointResolver = endpointResolver;
        }

        @NonNull
        @Override
        public ModelLoader<String, InputStream> build(@NonNull MultiModelLoaderFactory multiFactory) {
            return new RelativeUrlLoader(multiFactory.build(GlideUrl.class, InputStream.class), endpointResolver);
        }

        @Override
        public void teardown() {

        }
    }
}