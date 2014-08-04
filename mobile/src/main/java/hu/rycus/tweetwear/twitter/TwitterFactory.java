package hu.rycus.tweetwear.twitter;

import android.content.Context;

import org.scribe.model.Token;

import hu.rycus.tweetwear.common.model.User;
import hu.rycus.tweetwear.preferences.Preferences;
import hu.rycus.tweetwear.twitter.account.AccountProvider;
import hu.rycus.tweetwear.twitter.account.IAccountProvider;
import hu.rycus.tweetwear.twitter.client.ITwitterClient;
import hu.rycus.tweetwear.twitter.client.TwitterClient;

public class TwitterFactory {

    public static ITwitterClient createClient() {
        return new TwitterClient();
    }

    public static IAccountProvider createProvider() {
        return new AccountProvider();
    }

    public static long getUserId(final Context context,
                                 final ITwitterClient twitterClient, final Token accessToken) {
        final long savedUserId = Preferences.getUserId(context);
        if (savedUserId < 0L) {
            final User user = twitterClient.getUser(accessToken);
            if (user != null) {
                Preferences.saveUserId(context, user.getId());
                return user.getId();
            } else {
                return -1L;
            }
        } else {
            return savedUserId;
        }
    }

}
