package hu.rycus.tweetwear.ui;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;

import hu.rycus.tweetwear.R;
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

}
