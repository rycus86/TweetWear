package hu.rycus.tweetwear.twitter.stream.events;

import android.content.Context;
import android.util.Log;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;

import hu.rycus.tweetwear.common.api.ApiClientHelper;
import hu.rycus.tweetwear.common.model.Tweet;
import hu.rycus.tweetwear.common.model.User;
import hu.rycus.tweetwear.tasks.SendTweetTask;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class UnfavoriteEvent extends GenericEvent<User, User, Tweet> {

    private static final String TAG = UnfavoriteEvent.class.getSimpleName();

    private static final String EVENT_NAME = "unfavorite";

    public static boolean matches(final JsonNode rootNode) {
        return GenericEvent.matches(rootNode) &&
                EVENT_NAME.equals(rootNode.get(EVENT_NODE).asText());
    }

    @Override
    public void process(final Context context) {
        final String message = String.format("User @%s unfavorited @%s's tweet #%d - %s",
                getSource().getScreenName(),
                getTarget().getScreenName(),
                getTargetObject().getId(),
                getTargetObject().getText());
        Log.d(TAG, message);

        final Tweet tweet = getTargetObject();
        tweet.setFavorited(false);
        ApiClientHelper.runAsynchronously(context, new SendTweetTask(tweet));
    }

}
