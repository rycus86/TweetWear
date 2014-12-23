package hu.rycus.tweetwear.tasks;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;

import java.util.Collection;

import hu.rycus.tweetwear.common.api.ApiClientRunnable;
import hu.rycus.tweetwear.common.model.Tweet;
import hu.rycus.tweetwear.common.util.TweetData;
import hu.rycus.tweetwear.preferences.Preferences;

public class ClearExistingTweetsTask extends ApiClientRunnable {

    private static final String TAG = ClearExistingTweetsTask.class.getSimpleName();

    private final boolean markLastTweetId;

    public ClearExistingTweetsTask(final boolean markLastTweetId) {
        this.markLastTweetId = markLastTweetId;
    }

    @Override
    protected void run(final Context context, final GoogleApiClient apiClient) throws Exception {
        long lastTweetId = -1L;

        final Collection<Tweet> tweets = TweetData.loadAll(apiClient);
        for (final Tweet tweet : tweets) {
            TweetData.of(tweet).deleteBlocking(apiClient);

            if (markLastTweetId) {
                lastTweetId = Math.max(lastTweetId, tweet.getId());
            }
        }

        Log.d(TAG, String.format("Cleared %d tweets", tweets.size()));

        if (markLastTweetId && lastTweetId > -1L) {
            if (Preferences.saveLastReadTweetId(context, lastTweetId)) {
                Log.d(TAG, String.format("Marked last tweet ID as #%d", lastTweetId));
            } else {
                Log.e(TAG, "Failed to mark last tweet ID");
            }
        }
    }

}
