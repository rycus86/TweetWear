package hu.rycus.tweetwear.twitter.client;

import android.content.Context;

import org.scribe.model.Token;

import hu.rycus.rtweetwear.common.model.Tweet;
import hu.rycus.tweetwear.twitter.client.callbacks.AccessTokenCallback;
import hu.rycus.tweetwear.twitter.client.callbacks.UsernameCallback;

public interface ITwitterClient {

    void authorize(Context context);

    void processAccessToken(Context context, String oauthVerifier, AccessTokenCallback callback);

    void loadUsername(Context context, UsernameCallback callback);

    Tweet[] getTimeline(
            Token accessToken,
            Integer count,
            Long sinceId,
            Long maxId,
            Boolean trimUser,
            Boolean excludeReplies,
            Boolean contributorDetails,
            Boolean includeEntities);

    enum Uri {

        CALLBACK("tweetwear://oauth_callback"),
        TIMELINE("https://api.twitter.com/1.1/statuses/home_timeline.json"),
        SETTINGS("https://api.twitter.com/1.1/account/settings.json");

        private final String uri;

        Uri(final String uri) {
            this.uri = uri;
        }

        public String get() {
            return uri;
        }

    }

    enum Parameter {

        COUNT("count"),
        SINCE_ID("since_id"),
        MAX_ID("max_id"),
        TRIM_USER("trim_user"),
        EXCLUDE_REPLIES("exclude_replies"),
        CONTRIBUTOR_DETAILS("contributor_details"),
        INCLUDE_ENTITIES("include_entities");

        private final String key;

        Parameter(final String key) {
            this.key = key;
        }

        public String get() {
            return key;
        }

    }

}
