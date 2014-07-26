package hu.rycus.tweetwear.twitter;

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

}
