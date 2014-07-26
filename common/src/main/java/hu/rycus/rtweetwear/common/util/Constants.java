package hu.rycus.rtweetwear.common.util;

public interface Constants {

    String PACKAGE_NAME = "hu.rycus.tweetwear";

    String ACTION_START_ALARM = PACKAGE_NAME + ".StartAlarm";
    String ACTION_CANCEL_ALARM = PACKAGE_NAME + ".CancelAlarm";
    String ACTION_START_SYNC = PACKAGE_NAME + ".StartSync";
    String ACTION_CLEAR_EXISTING = PACKAGE_NAME + ".ClearExisting";

    String QUERY_PARAM_OAUTH_VERIFIER = "oauth_verifier";

    public enum DataPath {

        TWEETS("/tweets/%");

        private final String value;

        private DataPath(final String value) {
            this.value = value;
        }

        public String withId(final Object id) {
            return value.replace("%", id.toString());
        }

        public String pattern() {
            return value.replace("%", "[^/]+");
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
