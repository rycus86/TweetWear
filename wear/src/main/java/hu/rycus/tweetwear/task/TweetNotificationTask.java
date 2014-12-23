package hu.rycus.tweetwear.task;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import hu.rycus.tweetwear.common.api.ApiClientRunnable;
import hu.rycus.tweetwear.common.model.Tweet;
import hu.rycus.tweetwear.common.payload.NotificationSettings;
import hu.rycus.tweetwear.common.util.TweetData;
import hu.rycus.tweetwear.notification.SummaryNotification;
import hu.rycus.tweetwear.notification.TweetNotification;

public class TweetNotificationTask extends ApiClientRunnable {

    private static final String TAG = TweetNotificationTask.class.getSimpleName();

    private static final int LIMIT = 10;

    private final NotificationSettings settings;

    public TweetNotificationTask(final NotificationSettings settings) {
        this.settings = settings;
    }

    @Override
    protected void run(final Context context, final GoogleApiClient apiClient) throws Exception {
        final Collection<Tweet> existingTweets = TweetData.loadAll(apiClient);
        final Collection<Tweet> tweets = getLastTweets(existingTweets, LIMIT);

        Log.d(TAG, String.format("%d tweets found", tweets.size()));

        TweetNotification.clearAll(context);

        for (final Tweet tweet : tweets) {
            TweetNotification.send(context, tweet);
        }

        SummaryNotification.send(context, tweets.size(), settings);
    }

    private Collection<Tweet> getLastTweets(final Collection<Tweet> unordered, final int limit) {
        final List<Tweet> tweets = new ArrayList<Tweet>(unordered);
        Collections.sort(tweets);
        final int size = Math.min(limit, tweets.size());
        final List<Tweet> latest = tweets.subList(0, size);
        Collections.reverse(latest);
        return latest;
    }

}
