package hu.rycus.tweetwear.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.text.Html;

import hu.rycus.tweetwear.BuildConfig;
import hu.rycus.tweetwear.R;
import hu.rycus.tweetwear.SyncService;
import hu.rycus.tweetwear.preferences.ListSettings;
import hu.rycus.tweetwear.preferences.Preferences;

public class SettingsHelper {

    private static final String KEY_ACCOUNT_INFO = "ac_account_info";
    private static final String KEY_ACCOUNT_LISTS = "ac_account_lists";
    private static final String KEY_SYNC_INTERVAL = "interval";
    private static final String KEY_SYNC_NOW = "ac_sync_now";
    private static final String KEY_SYNC_DEMO = "ac_sync_demo";
    private static final String KEY_CLEAR_EXISTING = "ac_clear_existing";
    private static final String KEY_VERSION_HEADER = "ac_version_header";

    private static final String CAT_SYNC = "cat_sync";

    public static Preference findAccountInfo(final PreferenceActivity activity) {
        return findPreferenceItem(activity, KEY_ACCOUNT_INFO);
    }

    public static void setupLists(final PreferenceActivity activity) {
        final Preference preference = findPreferenceItem(activity, KEY_ACCOUNT_LISTS);
        final ListSettings settings = Preferences.getListSettings(activity);

        String summary = "";
        if (settings.isTimelineSelected()) {
            summary += activity.getString(R.string.timeline);
        }

        final int selectedListCount = settings.getSelectedListIds().size();
        if (selectedListCount > 0) {
            if (settings.isTimelineSelected()) {
                summary += " & ";
            }

            summary += activity.getResources().getQuantityString(
                    R.plurals.pref_list_summary_lists, selectedListCount, selectedListCount);
        }

        if (!settings.isTimelineSelected() && selectedListCount == 0) {
            summary = activity.getString(R.string.nothing_selected);
        }

        preference.setSummary(summary);

        preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(final Preference preference) {
                final Intent intent = new Intent(activity, ListSettingActivity.class);
                activity.startActivity(intent);
                return false;
            }
        });
    }

    public static void setupSyncInterval(final PreferenceActivity activity) {
        final ListPreference listPreference =
                (ListPreference) findPreferenceItem(activity, KEY_SYNC_INTERVAL);
        final String value = listPreference.getValue();
        final int index = listPreference.findIndexOfValue(value);
        final CharSequence name = listPreference.getEntries()[index];
        listPreference.setSummary(name);
    }

    public static void setupSyncNow(final PreferenceActivity activity) {
        final Preference preference = findPreferenceItem(activity, KEY_SYNC_NOW);
        preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(final Preference preference) {
                SyncService.startSync(preference.getContext());
                return true;
            }
        });
    }

    public static void setupCreateDemo(final PreferenceActivity activity) {
        final Preference preference = findPreferenceItem(activity, KEY_SYNC_DEMO);
        if (BuildConfig.DEBUG) {
            preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(final Preference preference) {
                    SyncService.createDemoTweet(preference.getContext());
                    return true;
                }
            });
        } else {
            findPreferenceCategory(activity, CAT_SYNC).removePreference(preference);
        }
    }

    public static void setupClearExisting(final PreferenceActivity activity) {
        final Preference preference = findPreferenceItem(activity, KEY_CLEAR_EXISTING);
        if (BuildConfig.DEBUG) {
            preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(final Preference preference) {
                    SyncService.clearExisting(preference.getContext());
                    return true;
                }
            });
        } else {
            findPreferenceCategory(activity, CAT_SYNC).removePreference(preference);
        }
    }

    public static void setupVersionHeader(final PreferenceActivity activity) {
        final Preference preference = findPreferenceItem(activity, KEY_VERSION_HEADER);
        final String content = activity.getString(R.string.about_version, BuildConfig.VERSION_NAME);
        preference.setTitle(Html.fromHtml(content));
    }

    public static void enableItemsRequireAuthentication(final PreferenceActivity activity) {
        setItemStatesThatRequireAuthentication(activity, true);
    }

    public static void disableItemsRequireAuthentication(final PreferenceActivity activity) {
        setItemStatesThatRequireAuthentication(activity, false);
    }

    private static void setItemStatesThatRequireAuthentication(
            final PreferenceActivity activity, final boolean enabled) {
        findPreferenceItem(activity, KEY_ACCOUNT_LISTS).setEnabled(enabled);
        findPreferenceCategory(activity, CAT_SYNC).setEnabled(enabled);
    }

    @SuppressWarnings("deprecation")
    public static void setupSharedPreferences(final PreferenceActivity activity) {
        activity.getPreferenceManager().setSharedPreferencesName(Preferences.PREFERENCES_NAME);

        // fix a bug when refresh interval was modified and saved as a Long
        Preferences.fixRefreshIntervalIfNeeded(activity);
    }

    @SuppressWarnings("deprecation")
    private static PreferenceCategory findPreferenceCategory(
            final PreferenceActivity activity, final String key) {
        return (PreferenceCategory) activity.findPreference(key);
    }

    @SuppressWarnings("deprecation")
    private static Preference findPreferenceItem(
            final PreferenceActivity activity, final String key) {
        return activity.findPreference(key);
    }

    public static SharedPreferences.OnSharedPreferenceChangeListener createPreferenceListener(
            final PreferenceActivity activity) {
        return new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(
                    final SharedPreferences sharedPreferences, final String key) {
                if (KEY_SYNC_INTERVAL.equals(key)) {
                    setupSyncInterval(activity);
                    SyncService.scheduleSync(activity);
                }
            }
        };
    }

}
