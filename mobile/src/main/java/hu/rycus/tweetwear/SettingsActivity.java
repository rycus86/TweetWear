package hu.rycus.tweetwear;

import android.app.ListActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import hu.rycus.rtweetwear.common.util.Constants;
import hu.rycus.tweetwear.twitter.TwitterFactory;
import hu.rycus.tweetwear.twitter.client.callbacks.AccessTokenCallback;
import hu.rycus.tweetwear.ui.SettingsAdapter;


public class SettingsActivity extends ListActivity implements AccessTokenCallback {

    private static final String TAG = SettingsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        setListAdapter(SettingsAdapter.create(this));
    }

    @Override
    protected void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);
        checkForOauthCallback(intent);
    }

    private void checkForOauthCallback(final Intent intent) {
        final Uri uri = intent.getData();
        if (uri != null) {
            final String verifier = uri.getQueryParameter(Constants.QUERY_PARAM_OAUTH_VERIFIER);
            if (verifier != null) {
                handleOauthVerification(verifier);
            }
        }
    }

    private void handleOauthVerification(final String verifier) {
        Log.d(TAG, "Handling OAuth verification");
        TwitterFactory.createClient().processAccessToken(this, verifier, this);
    }

    @Override
    public void onAccessTokenSaved() {
        Log.d(TAG, "OAuth access token ready");
        setListAdapter(SettingsAdapter.create(this));
        SyncService.scheduleSync(this);
    }

    @Override
    public void onAccessTokenError() {
        Toast.makeText(this, "Failed to get access token", Toast.LENGTH_SHORT).show();
    }

}
