package org.stepic.droid.util.glide;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.ModelLoader;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.stepic.droid.configuration.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;

public class RelativeLoaderTest {

    private RelativeUrlLoader relativeUrlLoader;

    @Mock
    Config config;

    @Mock
    ModelLoader<GlideUrl, InputStream> loaderFactory;

    @Before
    public void beforeEachTest() {
        MockitoAnnotations.initMocks(this);
        Mockito.when(config.getBaseUrl()).thenReturn("https://stepik.org");
        relativeUrlLoader = new RelativeUrlLoader(loaderFactory, config);
    }

    @Test
    public void handlesTest() {
        assertTrue(relativeUrlLoader.handles("/topsecret/sandwich.svg"));
        assertTrue(relativeUrlLoader.handles("/some/addr/http/image.png"));
        assertTrue(relativeUrlLoader.handles("/some/image/wow.png"));
        assertFalse(relativeUrlLoader.handles("stepik.org/logo.png"));
        assertFalse(relativeUrlLoader.handles("https://stepik.org/logo.png"));
        assertFalse(relativeUrlLoader.handles(""));
    }

    @Test
    public void getUrlTest() {
        assertEquals(
            config.getBaseUrl() + "/some/image/wow.png",
            relativeUrlLoader.getUrl("/some/image/wow.png", 100, 100, new Options()));
        assertEquals(config.getBaseUrl() + "/some/addr/http/image.png",
            relativeUrlLoader.getUrl("/some/addr/http/image.png", 100, 100, new Options()));
        assertEquals(config.getBaseUrl() + "/topsecret/sandwich.svg",
            relativeUrlLoader.getUrl("/topsecret/sandwich.svg", 100, 100, new Options()));
    }

}
