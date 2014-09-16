package hu.rycus.tweetwear.message;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import hu.rycus.tweetwear.ShowImageActivity;
import hu.rycus.tweetwear.common.api.ApiClientHelper;
import hu.rycus.tweetwear.common.model.Tweet;
import hu.rycus.tweetwear.common.util.Constants;
import hu.rycus.tweetwear.common.util.TweetData;
import hu.rycus.tweetwear.task.FavoriteTask;
import hu.rycus.tweetwear.task.MarkAsReadTask;
import hu.rycus.tweetwear.task.ReadItLaterTask;
import hu.rycus.tweetwear.task.RetweetTask;
import hu.rycus.tweetwear.task.ShowImageTask;

public class ActionHandler {

    private static final String TAG = ActionHandler.class.getSimpleName();

    private ActionHandler() {}

    public static void handle(final Context context, final Intent intent) {
        if (Constants.ACTION_SEND_RETWEET.equals(intent.getAction())) {
            sendRetweet(context, intent);
        } else if (Constants.ACTION_SEND_FAVORITE.equals(intent.getAction())) {
            sendFavorite(context, intent);
        } else if (Constants.ACTION_MARK_AS_READ.equals(intent.getAction())) {
            sendMarkAsRead(context);
        } else if (Constants.ACTION_READ_IT_LATER.equals(intent.getAction())) {
            sendReadItLater(context, intent);
        } else if (Constants.ACTION_SHOW_IMAGE.equals(intent.getAction())) {
            sendShowImage(context, intent);
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

    private static void sendMarkAsRead(final Context context) {
        Log.d(TAG, "Sending mark-as-read request");
        ApiClientHelper.runAsynchronously(context, new MarkAsReadTask());
    }

    private static void sendReadItLater(final Context context, final Intent intent) {
        final String tweetJson = intent.getStringExtra(Constants.EXTRA_TWEET_JSON);
        final Tweet tweet = TweetData.parse(tweetJson);
        final String url = intent.getStringExtra(Constants.EXTRA_READ_IT_LATER_URL);
        if (tweet != null && url != null) {
            Log.d(TAG, String.format("Requesting read later for url: %s", url));
            ApiClientHelper.runAsynchronously(context, new ReadItLaterTask(tweet, url));
        }
    }

    private static void sendShowImage(final Context context, final Intent intent) {
        final String tweetJson = intent.getStringExtra(Constants.EXTRA_TWEET_JSON);
        final Tweet tweet = TweetData.parse(tweetJson);
        final long mediaId = intent.getLongExtra(Constants.EXTRA_SHOW_MEDIA_ID, -1);
        if (tweet != null && mediaId != -1L) {
            ShowImageActivity.start(context);

            Log.d(TAG, String.format("Requesting image with ID: %d", mediaId));
            ApiClientHelper.runAsynchronously(context, new ShowImageTask(tweet, mediaId));
        }
    }

}
