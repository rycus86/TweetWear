package hu.rycus.tweetwear.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import hu.rycus.tweetwear.R;
import hu.rycus.tweetwear.preferences.ListSettings;
import hu.rycus.tweetwear.preferences.Preferences;
import hu.rycus.tweetwear.twitter.client.ITwitterClient;
import hu.rycus.tweetwear.ui.lists.ListTabListener;

public class ListSettingActivity extends Activity {

    private static final String TAG = ListSettingActivity.class.getSimpleName();

    private static final String KEY_SELECTED_TAB =
            ListSettingActivity.class.getCanonicalName() + ".selectedTab";

    private View vTimelineContainer;
    private CheckBox ckboxTimeline;

    private ListTabListener tabListenerSubscription;
    private ListTabListener tabListenerOwnership;

    private boolean timelineContainerShowing = true;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_settings);

        tabListenerSubscription = new ListTabListener(this, ITwitterClient.ListType.SUBSCRIPTIONS);
        tabListenerOwnership = new ListTabListener(this, ITwitterClient.ListType.OWNERSHIPS);

        vTimelineContainer = findViewById(R.id.timeline_container);
        ckboxTimeline = (CheckBox) vTimelineContainer.findViewById(R.id.chkbox_timeline);
        ckboxTimeline.setChecked(isTimelineSelected());

        vTimelineContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                ckboxTimeline.performClick();
            }
        });

        ckboxTimeline.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                if (!setTimelineSelected(isChecked)) {
                    Log.e(TAG, "Failed to save list settings");
                }
            }
        });

        final Integer selectedTab;
        if (savedInstanceState != null) {
            selectedTab = (Integer) savedInstanceState.get(KEY_SELECTED_TAB);
        } else {
            selectedTab = null;
        }

        setupActionBarTabs(selectedTab);
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);

        final ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            outState.putInt(KEY_SELECTED_TAB, actionBar.getSelectedTab().getPosition());
        }
    }

    private void setupActionBarTabs(final Integer selectedTab) {
        final ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

            actionBar.addTab(actionBar.newTab()
                    .setText(getString(R.string.lists_subscribed).toUpperCase())
                    .setTabListener(tabListenerSubscription));
            actionBar.addTab(actionBar.newTab()
                    .setText(getString(R.string.lists_owned).toUpperCase())
                    .setTabListener(tabListenerOwnership));

            if (selectedTab != null) {
                actionBar.getTabAt(selectedTab).select();
            }
        }
    }

    public void showTimelineContainer() {
        if (!timelineContainerShowing) {
            timelineContainerShowing = true;
            vTimelineContainer.startAnimation(createAlphaAnimation(0f, 1f));
        }
    }

    public void hideTimelineContainer() {
        if (timelineContainerShowing) {
            timelineContainerShowing = false;
            vTimelineContainer.startAnimation(createAlphaAnimation(1f, 0f));
        }
    }

    private Animation createAlphaAnimation(final float start, final float end) {
        final Animation animation = new AlphaAnimation(start, end);
        animation.setDuration(500L);
        animation.setFillAfter(true);
        return animation;
    }

    private boolean isTimelineSelected() {
        return getSettings().isTimelineSelected();
    }

    private boolean setTimelineSelected(final boolean selected) {
        final ListSettings settings = getSettings();
        settings.setTimelineSelected(selected);
        final boolean saved = Preferences.saveListSettings(this, settings);
        if (saved) {
            tabListenerSubscription.resetListAdapterData();
            tabListenerOwnership.resetListAdapterData();
        }
        return saved;
    }

    private ListSettings getSettings() {
        return Preferences.getListSettings(this);
    }

}
