package hu.rycus.tweetwear.tasks;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;

import org.scribe.model.Token;

import hu.rycus.tweetwear.common.api.ApiClientHelper;
import hu.rycus.tweetwear.common.api.ApiClientRunnable;
import hu.rycus.tweetwear.common.model.Tweet;
import hu.rycus.tweetwear.common.util.Constants;
import hu.rycus.tweetwear.common.util.TweetData;
import hu.rycus.tweetwear.preferences.Preferences;
import hu.rycus.tweetwear.twitter.TwitterFactory;

public class RetweetTask extends ApiClientRunnable {

    private static final String TAG = RetweetTask.class.getSimpleName();

    private final long tweetId;

    public RetweetTask(final long tweetId) {
        this.tweetId = tweetId;
    }

    @Override
    protected void run(final Context context, final GoogleApiClient apiClient) throws Exception {
        final Tweet tweet = processRetweet(context, apiClient);
        final String path = createPath(tweet);
        sendResult(apiClient, path, tweet);
    }

    private Tweet processRetweet(final Context context, final GoogleApiClient apiClient) {
        final Token accessToken = Preferences.getUserToken(context);
        final Tweet tweet = TwitterFactory.restClient().retweet(accessToken, tweetId, null);

        if (tweet != null) {
            final Tweet retweeted = tweet.getRetweetedStatus();

            /*
             * we can not retweet our own tweets
             * so no need to check if we created the original one
             */

            Log.d(TAG, String.format("Retweet successful, retweet count: %d",
                    retweeted.getRetweetCount()));

            // store tweet
            TweetData.of(tweet).sendAsync(apiClient);

            return retweeted;
        } else {
            Log.w(TAG, "Retweet failed");
            return null;
        }
    }

    private String createPath(final Tweet tweet) {
        if (tweet != null) {
            return Constants.DataPath.RESULT_RETWEET_SUCCESS.withId(tweetId);
        } else {
            return Constants.DataPath.RESULT_RETWEET_FAILURE.withId(tweetId);
        }
    }

    private void sendResult(final GoogleApiClient apiClient, final String path, final Tweet tweet) {
        final byte[] payload = TweetData.of(tweet).serialize();
        ApiClientHelper.sendMessageToConnectedNode(apiClient, path, payload);
    }

}
