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

public class FavoriteTask extends ApiClientRunnable {

    private static final String TAG = FavoriteTask.class.getSimpleName();

    private final long tweetId;

    public FavoriteTask(final long tweetId) {
        this.tweetId = tweetId;
    }

    @Override
    protected void run(final Context context, final GoogleApiClient apiClient) throws Exception {
        final Tweet tweet = processFavorite(context, apiClient);
        final String path = createPath(tweet);
        sendResult(apiClient, path, tweet);
    }

    private Tweet processFavorite(final Context context, final GoogleApiClient apiClient) {
        final Token accessToken = Preferences.getUserToken(context);
        final Tweet tweet = TwitterFactory.createClient().favorite(accessToken, tweetId, null);

        if (tweet != null) {
            Log.d(TAG, String.format("Favorite successful, favorite count: %d",
                    tweet.getFavoriteCount()));

            // store tweet
            TweetData.of(tweet).sendAsync(apiClient);

            return tweet;
        } else {
            Log.w(TAG, "Favorite failed");
            return null;
        }
    }

    private String createPath(final Tweet tweet) {
        if (tweet != null) {
            return Constants.DataPath.RESULT_FAVORITE_SUCCESS.withId(tweetId);
        } else {
            return Constants.DataPath.RESULT_FAVORITE_FAILURE.withId(tweetId);
        }
    }

    private void sendResult(final GoogleApiClient apiClient, final String path, final Tweet tweet) {
        final byte[] payload = TweetData.of(tweet).serialize();
        ApiClientHelper.sendMessageToConnectedNode(apiClient, path, payload);
    }

}
