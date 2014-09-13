package hu.rycus.tweetwear.notification;

import android.app.Notification;
import android.content.Context;
import android.support.v4.app.NotificationManagerCompat;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;

import java.text.DateFormat;

import hu.rycus.tweetwear.R;
import hu.rycus.tweetwear.common.model.Tweet;
import hu.rycus.tweetwear.common.model.entities.Entities;
import hu.rycus.tweetwear.common.model.entities.Hashtag;
import hu.rycus.tweetwear.common.model.entities.Media;
import hu.rycus.tweetwear.common.model.entities.Url;
import hu.rycus.tweetwear.common.model.entities.UserMention;
import hu.rycus.tweetwear.common.util.TweetData;
import hu.rycus.tweetwear.notification.action.FavoriteAction;
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
    
    private static String processEntities(final Tweet tweet, final String originalContent) {
        final Entities entities = tweet.getEntities();

        String content = originalContent;
        
        if (entities.getHashtags() != null) {
            for (final Hashtag hashtag : entities.getHashtags()) {
                content = content.replace("#" + hashtag.getText(),
                        String.format("<i>#<b>%s</b></i>", hashtag.getText()));
            }
        }

        if (entities.getUrls() != null) {
            for (final Url url : entities.getUrls()) {
                content = content.replace(url.getUrl(),
                        String.format("<font color='#0099FF'><i>%s</i></font>",
                                url.getDisplayUrl()));
            }
        }

        if (entities.getMedia() != null) {
            for (final Media media : entities.getMedia()) {
                content = content.replace(media.getUrl(), "");
            }
        }

        if (entities.getUserMentions() != null) {
            for (final UserMention mention : entities.getUserMentions()) {
                content = content.replace(
                        String.format("@%s", mention.getScreenName()),
                        String.format("<font color='#0033CC'><i>@%s</i></font>",
                                mention.getName()));
            }
        }
        
        return content;
    }

    private static String getTitle(final Tweet tweet) {
        if (tweet.getRetweetedStatus() != null) {
            return "@" + tweet.getRetweetedStatus().getUser().getScreenName();
        } else {
            return "@" + tweet.getUser().getScreenName();
        }
    }

    private static String getTimestamp(final Tweet tweet) {
        final DateFormat dateFormat = DateFormat.getDateTimeInstance(
                DateFormat.SHORT, DateFormat.SHORT);
        return dateFormat.format(tweet.getCreatedAt());
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

        addReadLaterActions(context, tweet, extender);

        if (!tweet.isOwnTweet()) {
            extender.addAction(new RetweetAction().build(context, tweet));
        }

        extender.addAction(new FavoriteAction().build(context, tweet));
        extender.addAction(new ReplyAction().build(context, tweet));

        return extender;
    }

    private static void addReadLaterActions(final Context context, final Tweet tweet,
                                            final Notification.WearableExtender extender) {
        final Entities entities = tweet.getEntities();
        if (entities != null) {
            final Url[] urls = entities.getUrls();
            if (urls != null) {
                for (final Url url : urls) {
                    final ReadItLaterAction action = new ReadItLaterAction(url);
                    extender.addAction(action.build(context, tweet));
                }
            }
        }
    }

}
