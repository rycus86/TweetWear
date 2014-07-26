package hu.rycus.tweetwear.ui;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import hu.rycus.tweetwear.R;
import hu.rycus.tweetwear.SyncService;
import hu.rycus.tweetwear.preferences.Preferences;

public class RefreshItem extends SettingsItem<Long> {

    private static final long[] INTERVALS = new long[] {
            AlarmManager.INTERVAL_FIFTEEN_MINUTES,
            AlarmManager.INTERVAL_HALF_HOUR,
            AlarmManager.INTERVAL_HOUR,
            AlarmManager.INTERVAL_HOUR * 2L,
            AlarmManager.INTERVAL_HOUR * 3L,
            AlarmManager.INTERVAL_HOUR * 6L,
            AlarmManager.INTERVAL_HALF_DAY
    };

    private long interval;

    public RefreshItem(final Context context) {
        this.interval = Preferences.getRefreshInterval(context);
    }

    @Override
    public Long getItem() {
        return interval;
    }

    @Override
    protected View createView(final Context context, final ViewGroup parent) {
        final View view = LayoutInflater.from(context).inflate(R.layout.item_refresh, parent, false);
        final TextView txtValue = (TextView) view.findViewById(R.id.txt_value);
        txtValue.setText(getTextForInterval(context));
        return view;
    }

    private String getTextForInterval(final Context context) {
        final String[] items = context.getResources().getStringArray(R.array.refresh_intervals);
        for (int index = 0; index < items.length && index < INTERVALS.length; index++) {
            if (interval == INTERVALS[index]) {
                return items[index];
            }
        }

        return context.getString(R.string.unknown_interval);
    }

    @Override
    protected void onClick(final Context context) {
        final int layoutResource = android.R.layout.simple_list_item_1;
        final String[] items = context.getResources().getStringArray(R.array.refresh_intervals);

        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                context, layoutResource, items);

        final DialogInterface.OnClickListener selectionListener =
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(final DialogInterface dialog, final int which) {
                    intervalSelected(context, INTERVALS[which]);
                }
        };

        new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.refresh_title))
                .setAdapter(adapter, selectionListener)
                .show();
    }

    private void intervalSelected(final Context context, final long newInterval) {
        if (newInterval != interval) {
            if (Preferences.setRefreshInterval(context, newInterval)) {
                this.interval = newInterval;

                getAdapter().notifyDataSetChanged();
                SyncService.scheduleSync(context);
            }
        }
    }

}
