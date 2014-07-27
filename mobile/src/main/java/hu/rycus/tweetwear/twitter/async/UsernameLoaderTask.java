package hu.rycus.tweetwear.twitter.async;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.scribe.model.Token;
import org.scribe.oauth.OAuthService;

import hu.rycus.tweetwear.common.model.AccountSettings;
import hu.rycus.tweetwear.preferences.Preferences;
import hu.rycus.tweetwear.twitter.client.ITwitterClient;
import hu.rycus.tweetwear.twitter.client.RequestBuilder;
import hu.rycus.tweetwear.twitter.client.callbacks.UsernameCallback;

public class UsernameLoaderTask extends AsyncTask<Token, Void, String> {

    private static final String TAG = UsernameLoaderTask.class.getSimpleName();

    private final Context context;
    private final OAuthService service;
    private final UsernameCallback callback;

    public UsernameLoaderTask(final Context context, final OAuthService service,
                              final UsernameCallback callback) {
        this.context = context;
        this.service = service;
        this.callback = callback;
    }

    @Override
    protected String doInBackground(final Token... params) {
        final String savedUsername = Preferences.getUserName(context);
        if (savedUsername != null) {
            return savedUsername;
        }

        try {
            final Token accessToken = params[0];
            final AccountSettings settings = RequestBuilder
                    .start(ITwitterClient.Uri.SETTINGS.get())
                    .get(service, accessToken)
                    .respond(AccountSettings.class);
            return settings.getScreenName();
        } catch (Exception ex) {
            Log.e(TAG, "Failed to load account settings", ex);
        }

        return null;
    }

    @Override
    protected void onPostExecute(final String name) {
        if (name == null) {
            callback.onUsernameLoadError();
            return;
        }

        final boolean saved = Preferences.saveUserName(context, name);
        if (saved) {
            callback.onUsernameLoaded(name);
        } else {
            callback.onUsernameLoadError();
        }
    }

}
