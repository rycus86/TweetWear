package hu.rycus.tweetwear.task;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;

import hu.rycus.tweetwear.common.api.ApiClientHelper;
import hu.rycus.tweetwear.common.api.ApiClientRunnable;
import hu.rycus.tweetwear.common.model.Tweet;
import hu.rycus.tweetwear.common.util.Constants;

public class ReadItLaterTask extends ApiClientRunnable {

    private static final String TAG = ReadItLaterTask.class.getSimpleName();

    private final Tweet tweet;

    public ReadItLaterTask(final Tweet tweet) {
        this.tweet = tweet;
    }

    @Override
    protected void run(final Context context, final GoogleApiClient apiClient) throws Exception {
        final String path = Constants.DataPath.READ_IT_LATER.withId(tweet.getId());
        if (!ApiClientHelper.sendMessageToConnectedNode(apiClient, path, tweet)) {
            Log.e(TAG, String.format("Failed to request read later for tweet #%d", tweet.getId()));
        }
    }

}
