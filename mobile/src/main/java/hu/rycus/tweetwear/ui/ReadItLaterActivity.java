package hu.rycus.tweetwear.ui;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import hu.rycus.tweetwear.R;
import hu.rycus.tweetwear.ril.ReadItLater;
import hu.rycus.tweetwear.ui.ril.ListFragment;

public class ReadItLaterActivity extends Activity {

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
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_read_later, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_delete_all: {
                onDeleteSavedReadLaterPages();
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void onDeleteSavedReadLaterPages() {
        ReadItLater.deleteAll(this);
        finish();
    }

}
