package hu.rycus.tweetwear.twitter.account;

import android.content.Context;

import org.scribe.model.Token;

import java.util.Collection;
import java.util.Collections;

import hu.rycus.tweetwear.preferences.Preferences;

public class AccountProvider implements IAccountProvider {

    @Override
    public Collection<Account> getAccounts(final Context context) {
        final Token accessToken = Preferences.getUserToken(context);
        if (accessToken != null) {
            final String username = getUsername(context);
            return Collections.singleton(new Account(username, accessToken));
        } else {
            return Collections.emptyList();
        }
    }

    private String getUsername(final Context context) {
        final String username = Preferences.getUserName(context);
        if (username != null) {
            return username;
        } else {
            return "Unknown user";
        }
    }

}
