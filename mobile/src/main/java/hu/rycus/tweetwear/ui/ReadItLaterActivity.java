package hu.rycus.tweetwear.ui;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import hu.rycus.tweetwear.R;
import hu.rycus.tweetwear.ril.ReadItLater;
import hu.rycus.tweetwear.ui.ril.ListFragment;

public class ReadItLaterActivity extends Activity {

    private static final String TAG = ReadItLaterActivity.class.getSimpleName();

    private static final String KEY_ARCHIVE_CHECKED =
            ReadItLaterActivity.class.getCanonicalName() + ".archive";

    private boolean savedArchiveState;
    private MenuItem archiveItem;

    public static void start(final Context context) {
        context.startActivity(new Intent(context, ReadItLaterActivity.class));
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_it_later);

        final Fragment existing = getFragmentManager().findFragmentById(R.id.container);
        if (existing == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new ListFragment())
                    .commit();
        }

        if (savedInstanceState != null) {
            savedArchiveState = savedInstanceState.getBoolean(KEY_ARCHIVE_CHECKED, false);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_ARCHIVE_CHECKED, archiveItem.isChecked());
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_read_later, menu);

        archiveItem = menu.findItem(R.id.menu_archive_toggle);
        archiveItem.setChecked(savedArchiveState);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_archive_toggle: {
                final boolean archive = !item.isChecked();
                setArchiveItemState(archive);
                return true;
            }
            case R.id.menu_delete_all: {
                onDeleteSavedReadLaterPages();
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    public void setArchiveItemState(final boolean archive) {
        final ListFragment fragment = findListFragment();
        if (fragment != null) {
            if (archive) {
                new AsyncTask<Void, Void, Integer>() {
                    @Override
                    protected Integer doInBackground(final Void... params) {
                        return ReadItLater.countArchives(ReadItLaterActivity.this);
                    }

                    @Override
                    protected void onPostExecute(final Integer count) {
                        if (count > 0) {
                            archiveItem.setChecked(true);
                            fragment.setArchive(true);
                        } else {
                            Toast.makeText(
                                    ReadItLaterActivity.this,
                                    getString(R.string.no_archives),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }.execute();
            } else {
                archiveItem.setChecked(false);
                fragment.setArchive(false);
            }
        }
    }

    private void onDeleteSavedReadLaterPages() {
        if (archiveItem.isChecked()) {
            ReadItLater.deleteAllArchives(this);
        } else {
            ReadItLater.deleteAll(this);
        }

        final ListFragment fragment = findListFragment();
        if (fragment != null) {
            fragment.animateListClearing();
        } else {
            Log.d(TAG, "Fragment not found, simply finishing");
            finish();
        }
    }

    private ListFragment findListFragment() {
        return (ListFragment) getFragmentManager().findFragmentById(R.id.container);
    }

}
