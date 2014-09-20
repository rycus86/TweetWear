package hu.rycus.tweetwear;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.scribe.model.Token;

import hu.rycus.tweetwear.common.util.Constants;
import hu.rycus.tweetwear.database.TweetWearDatabase;
import hu.rycus.tweetwear.preferences.Preferences;
import hu.rycus.tweetwear.promo.Promotions;
import hu.rycus.tweetwear.ril.ReadItLater;
import hu.rycus.tweetwear.twitter.TwitterFactory;
import hu.rycus.tweetwear.twitter.client.callbacks.AccessLevelCallback;
import hu.rycus.tweetwear.twitter.client.callbacks.AccessTokenCallback;
import hu.rycus.tweetwear.twitter.client.callbacks.UsernameCallback;
import hu.rycus.tweetwear.ui.ReadItLaterActivity;
import hu.rycus.tweetwear.ui.SettingsHelper;

public class SettingsActivity extends PreferenceActivity
        implements UsernameCallback, AccessLevelCallback, AccessTokenCallback {

    private static final String TAG = SettingsActivity.class.getSimpleName();

    private final SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener =
            SettingsHelper.createPreferenceListener(this);

    private Preference accountItem;

    private TextView txtReadLaterCount;
    private int readLaterCount;

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

        final IntentFilter filter = new IntentFilter(Constants.ACTION_BROADCAST_READ_IT_LATER);
        LocalBroadcastManager.getInstance(this).registerReceiver(readLaterBroadcastReceiver, filter);

        // refresh lists summary, maybe we just came back from lists settings
        SettingsHelper.setupLists(this);

        loadReadLaterCount();

        Promotions.showDialogIfNeeded(this);
    }

    @Override
    protected void onPause() {
        Promotions.dismissDialog();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(readLaterBroadcastReceiver);

        getSharedPreferences(Preferences.PREFERENCES_NAME, MODE_PRIVATE)
                .unregisterOnSharedPreferenceChangeListener(preferenceChangeListener);

        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_settings, menu);
        setupReadLaterMenu(menu);
        setupDeleteDatabaseMenuItem(menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_read_later: {
                onReadLaterItemSelected();
                return true;
            }
            case R.id.debug_delete_database: {
                onDeleteDatabaseSelected();
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
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

    private void setupReadLaterMenu(final Menu menu) {
        final int id = R.id.menu_read_later;
        final MenuItem item = menu.findItem(id);

        final View view = item.getActionView();
        view.setHapticFeedbackEnabled(true);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                menu.performIdentifierAction(id, 0);
            }
        });
        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(final View v) {
                final Toast toast = Toast
                        .makeText(v.getContext(), item.getTitle(), Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.TOP | Gravity.END, v.getWidth() / 2, v.getHeight());
                toast.show();

                return true;
            }
        });

        txtReadLaterCount = (TextView) view.findViewById(R.id.txt_read_later_count);

        loadReadLaterCount();
    }

    private void loadReadLaterCount() {
        if (!skipLoadingReadLaterCount()) {
            final LoadReadLaterCountTask task = new LoadReadLaterCountTask();
            task.execute();
        }
    }

    private boolean skipLoadingReadLaterCount() {
        return txtReadLaterCount == null;
    }

    private void setReadLaterCount(final int count) {
        readLaterCount = count;
        txtReadLaterCount.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
        if (count > 99) {
            txtReadLaterCount.setText(R.string.infinity_sign);
        } else {
            txtReadLaterCount.setText(Integer.toString(count));
        }
    }

    private void onReadLaterItemSelected() {
        if (readLaterCount > 0) {
            ReadItLaterActivity.start(SettingsActivity.this);
        } else {
            startReadItLaterIfThereAreArchives();
        }
    }

    private void startReadItLaterIfThereAreArchives() {
        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(final Void... params) {
                return ReadItLater.countArchives(SettingsActivity.this);
            }

            @Override
            protected void onPostExecute(final Integer count) {
                if (count > 0) {
                    ReadItLaterActivity.start(SettingsActivity.this);
                } else {
                    Toast.makeText(SettingsActivity.this,
                            getString(R.string.read_later_empty),
                            Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }

    private void setupDeleteDatabaseMenuItem(final Menu menu) {
        final boolean active = BuildConfig.DEBUG;
        final MenuItem item = menu.findItem(R.id.debug_delete_database);
        item.setEnabled(active);
        item.setVisible(active);
    }

    private void onDeleteDatabaseSelected() {
        deleteDatabase(TweetWearDatabase.getName());
        finish();
    }

    private final BroadcastReceiver readLaterBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            if (Constants.ACTION_BROADCAST_READ_IT_LATER.equals(intent.getAction())) {
                loadReadLaterCount();
            }
        }
    };

    private class LoadReadLaterCountTask extends AsyncTask<Void, Void, Integer> {
        @Override
        protected Integer doInBackground(final Void... params) {
            return ReadItLater.count(SettingsActivity.this);
        }

        @Override
        protected void onPostExecute(final Integer count) {
            setReadLaterCount(count);
        }

    }

}
