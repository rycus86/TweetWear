package hu.rycus.tweetwear.task;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;

import hu.rycus.tweetwear.common.api.ApiClientHelper;
import hu.rycus.tweetwear.common.api.ApiClientRunnable;
import hu.rycus.tweetwear.common.model.Tweet;
import hu.rycus.tweetwear.common.payload.ReadItLaterData;
import hu.rycus.tweetwear.common.util.Constants;

public class ReadItLaterTask extends ApiClientRunnable {

    private static final String TAG = ReadItLaterTask.class.getSimpleName();

    private final Tweet tweet;
    private final String url;

    public ReadItLaterTask(final Tweet tweet, final String url) {
        this.tweet = tweet;
        this.url = url;
    }

    @Override
    protected void run(final Context context, final GoogleApiClient apiClient) throws Exception {
        final String path = Constants.DataPath.READ_IT_LATER.withId(tweet.getId());
        final ReadItLaterData payload = new ReadItLaterData(tweet, url);
        if (!ApiClientHelper.sendMessageToConnectedNode(apiClient, path, payload)) {
            Log.e(TAG, String.format("Failed to request read later for url: %s", url));
        }
    }

}
