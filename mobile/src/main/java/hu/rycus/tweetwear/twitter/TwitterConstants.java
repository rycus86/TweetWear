package hu.rycus.tweetwear.twitter;

import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.TwitterApi;
import org.scribe.oauth.OAuthService;

import hu.rycus.tweetwear.BuildConfig;

public interface TwitterConstants {

    enum Uri {

        CALLBACK("tweetwear://oauth_callback"),
        TIMELINE("https://api.twitter.com/1.1/statuses/home_timeline.json"),
        RETWEET("https://api.twitter.com/1.1/statuses/retweet/%d.json"),
        FAVORITE("https://api.twitter.com/1.1/favorites/create.json"),
        UPDATE_STATUS("https://api.twitter.com/1.1/statuses/update.json"),
        VERIFY_CREDENTIALS("https://api.twitter.com/1.1/account/verify_credentials.json"),
        LIST_STATUSES("https://api.twitter.com/1.1/lists/statuses.json"),
        LIST_SUBSCRIPTIONS("https://api.twitter.com/1.1/lists/subscriptions.json"),
        LIST_OWNERSHIPS("https://api.twitter.com/1.1/lists/ownerships.json"),
        GET_LIST_MEMBERS("https://api.twitter.com/1.1/lists/members.json"),
        STREAM_TIMELINE("https://userstream.twitter.com/1.1/user.json"),
        STREAM_FILTER("https://stream.twitter.com/1.1/statuses/filter.json"),
        STREAM_SAMPLE("https://stream.twitter.com/1.1/statuses/sample.json");

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
        INCLUDE_ENTITIES("include_entities"),
        STATUS("status"),
        IN_REPLY_TO_STATUS_ID("in_reply_to_status_id"),
        POSSIBLY_SENSITIVE("possibly_sensitive"),
        LATITUDE("lat"),
        LONGITUDE("long"),
        PLACE_ID("place_id"),
        DISPLAY_COORDINATES("display_coordinates"),
        USER_ID("user_id"),
        SCREEN_NAME("screen_name"),
        CURSOR("cursor"),
        LIST_ID("list_id"),
        INCLUDE_RTS("include_rts"),
        DELIMITED("delimited"),
        STALL_WARNINGS("stall_warnings"),
        FILTER_LEVEL("filter_level"),
        LANGUAGE("language"),
        FOLLOW("follow"),
        TRACK("track"),
        LOCATIONS("locations"),
        WITH("with"),
        REPLIES("replies"),
        STRINGIFY_FRIEND_ID("stringify_friend_id");

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

    enum ListType {

        SUBSCRIPTIONS, OWNERSHIPS

    }

    enum OAuthServiceProvider {

        INSTANCE;

        private final OAuthService service = createOAuthService();

        public OAuthService get() {
            return service;
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

}
