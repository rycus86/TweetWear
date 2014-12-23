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

public class PostTweetTask extends ApiClientRunnable {

    private static final String TAG = PostTweetTask.class.getSimpleName();

    private final Mode mode;
    private final String content;
    private final Long originalTweetId;

    private enum Mode {
        NEW_TWEET, REPLY
    }

    private PostTweetTask(final Mode mode, final String content, final Long originalTweetId) {
        this.mode = mode;
        this.content = content;
        this.originalTweetId = originalTweetId;
    }

    @Override
    protected void run(final Context context, final GoogleApiClient apiClient) throws Exception {
        final Tweet tweet = processTask(context, apiClient);
        final String path = getPath(tweet);
        sendResult(apiClient, path, tweet);
    }

    private Tweet processTask(final Context context, final GoogleApiClient apiClient) {
        final Token token = Preferences.getUserToken(context);
        final Long inReplyToId = mode == Mode.REPLY ? originalTweetId : null;
        final Tweet tweet = TwitterFactory.restClient().postStatus(token, content, inReplyToId);
        if (tweet != null) {
            tweet.setOwnTweet(true);

            Log.d(TAG, String.format("Posting a %s was successful, created as: %s",
                    mode == Mode.REPLY ? "reply" : "new tweet", tweet.getText()));

            // store tweet
            TweetData.of(tweet).sendAsync(apiClient);
        }

        return tweet;
    }

    private String getPath(final Tweet tweet) {
        if (tweet != null) {
            if (mode.equals(Mode.NEW_TWEET)) {
                return Constants.DataPath.RESULT_POST_NEW_TWEET_SUCCESS.withId(tweet.getId());
            } else if (mode.equals(Mode.REPLY)) {
                return Constants.DataPath.RESULT_POST_REPLY_SUCCESS.withId(tweet.getId());
            }
        } else {
            if (mode.equals(Mode.NEW_TWEET)) {
                return Constants.DataPath.RESULT_POST_NEW_TWEET_FAILURE.get();
            } else if (mode.equals(Mode.REPLY)) {
                return Constants.DataPath.RESULT_POST_REPLY_FAILURE.get();
            }
        }

        return null; // Should not happen
    }

    private void sendResult(final GoogleApiClient apiClient, final String path, final Tweet tweet) {
        final byte[] payload = TweetData.of(tweet).serialize();
        ApiClientHelper.sendMessageToConnectedNode(apiClient, path, payload);
    }

    public static PostTweetTask newTweet(final String text) {
        return new PostTweetTask(Mode.NEW_TWEET, text, null);
    }

    public static PostTweetTask reply(final String text, final long originalTweetId) {
        return new PostTweetTask(Mode.REPLY, text, originalTweetId);
    }

}
