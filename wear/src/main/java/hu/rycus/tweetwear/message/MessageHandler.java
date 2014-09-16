package hu.rycus.tweetwear.message;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;

import hu.rycus.tweetwear.FinishingConfirmationActivity;
import hu.rycus.tweetwear.PostTweetActivity;
import hu.rycus.tweetwear.R;
import hu.rycus.tweetwear.ShowImageActivity;
import hu.rycus.tweetwear.common.api.ApiClientHelper;
import hu.rycus.tweetwear.common.model.Tweet;
import hu.rycus.tweetwear.common.payload.NotificationSettings;
import hu.rycus.tweetwear.common.util.Constants;
import hu.rycus.tweetwear.common.util.TweetData;
import hu.rycus.tweetwear.notification.PromotionNotification;
import hu.rycus.tweetwear.notification.TweetNotification;
import hu.rycus.tweetwear.task.TweetNotificationTask;

public class MessageHandler {

    private static final String TAG = MessageHandler.class.getSimpleName();

    private MessageHandler() {}

    public static void handle(final Context context, final MessageEvent messageEvent) {
        final String path = messageEvent.getPath();
        try {
            if (Constants.DataPath.SYNC_COMPLETE.matches(path)) {
                final NotificationSettings settings =
                        NotificationSettings.parse(messageEvent.getData());
                onSyncComplete(context, settings);
            } else if (Constants.DataPath.PROMOTION.matches(path)) {
                final String promotionId = Constants.DataPath.PROMOTION.replace(path, "$1");
                final String promotionText = new String(messageEvent.getData());
                onPromotionReceived(context, promotionId, promotionText);
            } else if (Constants.DataPath.RESULT_RETWEET_SUCCESS.matches(path)) {
                final Tweet tweet = TweetData.parse(messageEvent.getData());
                onSuccessfulRetweet(context, tweet);
            } else if (Constants.DataPath.RESULT_RETWEET_FAILURE.matches(path)) {
                Log.w(TAG, "Retweet failed");
                FinishingConfirmationActivity.show(context, false, context.getString(R.string.retweet_failed));
            } else if (Constants.DataPath.RESULT_FAVORITE_SUCCESS.matches(path)) {
                final Tweet tweet = TweetData.parse(messageEvent.getData());
                onSuccessfulFavorite(context, tweet);
            } else if (Constants.DataPath.RESULT_FAVORITE_FAILURE.matches(path)) {
                Log.w(TAG, "Favorite failed");
                FinishingConfirmationActivity.show(context, false, context.getString(R.string.favorite_failed));
            } else if (Constants.DataPath.RESULT_POST_NEW_TWEET_SUCCESS.matches(path)) {
                final Tweet tweet = TweetData.parse(messageEvent.getData());
                final String message = context.getString(R.string.post_tweet_successful_new_tweet);
                onSuccessfulPost(context, tweet, message);
            } else if (Constants.DataPath.RESULT_POST_REPLY_SUCCESS.matches(path)) {
                final Tweet tweet = TweetData.parse(messageEvent.getData());
                final String message = context.getString(R.string.post_tweet_successful_reply);
                onSuccessfulPost(context, tweet, message);
            } else if (Constants.DataPath.RESULT_POST_NEW_TWEET_FAILURE.matches(path)) {
                final String message = context.getString(R.string.post_tweet_failure_new_tweet);
                onFailedPost(context, message);
            } else if (Constants.DataPath.RESULT_POST_REPLY_FAILURE.matches(path)) {
                final String message = context.getString(R.string.post_tweet_failure_reply);
                onFailedPost(context, message);
            } else if (Constants.DataPath.RESULT_READ_IT_LATER_SUCCESS.matches(path)) {
                final Tweet tweet = TweetData.parse(messageEvent.getData());
                onSuccessfulReadLater(context, tweet);
            } else if (Constants.DataPath.RESULT_READ_IT_LATER_FAILURE.matches(path)) {
                Log.w(TAG, "Read later failed");
                final String message = context.getString(R.string.read_later_failure);
                FinishingConfirmationActivity.show(context, false, message);
            } else if (Constants.DataPath.RESULT_SHOW_IMAGE_SUCCESS.matches(path)) {
                onImageDataReceived(context, messageEvent.getData());
            } else {
                onLoadingImageFailed(context);
            }
        } catch (Exception ex) {
            Log.e(TAG, String.format("Failed to handle message for path: %s", path), ex);
        }
    }

    private static void onSyncComplete(final Context context, final NotificationSettings settings) {
        Log.d(TAG, "Starting Tweet notification task");
        ApiClientHelper.runAsynchronously(context, new TweetNotificationTask(settings));
    }

    private static void onPromotionReceived(final Context context,
                                            final String promotionId, final String promotionText) {
        Log.d(TAG, String.format("Received promotion text: %s", promotionText));
        PromotionNotification.send(context, promotionId, promotionText);
    }

    private static void onSuccessfulRetweet(final Context context, final Tweet tweet) {
        FinishingConfirmationActivity.show(context, true, context.getString(R.string.retweeted));

        Log.d(TAG, String.format("Retweet successful for @%s - %s",
                tweet.getUser().getScreenName(), tweet.getText()));

        TweetNotification.send(context, tweet);
    }

    private static void onSuccessfulFavorite(final Context context, final Tweet tweet) {
        FinishingConfirmationActivity.show(context, true, context.getString(R.string.favorited));

        Log.d(TAG, String.format("Favorite successful for @%s - %s",
                tweet.getUser().getScreenName(), tweet.getText()));

        TweetNotification.send(context, tweet);
    }

    private static void onSuccessfulPost(final Context context, final Tweet tweet,
                                         final String message) {

        FinishingConfirmationActivity.show(context, true, message);
        TweetNotification.send(context, tweet);

        Log.d(TAG, String.format("Posting a tweet was successful: @%s - %s",
                tweet.getUser().getScreenName(), tweet.getText()));

        PostTweetActivity.notifyTaskFinished(context);
    }

    private static void onFailedPost(final Context context, final String message) {
        FinishingConfirmationActivity.show(context, false, message);

        Log.d(TAG, "Failed to post a tweet");

        PostTweetActivity.notifyTaskFinished(context);
    }

    private static void onSuccessfulReadLater(final Context context, final Tweet tweet) {
        final String message = context.getString(R.string.read_later_successful);
        FinishingConfirmationActivity.show(context, true, message);

        Log.d(TAG, String.format("Read later successful for @%s - %s",
                tweet.getUser().getScreenName(), tweet.getText()));

        TweetNotification.send(context, tweet);
    }

    private static void onImageDataReceived(final Context context, final byte[] mediaData) {
        final Bitmap bitmap = BitmapFactory.decodeByteArray(mediaData, 0, mediaData.length);

        Log.d(TAG, "Showing downloaded media");

        ShowImageActivity.startForImage(context, bitmap);
    }

    private static void onLoadingImageFailed(final Context context) {
        Log.w(TAG, "Show image failed");
        ShowImageActivity.startForFailure(context);
    }

}
