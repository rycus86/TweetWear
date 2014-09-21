package hu.rycus.tweetwear;

import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import hu.rycus.tweetwear.common.api.ApiClientHelper;
import hu.rycus.tweetwear.common.model.Tweet;
import hu.rycus.tweetwear.common.util.Constants;
import hu.rycus.tweetwear.common.util.TweetData;
import hu.rycus.tweetwear.preferences.Preferences;
import hu.rycus.tweetwear.tasks.ClearExistingTweetsTask;
import hu.rycus.tweetwear.tasks.FavoriteTask;
import hu.rycus.tweetwear.tasks.PostTweetTask;
import hu.rycus.tweetwear.tasks.ReadItLaterTask;
import hu.rycus.tweetwear.tasks.RetweetTask;
import hu.rycus.tweetwear.tasks.ShowImageTask;

public class MessageListenerService extends WearableListenerService {

    private static final String TAG = MessageListenerService.class.getSimpleName();

    @Override
    public void onMessageReceived(final MessageEvent messageEvent) {
        final String path = messageEvent.getPath();
        try {
            if (Constants.DataPath.RETWEET.matches(path)) {
                processRetweet(path);
            } else if (Constants.DataPath.FAVORITE.matches(path)) {
                processFavorite(path);
            } else if (Constants.DataPath.POST_NEW_TWEET.matches(path)) {
                final String text = new String(messageEvent.getData());
                processPostNewTweet(text);
            } else if (Constants.DataPath.POST_REPLY.matches(path)) {
                final String text = new String(messageEvent.getData());
                processPostReply(path, text);
            } else if (Constants.DataPath.MARK_AS_READ.matches(path)) {
                processMarkAsRead();
            } else if (Constants.DataPath.READ_IT_LATER.matches(path)) {
                final Tweet tweet = TweetData.parse(messageEvent.getData());
                processReadItLater(tweet);
            } else if (Constants.DataPath.SHOW_IMAGE.matches(path)) {
                final Tweet tweet = TweetData.parse(messageEvent.getData());
                processShowImage(tweet, path);
            }
        } catch (Exception ex) {
            Log.d(TAG, String.format("Failed to process message with path: %s", path), ex);
        }
    }

    private void processRetweet(final String path) {
        final String strTweetId = Constants.DataPath.RETWEET.replace(path, "$1");
        final long tweetId = Long.parseLong(strTweetId);
        Log.d(TAG, String.format("Received retweet request: %d", tweetId));

        final RetweetTask task = new RetweetTask(tweetId);
        ApiClientHelper.runAsynchronously(this, task);
    }

    private void processFavorite(final String path) {
        final String strTweetId = Constants.DataPath.FAVORITE.replace(path, "$1");
        final long tweetId = Long.parseLong(strTweetId);
        Log.d(TAG, String.format("Received favorite request: %d", tweetId));

        final FavoriteTask task = new FavoriteTask(tweetId);
        ApiClientHelper.runAsynchronously(this, task);
    }

    private void processPostNewTweet(final String text) {
        Log.d(TAG, String.format("Received request to post a new tweet saying: %s", text));

        final PostTweetTask task = PostTweetTask.newTweet(text);
        ApiClientHelper.runAsynchronously(this, task);
    }

    private void processPostReply(final String path, final String text) {
        final String strTweetId = Constants.DataPath.POST_REPLY.replace(path, "$1");
        final long tweetId = Long.parseLong(strTweetId);
        Log.d(TAG, String.format("Received request to post a reply saying: %s", text));

        final PostTweetTask task = PostTweetTask.reply(text, tweetId);
        ApiClientHelper.runAsynchronously(this, task);
    }

    private void processMarkAsRead() {
        Log.d(TAG, "Received mark-as-read request");

        if (Preferences.isMarkAsReadOnDeleteEnabled(this)) {
            final ClearExistingTweetsTask task = new ClearExistingTweetsTask(true);
            ApiClientHelper.runAsynchronously(this, task);
        } else {
            Log.d(TAG, "Mark-as-read is disabled, not doing anything");
        }
    }

    private void processReadItLater(final Tweet tweet) {
        Log.d(TAG, String.format("Received read later request for tweet #%d", tweet.getId()));

        final ReadItLaterTask task = new ReadItLaterTask(tweet);
        ApiClientHelper.runAsynchronously(this, task);
    }

    private void processShowImage(final Tweet tweet, final String path) {
        final String strMediaId = Constants.DataPath.SHOW_IMAGE.replace(path, "$1");
        final long mediaId = Long.parseLong(strMediaId);

        Log.d(TAG, String.format("Received show image request to id: %d", mediaId));

        final ShowImageTask task = new ShowImageTask(tweet, mediaId);
        ApiClientHelper.runAsynchronously(this, task);
    }

}
