package hu.rycus.tweetwear.notification;

import android.app.Notification;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import hu.rycus.tweetwear.R;

public class PromotionNotification {

    private PromotionNotification() {}

    public static void send(final Context context,
                            final String promotionId, final String promotionText) {
        final Notification notification = build(context, promotionText);
        NotificationManagerCompat.from(context).notify(
                NotificationConstants.Tag.PROMOTION.withId(promotionId),
                NotificationConstants.Id.PROMOTION.get(),
                notification);
    }

    private static Notification build(final Context context, final String promotionText) {
        final String title = context.getString(R.string.promo_title);
        return new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(title)
                .setContentText(promotionText)
                .setDefaults(NotificationCompat.DEFAULT_VIBRATE)
                .build();
    }

}
