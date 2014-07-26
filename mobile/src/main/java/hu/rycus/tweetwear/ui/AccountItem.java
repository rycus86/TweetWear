package hu.rycus.tweetwear.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import hu.rycus.tweetwear.R;
import hu.rycus.tweetwear.preferences.Preferences;
import hu.rycus.tweetwear.twitter.TwitterFactory;
import hu.rycus.tweetwear.twitter.client.callbacks.UsernameCallback;

public class AccountItem extends SettingsItem<String> implements UsernameCallback {

    private Context context;
    private boolean contentLoaded = false;
    private String content;

    public AccountItem(final Context context) {
        this.context = context;

        final String existingUsername = Preferences.getUserName(context);
        if (existingUsername != null) {
            this.content = formatUsername(existingUsername);
            this.contentLoaded = true;
        } else {
            this.content = context.getString(R.string.loading_account);
        }
    }

    @Override
    protected void initialize() {
        if (!contentLoaded) {
            TwitterFactory.createClient().loadUsername(context, this);
        }
    }

    @Override
    public String getItem() {
        if (contentLoaded) {
            return content;
        } else {
            return null;
        }
    }

    @Override
    protected View createView(final Context context, final ViewGroup parent) {
        final View view = LayoutInflater.from(context).inflate(R.layout.item_account, parent, false);
        final TextView textView = (TextView) view.findViewById(R.id.txt_account);
        textView.setText(content);
        return view;
    }

    @Override
    public void onUsernameLoaded(final String name) {
        content = formatUsername(name);
        getAdapter().notifyDataSetChanged();
    }

    @Override
    public void onUsernameLoadError() {
        Toast.makeText(context, context.getString(R.string.error_account), Toast.LENGTH_SHORT).show();
    }

    private String formatUsername(final String name) {
        return String.format("@%s", name);
    }

}
