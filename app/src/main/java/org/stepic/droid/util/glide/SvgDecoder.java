package org.stepic.droid.util.glide;

import androidx.annotation.NonNull;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.ResourceDecoder;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.resource.SimpleResource;
import com.caverock.androidsvg.PreserveAspectRatio;
import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGParseException;

import java.io.IOException;
import java.io.InputStream;

/**
 * Decodes an SVG internal representation from an {@link InputStream}.
 */
public class SvgDecoder implements ResourceDecoder<InputStream, SVG> {

    @Override
    public Resource<SVG> decode(InputStream source, int width, int height,
                                @NonNull Options options) throws IOException {
        try {
            SVG svg = SVG.getFromInputStream(source);
            svg.setDocumentHeight(height);
            svg.setDocumentWidth(width);
            svg.setDocumentPreserveAspectRatio(PreserveAspectRatio.LETTERBOX);
            return new SimpleResource<>(svg);
        } catch (SVGParseException ex) {
            throw new IOException("Cannot load SVG from stream", ex);
        }
    }

    @Override
    public boolean handles(@NonNull InputStream source, @NonNull Options options) {
        return true;
    }
}
