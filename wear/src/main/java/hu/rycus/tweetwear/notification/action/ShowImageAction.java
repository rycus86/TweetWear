package hu.rycus.tweetwear.notification.action;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.text.Html;

import hu.rycus.tweetwear.R;
import hu.rycus.tweetwear.common.model.Tweet;
import hu.rycus.tweetwear.common.model.entities.Media;
import hu.rycus.tweetwear.common.util.Constants;
import hu.rycus.tweetwear.common.util.TweetData;

public class ShowImageAction extends BaseTweetAction {

    private final Media media;

    public ShowImageAction(final Media media) {
        this.media = media;
    }

    @Override
    protected int getIcon(final Context context, final Tweet tweet) {
        return R.drawable.ic_picture;
    }

    @Override
    protected CharSequence getTitle(final Context context, final Tweet tweet) {
        final String title = context.getString(R.string.show_image);
        return Html.fromHtml(String.format(
                "%s<br/><small>%s</small>", title, media.getDisplayUrl()));
    }

    @Override
    protected PendingIntent getPendingIntent(final Context context, final Tweet tweet) {
        return getPendingIntentForService(context, tweet, Constants.ACTION_SHOW_IMAGE);
    }

    @Override
    protected void putExtrasIntoIntent(final Context context, final Tweet tweet,
                                       final Intent startIntent) {
        super.putExtrasIntoIntent(context, tweet, startIntent);
        startIntent.putExtra(Constants.EXTRA_TWEET_JSON, TweetData.of(tweet).toJson());
        startIntent.putExtra(Constants.EXTRA_SHOW_MEDIA_ID, media.getId());
    }

}
