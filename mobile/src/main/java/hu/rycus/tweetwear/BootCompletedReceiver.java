package hu.rycus.tweetwear;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Collection;

import hu.rycus.tweetwear.twitter.TwitterFactory;
import hu.rycus.tweetwear.twitter.account.Account;

public class BootCompletedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            final Collection<Account> accounts =
                    TwitterFactory.createProvider().getAccounts(context);
            if (!accounts.isEmpty()) {
                SyncService.scheduleSync(context);
            }
        }
    }
}
