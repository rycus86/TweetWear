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
import hu.rycus.tweetwear.twitter.client.callbacks.AccessLevelCallback;
import hu.rycus.tweetwear.twitter.client.callbacks.UsernameCallback;

public class AccountItem extends SettingsItem<String>
        implements UsernameCallback, AccessLevelCallback {

    private Context context;
    private boolean usernameLoaded = false;
    private String username;
    private boolean newAccessTokenRequired = false;
    private boolean accessLevelChecked = false;

    public AccountItem(final Context context) {
        this.context = context;

        final String existingUsername = Preferences.getUserName(context);
        if (existingUsername != null) {
            this.username = formatUsername(existingUsername);
            this.usernameLoaded = true;
        } else {
            this.username = context.getString(R.string.loading_account);
        }

        if (Preferences.hasDesiredAccessLevel(context)) {
            this.newAccessTokenRequired = false;
            this.accessLevelChecked = true;
        } else {
            this.accessLevelChecked = false;
        }
    }

    @Override
    protected void initialize() {
        if (!usernameLoaded) {
            TwitterFactory.createClient().loadUsername(context, this);
        }

        if (!accessLevelChecked) {
            TwitterFactory.createClient().checkAccessLevel(context, this);
        }
    }

    @Override
    public String getItem() {
        if (usernameLoaded) {
            return username;
        } else {
            return null;
        }
    }

    @Override
    protected View createView(final Context context, final ViewGroup parent) {
        final View view = LayoutInflater.from(context).inflate(R.layout.item_account, parent, false);
        final TextView txtAccount = (TextView) view.findViewById(R.id.txt_account);
        txtAccount.setText(username);
        final TextView txtNotice = (TextView) view.findViewById(R.id.txt_notice);
        txtNotice.setVisibility(newAccessTokenRequired ? View.VISIBLE : View.GONE);
        return view;
    }

    @Override
    protected void onClick(final Context context) {
        if (newAccessTokenRequired) {
            TwitterFactory.createClient().authorize(context);
        }
    }

    @Override
    public void onUsernameLoaded(final String name) {
        username = formatUsername(name);
        usernameLoaded = true;

        getAdapter().notifyDataSetChanged();
    }

    @Override
    public void onUsernameLoadError() {
        Toast.makeText(context, context.getString(R.string.error_account), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNewAccessTokenRequired() {
        newAccessTokenRequired = true;
        accessLevelChecked = true;

        getAdapter().notifyDataSetChanged();
    }

    private String formatUsername(final String name) {
        return String.format("@%s", name);
    }

}
