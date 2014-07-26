package hu.rycus.tweetwear.twitter.async;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.scribe.model.Token;
import org.scribe.oauth.OAuthService;

import hu.rycus.tweetwear.R;
import hu.rycus.tweetwear.preferences.Preferences;

public class AuthorizationTask extends AsyncTask<Void, Void, String> {

    private static final String TAG = AuthorizationTask.class.getSimpleName();

    private final Context context;
    private final OAuthService service;

    public AuthorizationTask(final Context context, final OAuthService service) {
        this.context = context;
        this.service = service;
    }

    @Override
    protected String doInBackground(final Void... params) {
        try {
            final Token requestToken = service.getRequestToken();
            if (!Preferences.saveRequestToken(context, requestToken)) {
                throw new IllegalArgumentException("Failed to save request token");
            }
            return service.getAuthorizationUrl(requestToken);
        } catch (Exception ex) {
            Log.e(TAG, "Failed to request authorization", ex);
        }

        return null;
    }

    @Override
    protected void onPostExecute(final String url) {
        if (url != null) {
            final Intent intent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url));
            context.startActivity(intent);
        } else {
            final String message = context.getString(R.string.error_authorization);
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }

}
