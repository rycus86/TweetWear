package hu.rycus.tweetwear.task;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;

import hu.rycus.tweetwear.common.api.ApiClientHelper;
import hu.rycus.tweetwear.common.api.ApiClientRunnable;
import hu.rycus.tweetwear.common.model.Tweet;
import hu.rycus.tweetwear.common.util.Constants;

public class ShowImageTask extends ApiClientRunnable {

    private static final String TAG = ShowImageTask.class.getSimpleName();

    private final Tweet tweet;
    private final long mediaId;

    public ShowImageTask(final Tweet tweet, final long mediaId) {
        this.tweet = tweet;
        this.mediaId = mediaId;
    }

    @Override
    protected void run(final Context context, final GoogleApiClient apiClient) throws Exception {
        final String path = Constants.DataPath.SHOW_IMAGE.withId(mediaId);
        if (!ApiClientHelper.sendMessageToConnectedNode(apiClient, path, tweet)) {
            Log.e(TAG, String.format("Failed to request image for id: %d", mediaId));
        }
    }

}
