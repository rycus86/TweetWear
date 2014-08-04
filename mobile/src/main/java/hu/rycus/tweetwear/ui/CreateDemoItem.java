package hu.rycus.tweetwear.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
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
        final ITwitterClient twitterClient = DemoTwitterClient.create();
        ApiClientHelper.runAsynchronously(context, new FetchTimelineTask(provider, twitterClient));
    }

    private class DemoAccountProvider implements IAccountProvider {
        @Override
        public Collection<Account> getAccounts(final Context context) {
            return Collections.singleton(new Account("test@demo.com", null));
        }
    }

    private static class DemoTwitterClient implements InvocationHandler {

        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
            if (method.getName().equals("getTimeline")) {
                return new Tweet[] { TweetData.demo(System.currentTimeMillis()) };
            }

            return null;
        }

        public static ITwitterClient create() {
            return (ITwitterClient) Proxy.newProxyInstance(
                    DemoTwitterClient.class.getClassLoader(),
                    new Class[] { ITwitterClient.class },
                    new DemoTwitterClient());
        }

    }

}
