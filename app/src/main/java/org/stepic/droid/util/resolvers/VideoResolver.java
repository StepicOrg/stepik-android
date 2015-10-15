package org.stepic.droid.util.resolvers;

import android.content.Context;

import com.squareup.otto.Bus;

import org.stepic.droid.events.video.VideoResolvedEvent;
import org.stepic.droid.model.Video;
import org.stepic.droid.model.VideoUrl;
import org.stepic.droid.util.AppConstants;

import java.util.List;

public class VideoResolver implements IVideoResolver {


    private Context mContext;
    private Bus mBus;

    public VideoResolver(Context context, Bus bus) {
        mContext = context;
        mBus = bus;
    }

    @Override
    public void resolveVideoUrl(final Video video) {
        //// TODO: 15.10.15 check in database by id, check availability of playing on the device, etc
//// TODO: 15.10.15 log all "return" statements
        
        if (video == null) return;
        List<VideoUrl> urlList = video.getUrls();
        if (urlList == null || urlList.size() == 0) return;

        String resolvedURL = null;
        for (int i = 0; i < urlList.size(); i++) {
            VideoUrl tempLink = urlList.get(i);
            if (tempLink != null) {
                String quality = tempLink.getQuality();
                if (quality != null &&
                        (quality.equals(AppConstants.DEFAULT_QUALITY) || i == urlList.size() - 1)) {
                    //// TODO: 15.10.15 determine video which is available for the phone. Not default
                    resolvedURL = tempLink.getUrl();
                    break;
                }
            }
        }

        if (resolvedURL != null) {
            mBus.post(new VideoResolvedEvent(video, resolvedURL));
            return;
        } else
            return;
    }
}
