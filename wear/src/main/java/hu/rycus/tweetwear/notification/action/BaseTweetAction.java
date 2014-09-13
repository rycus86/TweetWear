package hu.rycus.tweetwear.notification.action;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.text.Html;

import hu.rycus.tweetwear.PostTweetActivity;
import hu.rycus.tweetwear.TweetWearService;
import hu.rycus.tweetwear.common.model.Tweet;
import hu.rycus.tweetwear.common.util.Constants;

abstract class BaseTweetAction {

    public Notification.Action build(final Context context, final Tweet tweet) {
        final int icon = getIcon(context, tweet);
        final CharSequence title = getTitle(context, tweet);
        final PendingIntent pendingIntent = getPendingIntent(context, tweet);

        return new Notification.Action.Builder(icon, title, pendingIntent)
                .extend(new Notification.Action.WearableExtender()
                        .setAvailableOffline(false))
                .build();
    }

    protected abstract int getIcon(final Context context, final Tweet tweet);

    protected CharSequence getTitle(final Context context, final Tweet tweet) {
        final String action = context.getString(getTitleActionResource(context, tweet));
        return getTitleWithActionAndTweetInformation(context, tweet, action);
    }

    protected int getTitleActionResource(final Context context, final Tweet tweet) {
        return 0;
    }

    protected CharSequence getTitleWithActionAndTweetInformation(
            final Context context, final Tweet tweet, final String action) {
        String tweetInformation = String.format(
                "@%s: %s", tweet.getUser().getScreenName(), tweet.getText());
        if (tweetInformation.length() > 25) {
            tweetInformation = tweetInformation.substring(0, 23).trim() + "...";
        }

        return Html.fromHtml(String.format(
                "%s<br/><small>%s</small>", action, tweetInformation));
    }

    protected abstract PendingIntent getPendingIntent(final Context context, final Tweet tweet);

    protected PendingIntent getPendingIntentForService(final Context context, final Tweet tweet,
                                                       final String action) {
        final Intent serviceIntent = new Intent(context, TweetWearService.class)
                .setAction(action);
        putExtrasIntoIntent(context, tweet, serviceIntent);
        return PendingIntent.getService(
                context, (int) tweet.getId(), serviceIntent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    protected PendingIntent getPendingIntentForPostActivity(
            final Context context, final Tweet tweet, final String action) {
        final Intent activity = new Intent(context, PostTweetActivity.class).setAction(action);
        putExtrasIntoIntent(context, tweet, activity);
        return PendingIntent.getActivity(
                context, (int) tweet.getId(), activity, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    protected void putExtrasIntoIntent(final Context context, final Tweet tweet,
                                       final Intent startIntent) {
            startIntent.putExtra(Constants.EXTRA_TWEET_ID, tweet.getId());
    }

}
