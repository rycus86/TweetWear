package hu.rycus.tweetwear.notification.action;

import android.app.PendingIntent;
import android.content.Context;

import hu.rycus.tweetwear.R;
import hu.rycus.tweetwear.common.model.Tweet;
import hu.rycus.tweetwear.common.util.Constants;

public class FavoriteAction extends BaseTweetAction {

    @Override
    protected int getIcon(final Context context, final Tweet tweet) {
        return R.drawable.ic_action_favorite;
    }

    @Override
    protected CharSequence getTitle(final Context context, final Tweet tweet) {
        if (tweet.isFavorited()) {
            return context.getString(R.string.favorited);
        } else {
            return super.getTitle(context, tweet);
        }
    }

    @Override
    protected int getTitleActionResource(final Context context, final Tweet tweet) {
        return R.string.favorite;
    }

    @Override
    protected PendingIntent getPendingIntent(final Context context, final Tweet tweet) {
        final String action;
        if (tweet.isFavorited()) {
            action = Constants.ACTION_DO_NOTHING;
        } else {
            action = Constants.ACTION_SEND_FAVORITE;
        }
        return getPendingIntentForService(context, tweet, action);
    }

}
