package hu.rycus.tweetwear.notification;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import hu.rycus.tweetwear.R;
import hu.rycus.tweetwear.TweetWearService;
import hu.rycus.tweetwear.common.payload.NotificationSettings;
import hu.rycus.tweetwear.common.util.Constants;

public class SummaryNotification {

    private SummaryNotification() {}

    public static void send(final Context context, final int count,
                            final NotificationSettings settings) {
        final Notification notification = build(context, count, settings);
        NotificationManagerCompat.from(context).notify(
                NotificationConstants.Tag.SUMMARY.get(),
                NotificationConstants.Id.SUMMARY.get(),
                notification);
    }

    private static Notification build(final Context context, final int count,
                                      final NotificationSettings settings) {
        final String title = context.getResources()
                .getQuantityString(R.plurals.summary_notification, count, count);
        final Bitmap background = createBackground();
        final NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle(title)
                        .setGroup(NotificationConstants.Group.TWEETS.get())
                        .setGroupSummary(true)
                        .extend(new NotificationCompat.WearableExtender().setBackground(background))
                        .setDeleteIntent(createDeleteIntent(context));

        if (settings.isVibrate()) {
            builder.setDefaults(NotificationCompat.DEFAULT_VIBRATE);
        }

        return builder.build();
    }

    private static Bitmap createBackground() {
        final Bitmap background = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        background.setPixel(0, 0, Constants.COLOR_TWITTER_BACKGROUND);
        return background;
    }

    private static PendingIntent createDeleteIntent(final Context context) {
        final Intent intent = new Intent(context, TweetWearService.class);
        intent.setAction(Constants.ACTION_MARK_AS_READ);
        return PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

}
