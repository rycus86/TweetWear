package hu.rycus.tweetwear.notification;

import android.app.Notification;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.text.Html;
import android.text.Spanned;

import java.text.DateFormat;

import hu.rycus.tweetwear.common.model.Tweet;
import hu.rycus.tweetwear.common.model.entities.Hashtag;
import hu.rycus.tweetwear.common.model.entities.Media;
import hu.rycus.tweetwear.common.model.entities.Url;
import hu.rycus.tweetwear.common.model.entities.UserMention;
import hu.rycus.tweetwear.R;

public class NotificationHandler {

    private static final String GROUP_KEY_TWEETS = "tweets";

    private NotificationHandler() { } // Singleton

    public static Notification buildTweetNotification(final Context context, final Tweet tweet) {
        String content = tweet.getText();
        if (tweet.getEntities() != null) {
            if (tweet.getEntities().getHashtags() != null) {
                for (final Hashtag hashtag : tweet.getEntities().getHashtags()) {
                    content = content.replace("#" + hashtag.getText(),
                            String.format("<i>#<b>%s</b></i>", hashtag.getText()));
                }
            }

            if (tweet.getEntities().getUrls() != null) {
                for (final Url url : tweet.getEntities().getUrls()) {
                    content = content.replace(url.getUrl(),
                            String.format("<font color='#0099FF'><i>%s</i></font>",
                                    url.getDisplayUrl()));
                }
            }

            if (tweet.getEntities().getMedia() != null) {
                for (final Media media : tweet.getEntities().getMedia()) {
                    content = content.replace(media.getUrl(), "");
                }
            }

            if (tweet.getEntities().getUserMentions() != null) {
                for (final UserMention mention : tweet.getEntities().getUserMentions()) {
                    content = content.replace(
                            String.format("@%s", mention.getScreenName()),
                            String.format("<font color='#0033CC'><i>@%s</i></font>",
                                    mention.getName()));
                }
            }
        }

        final String title;
        if (tweet.getRetweetedStatus() != null) {
            title = "@" + tweet.getRetweetedStatus().getUser().getScreenName();
        } else {
            title = "@" + tweet.getUser().getScreenName();
        }

        final DateFormat dateFormat = DateFormat.getDateTimeInstance(
                DateFormat.SHORT, DateFormat.SHORT);
        final String timestamp = dateFormat.format(tweet.getCreatedAt());

        content = String.format("<small>%s<br/>" +
                "<b><i> &mdash; %s</i></b><br/>" +
                "<i>&#x1f4c5; %s</i></small>",
                content, tweet.getUser().getName(), timestamp);

        final Spanned contentHtml = Html.fromHtml(content);

        return new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(title)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(contentHtml))
                .setGroup(GROUP_KEY_TWEETS)
                .build();
    }

    public static Notification buildSummaryNotification(final Context context, final int count) {
        final String title = context.getResources()
                .getQuantityString(R.plurals.summary_notification, count, count);
        return new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(title)
                .setGroup(GROUP_KEY_TWEETS)
                .setGroupSummary(true)
                .setDefaults(NotificationCompat.DEFAULT_VIBRATE)
                .build();
    }

}
