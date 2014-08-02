package hu.rycus.tweetwear.twitter.async;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.scribe.model.Token;
import org.scribe.oauth.OAuthService;

import hu.rycus.tweetwear.common.util.Value;
import hu.rycus.tweetwear.preferences.Preferences;
import hu.rycus.tweetwear.twitter.client.ITwitterClient;
import hu.rycus.tweetwear.twitter.client.RequestBuilder;
import hu.rycus.tweetwear.twitter.client.callbacks.AccessLevelCallback;

public class CheckAccessLevelTask extends AsyncTask<Token, Void, Boolean> {

    private static final String TAG = CheckAccessLevelTask.class.getSimpleName();

    private final Context context;
    private final OAuthService service;
    private final AccessLevelCallback callback;

    public CheckAccessLevelTask(final Context context, final OAuthService service,
                                final AccessLevelCallback callback) {
        this.context = context;
        this.service = service;
        this.callback = callback;
    }

    @Override
    protected Boolean doInBackground(final Token... params) {
        final Token accessToken = params[0];

        if (Preferences.hasDesiredAccessLevel(context)) {
            return false;
        }

        try {
            final Value<String> headerValue = new Value<String>(null);
            RequestBuilder.head(ITwitterClient.Uri.VERIFY_CREDENTIALS.get())
                    .send(service, accessToken)
                    .header(ITwitterClient.AccessLevel.header(), headerValue);

            final String level = headerValue.get();
            Preferences.saveAccessLevel(context, level);

            return !ITwitterClient.AccessLevel.desired().matches(level);
        } catch (Exception ex) {
            Log.e(TAG, "Failed to check if user needs a new access token", ex);
        }

        return false;
    }

    @Override
    protected void onPostExecute(final Boolean result) {
        if (result != null && result) {
            callback.onNewAccessTokenRequired();
        }
    }

}
