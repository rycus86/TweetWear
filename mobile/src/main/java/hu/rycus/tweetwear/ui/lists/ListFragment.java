package hu.rycus.tweetwear.ui.lists;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;

import hu.rycus.tweetwear.R;
import hu.rycus.tweetwear.twitter.client.ITwitterClient;
import hu.rycus.tweetwear.ui.ListSettingActivity;

public class ListFragment extends android.app.ListFragment {

    private ListSettingActivity activity;

    private static final String KEY_TYPE = "list$type";

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);
        this.activity = (ListSettingActivity) activity;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setListAdapter(new ListAdapter(getListType()));
    }

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setEmptyText(getString(R.string.lists_empty));

        getListView().setOnScrollListener(new AbsListView.OnScrollListener() {
            boolean scrolling = false;

            @Override
            public void onScrollStateChanged(final AbsListView view, final int scrollState) {
                scrolling = scrollState == SCROLL_STATE_TOUCH_SCROLL;
                if (scrolling) {
                    activity.hideTimelineContainer();
                }
            }

            @Override
            public void onScroll(final AbsListView view, final int firstVisibleItem,
                                 final int visibleItemCount, final int totalItemCount) {
                if (firstVisibleItem == 0 && !scrolling) {
                    final View firstChild = view.getChildAt(0);
                    if (firstChild == null || firstChild.getTop() == 0) {
                        activity.showTimelineContainer();
                    }
                }
            }
        });
    }

    void resetAdapterData() {
        final android.widget.ListAdapter adapter = getListAdapter();
        if (adapter != null && adapter instanceof ListAdapter) {
            ((ListAdapter) adapter).resetListData();
        }
    }

    private ITwitterClient.ListType getListType() {
        final int typeOrdinal = getArguments().getInt(KEY_TYPE);
        return ITwitterClient.ListType.values()[typeOrdinal];
    }

    public static ListFragment create(final ITwitterClient.ListType listType) {
        final ListFragment fragment = new ListFragment();
        final Bundle bundle = new Bundle(1);
        bundle.putInt(KEY_TYPE, listType.ordinal());
        fragment.setArguments(bundle);
        return fragment;
    }

}
