package hu.rycus.tweetwear.task;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;

import hu.rycus.tweetwear.FinishingConfirmationActivity;
import hu.rycus.tweetwear.PostTweetActivity;
import hu.rycus.tweetwear.R;
import hu.rycus.tweetwear.common.api.ApiClientHelper;
import hu.rycus.tweetwear.common.api.ApiClientRunnable;
import hu.rycus.tweetwear.common.util.Constants;

public class PostTweetTask extends ApiClientRunnable {

    private static final String TAG = PostTweetTask.class.getSimpleName();

    private final Mode mode;
    private final String text;
    private final Long originalTweetId;

    private enum Mode {
        NEW_TWEET, REPLY
    }

    private PostTweetTask(final Mode mode, final String text, final Long originalTweetId) {
        this.mode = mode;
        this.text = text;
        this.originalTweetId = originalTweetId;
    }

    @Override
    protected void run(final Context context, final GoogleApiClient apiClient) throws Exception {
        final String path = getPath();
        if (!ApiClientHelper.sendMessageToConnectedNode(apiClient, path, text.getBytes())) {
            onTaskFailure(context);
        }
    }

    private void onTaskFailure(final Context context) {
        Log.e(TAG, String.format("Failed to %s",
                mode == Mode.NEW_TWEET ? "post a new tweet" : "reply"));

        PostTweetActivity.notifyTaskFinished(context);

        final String message = getErrorMessage(context);
        FinishingConfirmationActivity.show(context, false, message);
    }

    private String getPath() {
        switch (mode) {
            case NEW_TWEET:
                return Constants.DataPath.POST_NEW_TWEET.get();
            case REPLY:
                return Constants.DataPath.POST_REPLY.withId(originalTweetId);
        }

        throw new IllegalArgumentException(String.format("Illegal mode setting: %s", mode));
    }

    private String getErrorMessage(final Context context) {
        switch (mode) {
            case NEW_TWEET:
                return context.getString(R.string.post_tweet_failure_new_tweet);
            case REPLY:
                return context.getString(R.string.post_tweet_failure_reply);
        }

        throw new IllegalArgumentException(String.format("Illegal mode setting: %s", mode));
    }

    public static PostTweetTask newTweet(final String text) {
        return new PostTweetTask(Mode.NEW_TWEET, text, null);
    }

    public static PostTweetTask reply(final String text, final long originalTweetId) {
        return new PostTweetTask(Mode.REPLY, text, originalTweetId);
    }

}
