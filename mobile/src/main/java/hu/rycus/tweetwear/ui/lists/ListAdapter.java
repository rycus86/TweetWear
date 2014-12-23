package hu.rycus.tweetwear.ui.lists;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

import hu.rycus.tweetwear.R;
import hu.rycus.tweetwear.common.model.List;
import hu.rycus.tweetwear.common.model.Lists;
import hu.rycus.tweetwear.common.util.Mapper;
import hu.rycus.tweetwear.preferences.ListSettings;
import hu.rycus.tweetwear.preferences.Preferences;
import hu.rycus.tweetwear.twitter.TwitterFactory;
import hu.rycus.tweetwear.twitter.client.ITwitterClient;

public class ListAdapter extends BaseAdapter {

    private static final String TAG = "lists." + ListAdapter.class.getSimpleName();

    private static final String KEY_PREFIX = ListAdapter.class.getCanonicalName();
    private static final String KEY_CONTENTS = KEY_PREFIX + ".contents";
    private static final String KEY_LIST_SIZE = KEY_PREFIX + ".listSize";
    private static final String KEY_FINISHED = KEY_PREFIX + ".finished";
    private static final String KEY_LOADING = KEY_PREFIX + ".loading";
    private static final String KEY_LAST_CURSOR = KEY_PREFIX + ".lastCursor";

    private final ITwitterClient.ListType listType;
    private final ArrayList<List> contents = new ArrayList<List>();

    private final AtomicReference<ListSettings> listSettings = new AtomicReference<ListSettings>();

    private int listSize = 0;
    private boolean finished = false;
    private boolean loading = false;
    private Long lastCursor = null;

    private ListAdapter(final ITwitterClient.ListType listType) {
        this.listType = listType;
    }

    @Override
    public int getCount() {
        return listSize + (finished ? 0 : 1);
    }

    @Override
    public Object getItem(final int position) {
        if (position < listSize) {
            return contents.get(position);
        }

        return null;
    }

    @Override
    public long getItemId(final int position) {
        if (position < listSize) {
            return getListItem(position).getId();
        }

        return -1L;
    }

    private List getListItem(final int position) {
        return (List) getItem(position);
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        final Context context = parent.getContext();

        View view = convertView;
        final ViewHolder viewHolder;

        if (view != null) {
            viewHolder = (ViewHolder) convertView.getTag();
        } else {
            view = View.inflate(context, R.layout.item_list_row, null);

            viewHolder = new ViewHolder();
            viewHolder.txtTitle = (TextView) view.findViewById(R.id.txt_title);
            viewHolder.txtDescription = (TextView) view.findViewById(R.id.txt_description);
            viewHolder.ckboxSelected = (CheckBox) view.findViewById(R.id.chkbox_list);
            viewHolder.ckboxSelected.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                    final Long id = (Long) buttonView.getTag();
                    if (id != null) {
                        setListSelected(context, id, isChecked);
                    }
                }
            });
            view.setTag(viewHolder);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    final CheckBox checkBox = (CheckBox) v.findViewById(R.id.chkbox_list);
                    checkBox.performClick();
                }
            });
        }

        if (position >= listSize) {
            viewHolder.txtTitle.setText(context.getString(R.string.loading));
            viewHolder.txtDescription.setVisibility(View.GONE);
            viewHolder.ckboxSelected.setVisibility(View.GONE);
            viewHolder.ckboxSelected.setTag(null);

            if (!finished) {
                loadLists(context);
            }
        } else {
            final ListSettings settings = getSettings(context);

            final List item = getListItem(position);
            viewHolder.txtTitle.setText(item.getFullName());
            viewHolder.txtDescription.setText(item.getDescription());
            viewHolder.txtDescription.setVisibility(View.VISIBLE);
            viewHolder.ckboxSelected.setVisibility(View.VISIBLE);

            // 1) disable the selection saving logic in the listener
            // 2) set the current checked state
            // 3) re-enable selection saving
            viewHolder.ckboxSelected.setTag(null);
            viewHolder.ckboxSelected.setChecked(settings.isListSelected(item.getId()));
            viewHolder.ckboxSelected.setTag(item.getId());
        }

        return view;
    }

    private ListSettings getSettings(final Context context) {
        final ListSettings settings = listSettings.get();
        if (settings != null) {
            return settings;
        } else {
            final ListSettings loadedSettings = Preferences.getListSettings(context);
            listSettings.set(loadedSettings);
            return loadedSettings;
        }
    }

    private void setListSelected(final Context context, final long id, final boolean selected) {
        final ListSettings settings = getSettings(context);
        settings.setListSelected(id, selected);
        if (!Preferences.saveListSettings(context, settings)) {
            Log.e(TAG, "Failed to set list selection status");
        }
    }

    void loadLists(final Context context) {
        if (!loading) {
            loading = true;

            final AsyncTask<Long, Void, Lists> loadTask = new AsyncTask<Long, Void, Lists>() {
                @Override
                protected Lists doInBackground(final Long... params) {
                    final Long cursor = params[0];
                    return TwitterFactory.restClient()
                            .getLists(context, listType, cursor, Lists.MAX_COUNT);
                }

                @Override
                protected void onPostExecute(final Lists lists) {
                    try {
                        if (lists != null) {
                            contents.addAll(Arrays.asList(lists.getLists()));
                            listSize = contents.size();
                            lastCursor = lists.getNextCursor();
                            finished = lists.getNextCursor() == Lists.CURSOR_FINISHED;
                        } else {
                            finished = true;
                            Toast.makeText(context,
                                    context.getString(R.string.error_load_lists),
                                    Toast.LENGTH_SHORT).show();
                        }
                    } finally {
                        loading = false;

                        notifyDataSetChanged();
                    }
                }
            };
            loadTask.execute(lastCursor);
        }
    }

    void resetListData() {
        listSettings.set(null);
    }

    private static class ViewHolder {

        TextView txtTitle;
        TextView txtDescription;
        CheckBox ckboxSelected;

    }

    public void onSaveState(final Bundle bundle) {
        bundle.putString(KEY_CONTENTS, Mapper.writeObjectAsString(contents));
        bundle.putInt(KEY_LIST_SIZE, listSize);
        bundle.putBoolean(KEY_FINISHED, finished);
        bundle.putBoolean(KEY_LOADING, loading);
        if (lastCursor != null) {
            bundle.putLong(KEY_LAST_CURSOR, lastCursor);
        }
    }

    public static ListAdapter create(final ITwitterClient.ListType listType,
                                     final Bundle savedState) {
        final ListAdapter adapter = new ListAdapter(listType);
        if (savedState != null) {
            adapter.listSize = savedState.getInt(KEY_LIST_SIZE, 0);
            adapter.finished = savedState.getBoolean(KEY_FINISHED, false);
            adapter.loading = savedState.getBoolean(KEY_LOADING, false);
            adapter.lastCursor = (Long) savedState.get(KEY_LAST_CURSOR);

            final String savedContents = savedState.getString(KEY_CONTENTS, "[]");
            final List[] savedLists = Mapper.readObject(savedContents, List[].class);
            adapter.contents.addAll(Arrays.asList(savedLists));
        }
        return adapter;
    }

}
