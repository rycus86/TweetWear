package hu.rycus.tweetwear.common.payload;

import hu.rycus.tweetwear.common.model.Tweet;
import hu.rycus.tweetwear.common.util.Mapper;
import lombok.Getter;
import lombok.ToString;


@Getter
@ToString
public class TweetWithNotificationSettings {

    private Tweet tweet;
    private NotificationSettings settings;

    private TweetWithNotificationSettings() {
        // for JSON deserialization
    }

    public TweetWithNotificationSettings(final Tweet tweet, final NotificationSettings settings) {
        this.tweet = tweet;
        this.settings = settings;
    }

    public byte[] serialize() {
        return Mapper.writeObject(this);
    }

    public static TweetWithNotificationSettings parse(final byte[] data) {
        return Mapper.readObject(data, TweetWithNotificationSettings.class);
    }

}
