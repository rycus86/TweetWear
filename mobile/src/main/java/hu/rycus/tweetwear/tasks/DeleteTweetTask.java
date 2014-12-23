package hu.rycus.tweetwear.tasks;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;

import hu.rycus.tweetwear.common.api.ApiClientHelper;
import hu.rycus.tweetwear.common.api.ApiClientRunnable;
import hu.rycus.tweetwear.common.util.Constants;
import hu.rycus.tweetwear.common.util.TweetData;

public class DeleteTweetTask extends ApiClientRunnable {

    private static final String TAG = DeleteTweetTask.class.getSimpleName();

    private final long tweetId;

    public DeleteTweetTask(final long tweetId) {
        this.tweetId = tweetId;
    }

    @Override
    protected void run(final Context context, final GoogleApiClient apiClient) throws Exception {
        if (deleteTweet(apiClient)) {
            sendResult(apiClient);
        } else {
            Log.e(TAG, String.format("Failed to delete tweet #%d", tweetId));
        }
    }

    private boolean deleteTweet(final GoogleApiClient apiClient) {
        return TweetData.forId(tweetId).deleteBlocking(apiClient);
    }

    private void sendResult(final GoogleApiClient apiClient) {
        final String path = getPath();
        ApiClientHelper.sendMessageToConnectedNode(apiClient, path, null);
    }

    private String getPath() {
        return Constants.DataPath.TWEET_DELETE.withId(tweetId);
    }

}
