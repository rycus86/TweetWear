package hu.rycus.tweetwear.tasks;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;

import hu.rycus.tweetwear.common.api.ApiClientHelper;
import hu.rycus.tweetwear.common.api.ApiClientRunnable;
import hu.rycus.tweetwear.common.model.Tweet;
import hu.rycus.tweetwear.common.model.entities.Media;
import hu.rycus.tweetwear.common.util.Constants;

public class ShowImageTask extends ApiClientRunnable {

    private static final String TAG = ShowImageTask.class.getSimpleName();

    private static final String MEDIA_SIZE = "small"; // default to small images for now

    private final Tweet tweet;
    private final long mediaId;

    public ShowImageTask(final Tweet tweet, final long mediaId) {
        this.tweet = tweet;
        this.mediaId = mediaId;
    }

    @Override
    protected void run(final Context context, final GoogleApiClient apiClient) throws Exception {
        final String mediaUrl = getMediaUrl();
        final byte[] mediaData = downloadMedia(mediaUrl);
        final boolean success = mediaData != null;
        final String path = getPath(success);
        sendResult(apiClient, path, mediaData);
    }

    private String getMediaUrl() {
        final Media media = findMedia();
        if (media.getSizes() != null) {
            if (media.getSizes().getSmall() != null) {
                return media.getMediaUrl() + ":" + MEDIA_SIZE;
            }
        }

        return media.getMediaUrl();
    }

    private Media findMedia() {
        for (final Media media : tweet.getEntities().getMedia()) {
            if (media.getId() == mediaId) {
                return media;
            }
        }

        Log.wtf(TAG, String.format("Media #%d not found for tweet #%d", mediaId, tweet.getId()));
        return null;
    }

    private byte[] downloadMedia(final String mediaUrl) {
        Log.d(TAG, String.format("Downloading media from url: %s", mediaUrl));

        try {
            final ByteArrayOutputStream output = new ByteArrayOutputStream();

            final URL url = new URL(mediaUrl);
            final InputStream input = url.openStream();
            try {
                try {
                    final byte[] buffer = new byte[64 * 1024];
                    int read;

                    while ((read = input.read(buffer)) > -1) {
                        output.write(buffer, 0, read);
                    }

                    output.flush();
                } finally {
                    output.close();
                }
            } finally {
                input.close();
            }

            Log.d(TAG, String.format("Media downloaded from url: %s", mediaUrl));
            return output.toByteArray();
        } catch (Exception ex) {
            Log.e(TAG, String.format("Failed to download media from url: %s", mediaUrl), ex);
        }

        return null;
    }

    private String getPath(final boolean success) {
        if (success) {
            return Constants.DataPath.RESULT_SHOW_IMAGE_SUCCESS.withId(mediaId);
        } else {
            return Constants.DataPath.RESULT_SHOW_IMAGE_FAILURE.withId(mediaId);
        }
    }

    private void sendResult(final GoogleApiClient apiClient,
                            final String path, final byte[] mediaData) {
        ApiClientHelper.sendMessageToConnectedNode(apiClient, path, mediaData);
    }

}
