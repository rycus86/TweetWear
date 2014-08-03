package hu.rycus.tweetwear.message;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;

import hu.rycus.tweetwear.R;
import hu.rycus.tweetwear.common.api.ApiClientHelper;
import hu.rycus.tweetwear.common.model.Tweet;
import hu.rycus.tweetwear.common.util.Constants;
import hu.rycus.tweetwear.common.util.TweetData;
import hu.rycus.tweetwear.notification.TweetNotification;
import hu.rycus.tweetwear.task.TweetNotificationTask;

public class MessageHandler {

    private static final String TAG = MessageHandler.class.getSimpleName();

    private MessageHandler() {}

    public static void handle(final Context context, final MessageEvent messageEvent) {
        final String path = messageEvent.getPath();
        if (Constants.DataPath.SYNC_COMPLETE.matches(path)) {
            onSyncComplete(context);
        } else if (Constants.DataPath.RESULT_RETWEET_SUCCESS.matches(path)) {
            final Tweet tweet = TweetData.parse(messageEvent.getData());
            onSuccessfulRetweet(context, tweet);
        } else if (Constants.DataPath.RESULT_RETWEET_FAILURE.matches(path)) {
            Log.w(TAG, "Retweet failed");
            broadcastTaskResult(context, false, context.getString(R.string.retweet_failed));
        } else if (Constants.DataPath.RESULT_FAVORITE_SUCCESS.matches(path)) {
            final Tweet tweet = TweetData.parse(messageEvent.getData());
            onSuccessfulFavorite(context, tweet);
        } else if (Constants.DataPath.RESULT_FAVORITE_FAILURE.matches(path)) {
            Log.w(TAG, "Favorite failed");
            broadcastTaskResult(context, false, context.getString(R.string.favorite_failed));
        }
    }

    private static void onSyncComplete(final Context context) {
        Log.d(TAG, "Starting Tweet notification task");
        ApiClientHelper.runAsynchronously(context, new TweetNotificationTask());
    }

    private static void onSuccessfulRetweet(final Context context, final Tweet tweet) {
        broadcastTaskResult(context, true, context.getString(R.string.retweeted));

        Log.d(TAG, String.format("Retweet successful for @%s - %s",
                tweet.getUser().getScreenName(), tweet.getText()));

        TweetNotification.send(context, tweet);
    }

    private static void onSuccessfulFavorite(final Context context, final Tweet tweet) {
        broadcastTaskResult(context, true, context.getString(R.string.favorited));

        Log.d(TAG, String.format("Favorite successful for @%s - %s",
                tweet.getUser().getScreenName(), tweet.getText()));

        TweetNotification.send(context, tweet);
    }

    private static void broadcastTaskResult(final Context context,
                                            final boolean success, final String message) {
        final Intent intent = new Intent(Constants.ACTION_TASK_RESULT);
        intent.putExtra(Constants.EXTRA_SUCCESS_FLAG, success);
        intent.putExtra(Constants.EXTRA_CONFIRMATION_MESSAGE, message);
        context.sendBroadcast(intent);
    }

}
