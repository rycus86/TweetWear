package hu.rycus.tweetwear.notification.action;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.text.Html;

import hu.rycus.tweetwear.R;
import hu.rycus.tweetwear.common.model.Tweet;
import hu.rycus.tweetwear.common.model.entities.Url;
import hu.rycus.tweetwear.common.util.Constants;
import hu.rycus.tweetwear.common.util.TweetData;

public class ReadItLaterAction extends BaseTweetAction {

    @Override
    protected int getIcon(final Context context, final Tweet tweet) {
        return R.drawable.ic_web_site;
    }

    @Override
    protected CharSequence getTitle(final Context context, final Tweet tweet) {
        final String action;
        if (tweet.isSavedToReadLater()) {
            action = context.getString(R.string.read_later_saved);
        } else {
            action = context.getString(R.string.read_later);
        }

        final Url[] urls = tweet.getEntities().getUrls();
        if (urls.length == 1) {
            final String link = urls[0].getDisplayUrl();
            return Html.fromHtml(String.format("%s<br/><small>%s</small>", action, link));
        } else {
            return getTitleWithActionAndTweetInformation(context, tweet, action);
        }
    }

    @Override
    protected PendingIntent getPendingIntent(final Context context, final Tweet tweet) {
        final String action;
        if (tweet.isSavedToReadLater()) {
            action = Constants.ACTION_DO_NOTHING;
        } else {
            action = Constants.ACTION_READ_IT_LATER;
        }
        return getPendingIntentForService(context, tweet, action);
    }

    @Override
    protected void putExtrasIntoIntent(
            final Context context, final Tweet tweet, final Intent startIntent) {
        super.putExtrasIntoIntent(context, tweet, startIntent);
        startIntent.putExtra(Constants.EXTRA_TWEET_JSON, TweetData.of(tweet).toJson());
    }
}
