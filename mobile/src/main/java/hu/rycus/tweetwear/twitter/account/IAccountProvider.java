package hu.rycus.tweetwear.twitter.account;

import android.content.Context;

import java.util.Collection;

public interface IAccountProvider {

    Collection<Account> getAccounts(Context context);

}
