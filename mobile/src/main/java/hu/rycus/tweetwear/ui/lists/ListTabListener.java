package hu.rycus.tweetwear.ui.lists;

import android.app.ActionBar;
import android.app.FragmentTransaction;

import hu.rycus.tweetwear.R;
import hu.rycus.tweetwear.twitter.client.ITwitterClient;
import hu.rycus.tweetwear.ui.ListSettingActivity;

public class ListTabListener implements ActionBar.TabListener {

    private final ListSettingActivity activity;
    private final ITwitterClient.ListType listType;

    private ListFragment listFragment;

    public ListTabListener(final ListSettingActivity activity,
                           final ITwitterClient.ListType listType) {
        this.activity = activity;
        this.listType = listType;

        this.listFragment = (ListFragment)
                activity.getFragmentManager().findFragmentByTag(listType.name());
        detachPreviousIfExists(activity);
    }

    private void detachPreviousIfExists(final ListSettingActivity activity) {
        if (listFragment != null && !listFragment.isDetached()) {
            final FragmentTransaction ft = activity.getFragmentManager().beginTransaction();
            ft.detach(listFragment).commit();
        }
    }

    @Override
    public void onTabSelected(final ActionBar.Tab tab, final FragmentTransaction ft) {
        if (listFragment == null) {
            listFragment = ListFragment.create(listType);

            ft.add(R.id.container, listFragment, listType.name());
        } else {
            ft.attach(listFragment);
        }

        resetListAdapterData();
        activity.showTimelineContainer();
    }

    @Override
    public void onTabUnselected(final ActionBar.Tab tab, final FragmentTransaction ft) {
        resetListAdapterData();

        if (listFragment != null) {
            ft.detach(listFragment);
        }
    }

    @Override
    public void onTabReselected(final ActionBar.Tab tab, final FragmentTransaction ft) {
    }

    public void resetListAdapterData() {
        if (listFragment != null) {
            listFragment.resetAdapterData();
        }
    }

}
