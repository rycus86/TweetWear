package hu.rycus.tweetwear.notification;

import android.app.Notification;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import hu.rycus.tweetwear.R;

public class SummaryNotification {

    private SummaryNotification() {}

    public static void send(final Context context, final int count) {
        final Notification notification = build(context, count);
        NotificationManagerCompat.from(context).notify(
                NotificationConstants.Tag.SUMMARY.get(),
                NotificationConstants.Id.SUMMARY.get(),
                notification);
    }

    private static Notification build(final Context context, final int count) {
        final String title = context.getResources()
                .getQuantityString(R.plurals.summary_notification, count, count);
        return new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(title)
                .setGroup(NotificationConstants.Group.TWEETS.get())
                .setGroupSummary(true)
                .setDefaults(NotificationCompat.DEFAULT_VIBRATE)
                .build();
    }

}
