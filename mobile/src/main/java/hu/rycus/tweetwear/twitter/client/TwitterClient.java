package hu.rycus.tweetwear.twitter.client;

import android.content.Context;
import android.util.Log;

import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.TwitterApi;
import org.scribe.model.Token;
import org.scribe.oauth.OAuthService;

import hu.rycus.tweetwear.BuildConfig;
import hu.rycus.tweetwear.common.model.Lists;
import hu.rycus.tweetwear.common.model.Tweet;
import hu.rycus.tweetwear.common.model.User;
import hu.rycus.tweetwear.preferences.Preferences;
import hu.rycus.tweetwear.twitter.TwitterFactory;
import hu.rycus.tweetwear.twitter.async.AuthorizationTask;
import hu.rycus.tweetwear.twitter.async.CheckAccessLevelTask;
import hu.rycus.tweetwear.twitter.async.ProcessAccessTokenTask;
import hu.rycus.tweetwear.twitter.async.UserInformationLoaderTask;
import hu.rycus.tweetwear.twitter.client.callbacks.AccessLevelCallback;
import hu.rycus.tweetwear.twitter.client.callbacks.AccessTokenCallback;
import hu.rycus.tweetwear.twitter.client.callbacks.UsernameCallback;

public class TwitterClient implements ITwitterClient {

    private static final String TAG = "TwitterClient";

    private static final OAuthService service = createOAuthService();

    @Override
    public void authorize(final Context context) {
        final AuthorizationTask task = new AuthorizationTask(context, service);
        task.execute();
    }

    @Override
    public void processAccessToken(final Context context, final String oauthVerifier,
                                   final AccessTokenCallback callback) {
        final ProcessAccessTokenTask task = new ProcessAccessTokenTask(
                context, service, callback);
        task.execute(oauthVerifier);
    }

    @Override
    public void loadUsername(final Context context, final UsernameCallback callback) {
        final Token accessToken = Preferences.getUserToken(context);
        final UserInformationLoaderTask task = new UserInformationLoaderTask(context, service, callback);
        task.execute(accessToken);
    }

    @Override
    public void checkAccessLevel(final Context context, final AccessLevelCallback callback) {
        final Token accessToken = Preferences.getUserToken(context);
        final CheckAccessLevelTask task = new CheckAccessLevelTask(context, service, callback);
        task.execute(accessToken);
    }

    @Override
    public User getUser(final Token accessToken) {
        try {
            if (accessToken == null) {
                throw new IllegalArgumentException("No access token present");
            }

            return RequestBuilder
                    .get(ITwitterClient.Uri.VERIFY_CREDENTIALS.get())
                    .send(service, accessToken)
                    .respond(User.class);
        } catch (Exception ex) {
            Log.e(TAG, "Failed to load user information");
        }

        return null;
    }

    @Override
    public Tweet[] getTimeline(
            final Token accessToken, final Integer count, final Long sinceId, final Long maxId,
            final Boolean trimUser, final Boolean excludeReplies, final Boolean contributorDetails,
            final Boolean includeEntities) {

        try {
            if (accessToken == null) {
                throw new IllegalArgumentException("No access token present");
            }

            final Tweet[] timelineTweets = RequestBuilder.get(Uri.TIMELINE.get())
                    .queryParam(Parameter.COUNT.get(), count)
                    .queryParam(Parameter.SINCE_ID.get(), sinceId)
                    .queryParam(Parameter.MAX_ID.get(), maxId)
                    .queryParam(Parameter.TRIM_USER.get(), trimUser)
                    .queryParam(Parameter.EXCLUDE_REPLIES.get(), excludeReplies)
                    .queryParam(Parameter.CONTRIBUTOR_DETAILS.get(), contributorDetails)
                    .queryParam(Parameter.INCLUDE_ENTITIES.get(), includeEntities)
                    .send(service, accessToken)
                    .respond(Tweet[].class);

            if (timelineTweets != null) {
                return timelineTweets;
            }
        } catch (Exception ex) {
            Log.e(TAG, "Failed to retrieve tweets for timeline", ex);
        }

        return new Tweet[0];
    }

    @Override
    public Tweet retweet(final Token accessToken, final long id, final Boolean trimUser) {
        try {
            if (accessToken == null) {
                throw new IllegalArgumentException("No access token present");
            }

            return RequestBuilder.post(Uri.RETWEET.format(id))
                    .bodyParam(Parameter.TRIM_USER.get(), trimUser)
                    .send(service, accessToken)
                    .respond(Tweet.class);
        } catch (Exception ex) {
            Log.e(TAG, "Failed to execute retweet", ex);
        }

        return null;
    }

    @Override
    public Tweet favorite(final Token accessToken, final long id, final Boolean includeEntities) {
        try {
            if (accessToken == null) {
                throw new IllegalArgumentException("No access token present");
            }

            final Tweet tweet = RequestBuilder.post(Uri.FAVORITE.get())
                    .bodyParam(Parameter.ID.get(), id)
                    .bodyParam(Parameter.INCLUDE_ENTITIES.get(), includeEntities)
                    .send(service, accessToken)
                    .respond(Tweet.class);

            // according to Twitter docs
            // the response may not immediately reflect the favorited status
            if (!tweet.isFavorited()) {
                Log.d(TAG, "Manually marking tweet as favorite");
                tweet.setFavorited(true);
            }

            return tweet;
        } catch (Exception ex) {
            Log.e(TAG, "Failed to favorite a tweet", ex);
        }

        return null;
    }

    @Override
    public Tweet postStatus(final Token accessToken, final String content,
                            final Long inReplyToStatusId) {
        try {
            if (accessToken == null) {
                throw new IllegalArgumentException("No access token present");
            }

            return RequestBuilder.post(Uri.UPDATE_STATUS.get())
                    .bodyParam(Parameter.STATUS.get(), content)
                    .bodyParam(Parameter.IN_REPLY_TO_STATUS_ID.get(), inReplyToStatusId)
                    .send(service, accessToken)
                    .respond(Tweet.class);
        } catch (Exception ex) {
            Log.e(TAG, "Failed to post new tweet", ex);
        }

        return null;
    }

    @Override
    public Lists getLists(final Context context, final ListType listType,
                          final Long cursor, final Integer count) {
        switch (listType) {
            case SUBSCRIPTIONS:
                return getLists(context, Uri.LIST_SUBSCRIPTIONS, cursor, count);
            case OWNERSHIPS:
                return getLists(context, Uri.LIST_OWNERSHIPS, cursor, count);
            default:
                Log.wtf(TAG, "Unexpected list type: " + listType);
                return null;
        }
    }

    private Lists getLists(final Context context, final Uri uri,
                           final Long cursor, final Integer count) {
        final Token accessToken = Preferences.getUserToken(context);
        try {
            if (accessToken == null) {
                throw new IllegalArgumentException("No access token present");
            }

            final long userId = TwitterFactory.getUserId(context, this, accessToken);
            if (userId < 0) {
                throw new IllegalArgumentException("No user ID found");
            }

            return RequestBuilder.get(uri.get())
                    .queryParam(Parameter.USER_ID.get(), userId)
                    .queryParam(Parameter.CURSOR.get(), cursor)
                    .queryParam(Parameter.COUNT.get(), count)
                    .send(service, accessToken)
                    .respond(Lists.class);
        } catch (Exception ex) {
            Log.e(TAG, "Failed to get lists", ex);
        }

        return null;
    }

    @Override
    public Tweet[] getListStatuses(
            final Token accessToken,
            final long listId, final Integer count, final Long sinceId, final Long maxId,
            final Boolean includeEntities, final Boolean includeRTs) {
        try {
            if (accessToken == null) {
                throw new IllegalArgumentException("No access token present");
            }

            final Tweet[] listTweets = RequestBuilder.get(Uri.LIST_STATUSES.get())
                    .queryParam(Parameter.LIST_ID.get(), listId)
                    .queryParam(Parameter.COUNT.get(), count)
                    .queryParam(Parameter.SINCE_ID.get(), sinceId)
                    .queryParam(Parameter.MAX_ID.get(), maxId)
                    .queryParam(Parameter.INCLUDE_ENTITIES.get(), includeEntities)
                    .queryParam(Parameter.INCLUDE_RTS.get(), includeRTs)
                    .send(service, accessToken)
                    .respond(Tweet[].class);

            if (listTweets != null) {
                return listTweets;
            }
        } catch (Exception ex) {
            Log.e(TAG, String.format("Failed to retrieve tweets for list #%d", listId), ex);
        }

        return new Tweet[0];
    }

    private static OAuthService createOAuthService() {
        return new ServiceBuilder()
                .provider(TwitterApi.SSL.class)
                .apiKey(BuildConfig.API_KEY)
                .apiSecret(BuildConfig.API_SECRET)
                .callback(Uri.CALLBACK.get())
                .build();
    }

}
