package hu.rycus.tweetwear.twitter.stream.events;

import android.content.Context;
import android.util.Log;

import com.fasterxml.jackson.databind.JsonNode;

import hu.rycus.tweetwear.common.api.ApiClientHelper;
import hu.rycus.tweetwear.common.model.Tweet;
import hu.rycus.tweetwear.tasks.SendTweetTask;

public class TweetEvent extends Tweet implements StreamEvent {

    private static final String TAG = TweetEvent.class.getSimpleName();

    public static boolean matches(final JsonNode rootNode) {
        return rootNode.has("created_at") && rootNode.has("user") && rootNode.has("text");
    }

    @Override
    public void process(final Context context) {
        Log.d(TAG, String.format("Tweet #%d - @%s", getId(), getUser().getScreenName()));
        ApiClientHelper.runAsynchronously(context, new SendTweetTask(this));
    }

}
