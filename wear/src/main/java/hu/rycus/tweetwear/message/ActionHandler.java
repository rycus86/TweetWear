package hu.rycus.tweetwear.message;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import hu.rycus.tweetwear.common.api.ApiClientHelper;
import hu.rycus.tweetwear.common.util.Constants;
import hu.rycus.tweetwear.task.FavoriteTask;
import hu.rycus.tweetwear.task.RetweetTask;

public class ActionHandler {

    private static final String TAG = ActionHandler.class.getSimpleName();

    private ActionHandler() {}

    public static void handle(final Context context, final Intent intent) {
        if (Constants.ACTION_SEND_RETWEET.equals(intent.getAction())) {
            sendRetweet(context, intent);
        } else if (Constants.ACTION_SEND_FAVORITE.equals(intent.getAction())) {
            sendFavorite(context, intent);
        }
    }

    private static void sendRetweet(final Context context, final Intent intent) {
        final long tweetId = intent.getLongExtra(Constants.EXTRA_TWEET_ID, -1L);
        if (tweetId != -1L) {
            Log.d(TAG, String.format("Sending retweet for id #%d", tweetId));
            ApiClientHelper.runAsynchronously(context, new RetweetTask(tweetId));
        }
    }

    private static void sendFavorite(final Context context, final Intent intent) {
        final long tweetId = intent.getLongExtra(Constants.EXTRA_TWEET_ID, -1L);
        if (tweetId != -1L) {
            Log.d(TAG, String.format("Sending favorite for id #%d", tweetId));
            ApiClientHelper.runAsynchronously(context, new FavoriteTask(tweetId));
        }
    }

}
