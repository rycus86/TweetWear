package hu.rycus.tweetwear.notification.action;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.text.Html;

import hu.rycus.tweetwear.R;
import hu.rycus.tweetwear.TweetWearService;
import hu.rycus.tweetwear.common.model.Tweet;
import hu.rycus.tweetwear.common.util.Constants;

public class FavoriteAction {

    private FavoriteAction() {}

    public static Notification.Action build(final Context context, final Tweet tweet) {
        final int icon = getIcon(context, tweet);
        final CharSequence title = getTitle(context, tweet);
        final PendingIntent pendingIntent = getPendingIntent(context, tweet);

        return new Notification.Action.Builder(icon, title, pendingIntent)
                .extend(new Notification.Action.WearableExtender()
                        .setAvailableOffline(false))
                .build();
    }

    private static PendingIntent getPendingIntent(final Context context, final Tweet tweet) {
        if (tweet.isFavorited()) {
            return null;
        } else {
            final Intent broadcastIntent = new Intent(context, TweetWearService.class)
                    .setAction(Constants.ACTION_SEND_FAVORITE)
                    .putExtra(Constants.EXTRA_TWEET_ID, tweet.getId());
            return PendingIntent.getService(
                    context, (int) tweet.getId(), broadcastIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        }
    }

    private static CharSequence getTitle(final Context context, final Tweet tweet) {
        if (tweet.isFavorited()) {
            return context.getString(R.string.favorited);
        } else {
            String tweetInformation = "@" + tweet.getUser().getScreenName() + ": " + tweet.getText();
            if (tweetInformation.length() > 25) {
                tweetInformation = tweetInformation.substring(0, 23).trim() + "...";
            }

            final String action = context.getString(R.string.favorite);
            return Html.fromHtml(action + "<br/><small>" + tweetInformation + "</small>");
        }
    }

    private static int getIcon(final Context context, final Tweet tweet) {
        return R.drawable.ic_action_favorite;
    }

}
