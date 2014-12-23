package hu.rycus.tweetwear.twitter.client;

import android.content.Context;

import org.scribe.model.Token;

import hu.rycus.tweetwear.common.model.Lists;
import hu.rycus.tweetwear.common.model.Tweet;
import hu.rycus.tweetwear.common.model.User;
import hu.rycus.tweetwear.twitter.TwitterConstants;
import hu.rycus.tweetwear.twitter.client.callbacks.AccessLevelCallback;
import hu.rycus.tweetwear.twitter.client.callbacks.AccessTokenCallback;
import hu.rycus.tweetwear.twitter.client.callbacks.UsernameCallback;

public interface ITwitterClient extends TwitterConstants {

    void authorize(Context context);

    void processAccessToken(Context context, String oauthVerifier, AccessTokenCallback callback);

    void loadUsername(Context context, UsernameCallback callback);

    void checkAccessLevel(Context context, AccessLevelCallback callback);

    User getUser(Token accessToken);

    Tweet[] getTimeline(
            Token accessToken,
            Integer count,
            Long sinceId,
            Long maxId,
            Boolean trimUser,
            Boolean excludeReplies,
            Boolean contributorDetails,
            Boolean includeEntities);

    Tweet retweet(Token accessToken, long id, Boolean trimUser);

    Tweet favorite(Token accessToken, long id, Boolean includeEntities);

    Tweet postStatus(Token accessToken, String content, Long inReplyToStatusId);

    Lists getLists(Context context, ListType listType, Long cursor, Integer count);

    Tweet[] getListStatuses(Token accessToken,
                            long listId, Integer count, Long sinceId, Long maxId,
                            Boolean includeEntities, Boolean includeRTs);

}
