package hu.rycus.tweetwear.ui;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import org.scribe.model.Token;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import hu.rycus.tweetwear.BuildConfig;
import hu.rycus.tweetwear.preferences.Preferences;

public class SettingsAdapter extends BaseAdapter {

    private final List<SettingsItem> items;

    private SettingsAdapter(final List<SettingsItem> items) {
        this.items = Collections.unmodifiableList(new ArrayList<SettingsItem>(items));
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(final int position) {
        return items.get(position).getItem();
    }

    @Override
    public long getItemId(final int position) {
        return position;
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        return items.get(position).getView(parent.getContext(), parent);
    }

    public static SettingsAdapter create(final Context context) {
        final List<SettingsItem> items = new LinkedList<SettingsItem>();

        final Token token = Preferences.getUserToken(context);
        if (token != null) {
            items.add(new AccountItem(context));
            items.add(new RefreshItem(context));
            if (BuildConfig.DEBUG) {
                items.add(new SyncNowItem());
                items.add(new ClearExistingItem());
            }
        } else {
            items.add(new SigninItem());
        }
        items.add(new AboutItem());

        final SettingsAdapter adapter = new SettingsAdapter(items);
        for (final SettingsItem item : items) {
            item.setAdapter(adapter);
            item.initialize();
        }

        return adapter;
    }

}
