package hu.rycus.tweetwear.common.util;

import android.graphics.Color;

public interface Constants {

    String PACKAGE_NAME = "hu.rycus.tweetwear";

    String ACTION_START_ALARM = PACKAGE_NAME + ".StartAlarm";
    String ACTION_CANCEL_ALARM = PACKAGE_NAME + ".CancelAlarm";
    String ACTION_START_SYNC = PACKAGE_NAME + ".StartSync";
    String ACTION_CLEAR_EXISTING = PACKAGE_NAME + ".ClearExisting";
    String ACTION_SEND_RETWEET = PACKAGE_NAME + ".SendRetweet";
    String ACTION_SEND_FAVORITE = PACKAGE_NAME + ".SendFavorite";
    String ACTION_CAPTURE_REPLY = PACKAGE_NAME + ".CaptureReply";
    String ACTION_MARK_AS_READ = PACKAGE_NAME + ".MarkAsRead";
    String ACTION_READ_IT_LATER = PACKAGE_NAME + ".ReadItLater";
    String ACTION_DO_NOTHING = PACKAGE_NAME + ".DoNothing";

    String ACTION_BROADCAST_READ_IT_LATER = PACKAGE_NAME + ".Broadcast.ReadItLater";

    String EXTRA_TWEET_ID = "__tweet_id";
    String EXTRA_TWEET_JSON = "__tweet_json";
    String EXTRA_REPLY_TO_NAME = "__reply_to_name";
    String EXTRA_SHOW_MEDIA_ID = "__show_media_id";

    String QUERY_PARAM_OAUTH_VERIFIER = "oauth_verifier";

    int COLOR_TWITTER_BACKGROUND = Color.parseColor("#55ACEE");

    public enum Action {

        SHOW_IMAGE;

        public String get() {
            return name();
        }

        public String withId(final Object id) {
            return String.format("%s.%s/%s", PACKAGE_NAME, name(), id);
        }

        public boolean matches(final String action) {
            final String pattern = String.format("%s\\.%s(/.+)?", PACKAGE_NAME, name());
            return action.matches(pattern);
        }

    }

    public enum DataPath {

        SYNC_COMPLETE("/tweetwear/sync/complete"),
        TWEETS("/tweetwear/tweets/%"),
        RETWEET("/tweetwear/retweet/%"),
        FAVORITE("/tweetwear/favorite/%"),
        POST_NEW_TWEET("/tweetwear/post/new/tweet"),
        POST_REPLY("/tweetwear/post/reply/%"),
        MARK_AS_READ("/tweetwear/mark/as/read"),
        READ_IT_LATER("/tweetwear/read/it/later/%"),
        SHOW_IMAGE("/tweetwear/show/image/%"),
        PROMOTION("/tweetwear/promotion/%"),
        RESULT_RETWEET_SUCCESS("/tweetwear/retweet/%/success"),
        RESULT_RETWEET_FAILURE("/tweetwear/retweet/%/failure"),
        RESULT_FAVORITE_SUCCESS("/tweetwear/favorite/%/success"),
        RESULT_FAVORITE_FAILURE("/tweetwear/favorite/%/failure"),
        RESULT_POST_NEW_TWEET_SUCCESS("/tweetwear/post/tweet/%/success"),
        RESULT_POST_REPLY_SUCCESS("/tweetwear/post/reply/%/success"),
        RESULT_POST_NEW_TWEET_FAILURE("/tweetwear/post/tweet/failure"),
        RESULT_POST_REPLY_FAILURE("/tweetwear/post/reply/failure"),
        RESULT_READ_IT_LATER_SUCCESS("/tweetwear/read/it/later/%/success"),
        RESULT_READ_IT_LATER_FAILURE("/tweetwear/read/it/later/%/failure"),
        RESULT_SHOW_IMAGE_SUCCESS("/tweetwear/show/image/%/success"),
        RESULT_SHOW_IMAGE_FAILURE("/tweetwear/show/image/%/failure");

        private final String value;

        private DataPath(final String value) {
            this.value = value;
        }

        public String get() {
            return value;
        }

        public String withId(final Object id) {
            return value.replace("%", id.toString());
        }

        public String pattern() {
            return value.replace("%", "([^/]+)");
        }

        public boolean matches(final String value) {
            return value != null && value.matches(pattern());
        }

        public String replace(final String value, final String replacement) {
            return value.replaceFirst(pattern(), replacement);
        }

    }

    public enum DataKey {

        CONTENT("content");

        private final String value;

        private DataKey(final String value) {
            this.value = value;
        }

        public String get() {
            return value;
        }

    }

}
