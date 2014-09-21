package hu.rycus.tweetwear.tasks;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;

import hu.rycus.tweetwear.common.api.ApiClientHelper;
import hu.rycus.tweetwear.common.api.ApiClientRunnable;
import hu.rycus.tweetwear.common.model.Tweet;
import hu.rycus.tweetwear.common.util.Constants;
import hu.rycus.tweetwear.common.util.TweetData;
import hu.rycus.tweetwear.ril.ReadItLater;

public class ReadItLaterTask extends ApiClientRunnable {

    private static final String TAG = ReadItLaterTask.class.getSimpleName();

    private final Tweet tweet;

    public ReadItLaterTask(final Tweet tweet) {
        this.tweet = tweet;
    }

    @Override
    protected void run(final Context context, final GoogleApiClient apiClient) throws Exception {
        final boolean success = saveToDatabase(context);
        updateTweet(apiClient, success);
        final String path = getPath(success);
        sendResult(apiClient, path, tweet);
        broadcastSuccess(context, success);
    }

    private boolean saveToDatabase(final Context context) {
        final boolean success = ReadItLater.insert(context, tweet) != -1L;
        if (success) {
            Log.d(TAG, String.format("Tweet #%d saved to read later", tweet.getId()));
        } else {
            Log.e(TAG, String.format("Failed to save tweet #%d to read later", tweet.getId()));
        }
        return success;
    }

    private void updateTweet(final GoogleApiClient apiClient, final boolean success) {
        if (success) {
            tweet.setSavedToReadLater(true);

            // store tweet
            TweetData.of(tweet).sendAsync(apiClient);
        }
    }

    private String getPath(final boolean success) {
        if (success) {
            return Constants.DataPath.RESULT_READ_IT_LATER_SUCCESS.withId(tweet.getId());
        } else {
            return Constants.DataPath.RESULT_READ_IT_LATER_FAILURE.withId(tweet.getId());
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
