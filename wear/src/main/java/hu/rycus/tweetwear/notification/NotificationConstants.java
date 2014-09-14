package hu.rycus.tweetwear.notification;

public interface NotificationConstants {

    enum Tag {

        TWEET("tweet/%"),
        SUMMARY("tweets/summary"),
        PROMOTION("promotion/%");

        private final String value;

        Tag(final String value) {
            this.value = value;
        }

        public String get() {
            return value;
        }

        public String withId(final Object id) {
            return value.replace("%", id.toString());
        }

    }

    enum Id {

        TWEET(1),
        SUMMARY(2),
        PROMOTION(3);

        private final int value;

        Id(final int value) {
            this.value = value;
        }

        public int get() {
            return value;
        }

    }

    enum Group {

        TWEETS("tweets");

        private final String id;

        Group(final String id) {
            this.id = id;
        }

        public String get() {
            return id;
        }

    }

}
