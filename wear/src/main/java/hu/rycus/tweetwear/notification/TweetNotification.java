package hu.rycus.tweetwear.notification;

import android.app.Notification;
import android.content.Context;
import android.support.v4.app.NotificationManagerCompat;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;

import hu.rycus.tweetwear.R;
import hu.rycus.tweetwear.common.model.Tweet;
import hu.rycus.tweetwear.common.model.entities.Entities;
import hu.rycus.tweetwear.common.model.entities.Media;
import hu.rycus.tweetwear.common.model.entities.Url;
import hu.rycus.tweetwear.common.util.TweetData;
import hu.rycus.tweetwear.notification.action.FavoriteAction;
import hu.rycus.tweetwear.notification.action.ShowImageAction;
import hu.rycus.tweetwear.notification.action.ReadItLaterAction;
import hu.rycus.tweetwear.notification.action.ReplyAction;
import hu.rycus.tweetwear.notification.action.RetweetAction;

public class TweetNotification {

    private static final String TAG = TweetNotification.class.getSimpleName();

    private TweetNotification() {}

    public static void send(final Context context, final Tweet tweet) {
        Log.d(TAG, String.format("Notifying for tweet: %s - %s",
                tweet.getUser().getScreenName(), tweet.getText()));

        final Notification notification = build(context, tweet);
        NotificationManagerCompat.from(context).notify(
                NotificationConstants.Tag.TWEET.withId(tweet.getId()),
                NotificationConstants.Id.TWEET.get(),
                notification);
    }

    private static Notification build(final Context context, final Tweet tweet) {
        final TweetData tweetData = TweetData.of(tweet);

        final Spanned contentHtml = getHtmlContent(
                tweetData.toFormattedHtml(), tweet.getUser().getName(), tweetData.getTimestamp());

        return new Notification.Builder(context)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(tweetData.getTitle())
                .setSortKey(getSortKey(tweet))
                .setStyle(new Notification.BigTextStyle().bigText(contentHtml))
                .setGroup(NotificationConstants.Group.TWEETS.get())
                .extend(createActionExtender(context, tweet))
                .build();
    }

    private static Spanned getHtmlContent(final String rawContent, final String username,
                                          final String timestamp) {
        final String content = String.format("<small>%s<br/>" +
                        "<b><i> &mdash; %s</i></b><br/>" +
                        "<i>&#x1f4c5; %s</i></small>",
                rawContent, username, timestamp);
        return Html.fromHtml(content);
    }

    private static String getSortKey(final Tweet tweet) {
        return Long.toString(Long.MAX_VALUE - tweet.getId());
    }

    private static Notification.WearableExtender createActionExtender(final Context context,
                                                                      final Tweet tweet) {

        final Notification.WearableExtender extender = new Notification.WearableExtender();

        addShowImageActions(context, tweet, extender);
        addReadLaterActions(context, tweet, extender);

        if (!tweet.isOwnTweet()) {
            extender.addAction(new RetweetAction().build(context, tweet));
        }

        extender.addAction(new FavoriteAction().build(context, tweet));
        extender.addAction(new ReplyAction().build(context, tweet));

        return extender;
    }

    private static void addShowImageActions(final Context context, final Tweet tweet,
                                            final Notification.WearableExtender extender) {
        final Entities entities = tweet.getEntities();
        if (entities != null) {
            final Media[] medias = entities.getMedia();
            if (medias != null) {
                for (final Media media : medias) {
                    final ShowImageAction action = new ShowImageAction(media);
                    extender.addAction(action.build(context, tweet));
                }
            }
        }
    }

    private static void addReadLaterActions(final Context context, final Tweet tweet,
                                            final Notification.WearableExtender extender) {
        final Entities entities = tweet.getEntities();
        if (entities != null) {
            final Url[] urls = entities.getUrls();
            if (urls != null && urls.length > 0) {
                extender.addAction(new ReadItLaterAction().build(context, tweet));
            }
        }
    }

}
