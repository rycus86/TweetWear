package hu.rycus.tweetwear.notification.action;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import hu.rycus.tweetwear.R;
import hu.rycus.tweetwear.common.model.Tweet;
import hu.rycus.tweetwear.common.util.Constants;

public class ReplyAction extends BaseTweetAction {

    @Override
    protected int getIcon(final Context context, final Tweet tweet) {
        return R.drawable.ic_action_reply;
    }

    @Override
    protected int getTitleActionResource(final Context context, final Tweet tweet) {
        return R.string.reply_to;
    }

    @Override
    protected PendingIntent getPendingIntent(final Context context, final Tweet tweet) {
        return getPendingIntentForPostActivity(context, tweet, Constants.ACTION_CAPTURE_REPLY);
    }

    @Override
    protected void putExtrasIntoIntent(final Context context, final Tweet tweet,
                                       final Intent startIntent) {
        super.putExtrasIntoIntent(context, tweet, startIntent);
        startIntent.putExtra(Constants.EXTRA_REPLY_TO_NAME, tweet.getUser().getScreenName());
    }

}
