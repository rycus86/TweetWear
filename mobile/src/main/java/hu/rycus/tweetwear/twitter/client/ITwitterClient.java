package hu.rycus.tweetwear.twitter.client;

import android.content.Context;

import org.scribe.model.Token;

import hu.rycus.tweetwear.common.model.Tweet;
import hu.rycus.tweetwear.twitter.client.callbacks.AccessLevelCallback;
import hu.rycus.tweetwear.twitter.client.callbacks.AccessTokenCallback;
import hu.rycus.tweetwear.twitter.client.callbacks.UsernameCallback;

public interface ITwitterClient {

    void authorize(Context context);

    void processAccessToken(Context context, String oauthVerifier, AccessTokenCallback callback);

    void loadUsername(Context context, UsernameCallback callback);

    void checkAccessLevel(Context context, AccessLevelCallback callback);

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

    enum Uri {

        CALLBACK("tweetwear://oauth_callback"),
        TIMELINE("https://api.twitter.com/1.1/statuses/home_timeline.json"),
        RETWEET("https://api.twitter.com/1.1/statuses/retweet/%d.json"),
        FAVORITE("https://api.twitter.com/1.1/favorites/create.json"),
        SETTINGS("https://api.twitter.com/1.1/account/settings.json"),
        VERIFY_CREDENTIALS("https://api.twitter.com/1.1/account/verify_credentials.json");

        private final String uri;

        Uri(final String uri) {
            this.uri = uri;
        }

        public String get() {
            return uri;
        }

        public String format(final Object... args) {
            return String.format(uri, args);
        }

    }

    enum Parameter {

        ID("id"),
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

    enum AccessLevel {

        READ("read"),
        READ_WRITE("read-write"),
        READ_WRITE_ACCESS_DM("read-write-directmessages");

        public static String header() {
            return "x-access-level";
        }

        public static AccessLevel desired() {
            return READ_WRITE;
        }

        private String value;

        AccessLevel(final String value) {
            this.value = value;
        }

        public String get() {
            return value;
        }

        public boolean matches(final String value) {
            return this.value.equalsIgnoreCase(value);
        }

    }

}
