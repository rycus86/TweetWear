package hu.rycus.tweetwear.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.scribe.model.Token;

import java.util.Collection;
import java.util.Collections;

import hu.rycus.tweetwear.R;
import hu.rycus.tweetwear.common.api.ApiClientHelper;
import hu.rycus.tweetwear.common.model.Tweet;
import hu.rycus.tweetwear.common.util.TweetData;
import hu.rycus.tweetwear.tasks.FetchTimelineTask;
import hu.rycus.tweetwear.twitter.account.Account;
import hu.rycus.tweetwear.twitter.account.IAccountProvider;
import hu.rycus.tweetwear.twitter.client.ITwitterClient;
import hu.rycus.tweetwear.twitter.client.callbacks.AccessLevelCallback;
import hu.rycus.tweetwear.twitter.client.callbacks.AccessTokenCallback;
import hu.rycus.tweetwear.twitter.client.callbacks.UsernameCallback;

public class CreateDemoItem extends SettingsItem<Void> {

    @Override
    public Void getItem() {
        return null;
    }

    @Override
    protected View createView(final Context context, final ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.item_sync_demo, parent, false);
    }

    @Override
    protected void onClick(final Context context) {
        final IAccountProvider provider = new DemoAccountProvider();
        final ITwitterClient twitterClient = new DemoTwitterClient();
        ApiClientHelper.runAsynchronously(context, new FetchTimelineTask(provider, twitterClient));
    }

    private class DemoAccountProvider implements IAccountProvider {
        @Override
        public Collection<Account> getAccounts(final Context context) {
            return Collections.singleton(new Account("test@demo.com", null));
        }
    }

    private class DemoTwitterClient implements ITwitterClient {
        public void authorize(final Context context) {}
        public void processAccessToken(final Context context, final String oauthVerifier, final AccessTokenCallback callback) {}
        public void loadUsername(final Context context, final UsernameCallback callback) {}
        public void checkAccessLevel(final Context context, final AccessLevelCallback callback) {}
        public Tweet retweet(final Token accessToken, final long id, final Boolean trimUser) { return null; }
        public Tweet favorite(final Token accessToken, final long id, final Boolean includeEntities) { return null; }

        @Override
        public Tweet[] getTimeline(final Token accessToken, final Integer count, final Long sinceId, final Long maxId, final Boolean trimUser, final Boolean excludeReplies, final Boolean contributorDetails, final Boolean includeEntities) {
            return new Tweet[] { TweetData.demo(System.currentTimeMillis()) };
        }
    }

}
