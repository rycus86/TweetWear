package hu.rycus.tweetwear.common.payload;

import hu.rycus.tweetwear.common.util.Mapper;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class NotificationSettings {

    private boolean vibrate;

    public static NotificationSettings parse(final byte[] data) {
        return Mapper.readObject(data, NotificationSettings.class);
    }

    public static NotificationSettings getDefault() {
        final NotificationSettings settings = new NotificationSettings();
        settings.setVibrate(true);
        return settings;
    }

}
