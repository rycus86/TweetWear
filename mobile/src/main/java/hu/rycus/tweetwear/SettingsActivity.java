package hu.rycus.tweetwear;


import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.widget.Toast;

import org.scribe.model.Token;

import hu.rycus.tweetwear.common.util.Constants;
import hu.rycus.tweetwear.preferences.Preferences;
import hu.rycus.tweetwear.twitter.TwitterFactory;
import hu.rycus.tweetwear.twitter.client.callbacks.AccessLevelCallback;
import hu.rycus.tweetwear.twitter.client.callbacks.AccessTokenCallback;
import hu.rycus.tweetwear.twitter.client.callbacks.UsernameCallback;
import hu.rycus.tweetwear.ui.SettingsHelper;

public class SettingsActivity extends PreferenceActivity
        implements UsernameCallback, AccessLevelCallback, AccessTokenCallback {

    private static final String TAG = SettingsActivity.class.getSimpleName();

    private final SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener =
            SettingsHelper.createPreferenceListener(this);

    private Preference accountItem;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SettingsHelper.setupSharedPreferences(this);

        // we could use the newer-style preference activity
        // but we don't have that many preferences, so a flat
        // settings activity is probably better
        addPreferencesTheOldWay();

        setupUI();
    }

    @Override
    protected void onResume() {
        super.onResume();

        getSharedPreferences(Preferences.PREFERENCES_NAME, MODE_PRIVATE)
            .registerOnSharedPreferenceChangeListener(preferenceChangeListener);

        // refresh lists summary, maybe we just came back from lists settings
        SettingsHelper.setupLists(this);
    }

    @Override
    protected void onPause() {
        getSharedPreferences(Preferences.PREFERENCES_NAME, MODE_PRIVATE)
                .unregisterOnSharedPreferenceChangeListener(preferenceChangeListener);
        super.onPause();
    }

    @SuppressWarnings("deprecation")
    private void addPreferencesTheOldWay() {
        addPreferencesFromResource(R.xml.preferences);
    }

    private void setupUI() {
        accountItem = SettingsHelper.findAccountInfo(this);
        setupAccountItem();

        SettingsHelper.setupLists(this);
        SettingsHelper.setupSyncInterval(this);
        SettingsHelper.setupSyncNow(this);
        SettingsHelper.setupCreateDemo(this);
        SettingsHelper.setupClearExisting(this);
        SettingsHelper.setupVersionHeader(this);
    }

    private void setupAccountItem() {
        final Token token = Preferences.getUserToken(this);
        if (token != null) {
            onAccessTokenPresent();
        } else {
            onAccessTokenNotPresent();
        }
    }

    private void onAccessTokenPresent() {
        setupUsername();
        setupAccessLevelNotice();
    }

    private void setupUsername() {
        final String existingUsername = Preferences.getUserName(this);
        if (existingUsername != null) {
            accountItem.setTitle(formatUsername(existingUsername));
        } else {
            accountItem.setTitle(R.string.loading_account);
            TwitterFactory.createClient().loadUsername(this, this);
        }
    }

    private void setupAccessLevelNotice() {
        if (!Preferences.hasDesiredAccessLevel(this)) {
            TwitterFactory.createClient().checkAccessLevel(this, this);
        }
    }

    private void onAccessTokenNotPresent() {
        SettingsHelper.disableItemsRequireAuthentication(this);

        accountItem.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(final Preference preference) {
                TwitterFactory.createClient().authorize(preference.getContext());
                return true;
            }
        });
    }

    @Override
    public void onNewAccessTokenRequired() {
        accountItem.setSummary(R.string.notice_access_token);
        accountItem.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(final Preference preference) {
                TwitterFactory.createClient().authorize(preference.getContext());
                return true;
            }
        });
    }

    @Override
    public void onUsernameLoaded(final String name) {
        accountItem.setTitle(formatUsername(name));
    }

    @Override
    public void onUsernameLoadError() {
        Toast.makeText(this, getString(R.string.error_account), Toast.LENGTH_SHORT).show();
    }

    private String formatUsername(final String name) {
        return String.format("@%s", name);
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

        SettingsHelper.enableItemsRequireAuthentication(this);

        accountItem.setTitle(getString(R.string.loading_account));

        TwitterFactory.createClient().loadUsername(this, this);
        TwitterFactory.createClient().checkAccessLevel(this, this);

        SyncService.scheduleSync(this);
    }

    @Override
    public void onAccessTokenError() {
        Toast.makeText(this, R.string.error_access_token_save, Toast.LENGTH_SHORT).show();
    }

}
