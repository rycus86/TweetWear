package hu.rycus.tweetwear.twitter.async;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.scribe.model.Token;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

import hu.rycus.tweetwear.preferences.Preferences;
import hu.rycus.tweetwear.twitter.client.callbacks.AccessTokenCallback;

public class ProcessAccessTokenTask extends AsyncTask<String, Void, Token> {

    private static final String TAG = ProcessAccessTokenTask.class.getSimpleName();

    private final Context context;
    private final OAuthService service;
    private final AccessTokenCallback callback;

    public ProcessAccessTokenTask(final Context context, final OAuthService service,
                                  final AccessTokenCallback callback) {
        this.context = context;
        this.service = service;
        this.callback = callback;
    }

    @Override
    protected Token doInBackground(final String... params) {
        try {
            final String oauthVerifier = params[0];
            final Token requestToken = Preferences.getRequestToken(context);
            final Verifier verifier = new Verifier(oauthVerifier);
            return service.getAccessToken(requestToken, verifier);
        } catch (Exception ex) {
            Log.e(TAG, "Failed to process access token", ex);
        }

        return null;
    }

    @Override
    protected void onPostExecute(final Token token) {
        if (token == null) {
            callback.onAccessTokenError();
            return;
        }

        final boolean saved = Preferences.saveUserToken(context, token);
        if (saved) {
            callback.onAccessTokenSaved();
        } else {
            callback.onAccessTokenError();
        }
    }

}
