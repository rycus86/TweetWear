package hu.rycus.tweetwear.tasks;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;

import hu.rycus.tweetwear.common.api.ApiClientHelper;
import hu.rycus.tweetwear.common.api.ApiClientRunnable;
import hu.rycus.tweetwear.common.model.Tweet;
import hu.rycus.tweetwear.common.payload.ReadItLaterData;
import hu.rycus.tweetwear.common.util.Constants;
import hu.rycus.tweetwear.common.util.TweetData;
import hu.rycus.tweetwear.ril.ReadItLater;

public class ReadItLaterTask extends ApiClientRunnable {

    private static final String TAG = ReadItLaterTask.class.getSimpleName();

    private final ReadItLaterData data;

    public ReadItLaterTask(final ReadItLaterData data) {
        this.data = data;
    }

    @Override
    protected void run(final Context context, final GoogleApiClient apiClient) throws Exception {
        final boolean success = saveToDatabase(context);
        final Tweet tweet = updateTweet(apiClient, success);
        final String path = getPath(success);
        sendResult(apiClient, path, tweet);
        broadcastSuccess(context, success);
    }

    private boolean saveToDatabase(final Context context) {
        final boolean success = ReadItLater.insert(context, data.getUrl(), data.getTweet()) != -1L;
        if (success) {
            Log.d(TAG, String.format("Link saved to read later: %s", data.getUrl()));
        } else {
            Log.e(TAG, String.format("Failed to save link to read later: %s", data.getUrl()));
        }
        return success;
    }

    private Tweet updateTweet(final GoogleApiClient apiClient, final boolean success) {
        if (success) {
            final Tweet tweet = data.getTweet();
            tweet.setSavedToReadLater(true);

            // store tweet
            TweetData.of(tweet).sendAsync(apiClient);

            return tweet;
        } else {
            return null;
        }
    }

    private String getPath(final boolean success) {
        if (success) {
            return Constants.DataPath.RESULT_READ_IT_LATER_SUCCESS.withId(data.getTweet().getId());
        } else {
            return Constants.DataPath.RESULT_READ_IT_LATER_FAILURE.withId(data.getTweet().getId());
        }
    }

    private void sendResult(final GoogleApiClient apiClient, final String path, final Tweet tweet) {
        final byte[] payload = TweetData.of(tweet).serialize();
        ApiClientHelper.sendMessageToConnectedNode(apiClient, path, payload);
    }

    private void broadcastSuccess(final Context context,  final boolean success) {
        if (success) {
            final Intent intent = new Intent(Constants.ACTION_BROADCAST_READ_IT_LATER);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        }
    }

}
