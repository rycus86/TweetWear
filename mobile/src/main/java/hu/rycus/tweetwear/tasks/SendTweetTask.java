package hu.rycus.tweetwear.tasks;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;

import hu.rycus.tweetwear.common.api.ApiClientHelper;
import hu.rycus.tweetwear.common.api.ApiClientRunnable;
import hu.rycus.tweetwear.common.model.Tweet;
import hu.rycus.tweetwear.common.payload.NotificationSettings;
import hu.rycus.tweetwear.common.payload.TweetWithNotificationSettings;
import hu.rycus.tweetwear.common.util.Constants;
import hu.rycus.tweetwear.common.util.TweetData;
import hu.rycus.tweetwear.preferences.Preferences;

public class SendTweetTask extends ApiClientRunnable {

    private static final String TAG = SendTweetTask.class.getSimpleName();

    private final Tweet tweet;

    public SendTweetTask(final Tweet tweet) {
        this.tweet = tweet;
    }

    @Override
    protected void run(final Context context, final GoogleApiClient apiClient) throws Exception {
        if (saveTweet(apiClient)) {
            sendResult(context, apiClient);
        } else {
            Log.e(TAG, String.format("Failed to send tweet #%d - @%s: %s",
                    tweet.getId(), tweet.getUser().getScreenName(), tweet.getText()));
        }
    }

    private boolean saveTweet(final GoogleApiClient apiClient) {
        return TweetData.of(tweet).sendBlocking(apiClient);
    }

    private void sendResult(final Context context, final GoogleApiClient apiClient) {
        final String path = getPath();
        final byte[] payload = getPayload(context);
        ApiClientHelper.sendMessageToConnectedNode(apiClient, path, payload);
    }

    private String getPath() {
        return Constants.DataPath.TWEET_RECEIVED.withId(tweet.getId());
    }

    private byte[] getPayload(final Context context) {
        final NotificationSettings settings = getNotificationSettings(context);
        final TweetWithNotificationSettings payload =
                new TweetWithNotificationSettings(tweet, settings);
        return payload.serialize();
    }

    private NotificationSettings getNotificationSettings(final Context context) {
        final NotificationSettings settings = new NotificationSettings();
        settings.setVibrate(Preferences.isVibrationEnabled(context));
        return settings;
    }

}
