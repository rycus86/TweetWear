package hu.rycus.tweetwear.twitter.client;

import android.content.Context;
import android.util.Log;

import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.TwitterApi;
import org.scribe.model.Token;
import org.scribe.oauth.OAuthService;

import hu.rycus.tweetwear.common.model.Tweet;
import hu.rycus.tweetwear.BuildConfig;
import hu.rycus.tweetwear.preferences.Preferences;
import hu.rycus.tweetwear.twitter.async.AuthorizationTask;
import hu.rycus.tweetwear.twitter.async.ProcessAccessTokenTask;
import hu.rycus.tweetwear.twitter.async.UsernameLoaderTask;
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
        final UsernameLoaderTask task = new UsernameLoaderTask(context, service, callback);
        task.execute(accessToken);
    }

    @Override
    public Tweet[] getTimeline(
            final Token accessToken, final Integer count, final Long sinceId, final Long maxId,
            final Boolean trimUser, final Boolean excludeReplies, final Boolean contributorDetails,
            final Boolean includeEntities) {

        try {
            return RequestBuilder.start(Uri.TIMELINE.get())
                    .param(Parameter.COUNT.get(), count)
                    .param(Parameter.SINCE_ID.get(), sinceId)
                    .param(Parameter.MAX_ID.get(), maxId)
                    .param(Parameter.TRIM_USER.get(), trimUser)
                    .param(Parameter.EXCLUDE_REPLIES.get(), excludeReplies)
                    .param(Parameter.CONTRIBUTOR_DETAILS.get(), contributorDetails)
                    .param(Parameter.INCLUDE_ENTITIES.get(), includeEntities)
                    .get(service, accessToken)
                    .respond(Tweet[].class);
        } catch (Exception ex) {
            Log.e(TAG, "Failed to retrieve tweets for timeline", ex);
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
