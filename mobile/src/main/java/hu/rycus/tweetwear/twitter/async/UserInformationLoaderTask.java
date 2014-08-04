package hu.rycus.tweetwear.twitter.async;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.scribe.model.Token;
import org.scribe.oauth.OAuthService;

import hu.rycus.tweetwear.common.model.User;
import hu.rycus.tweetwear.preferences.Preferences;
import hu.rycus.tweetwear.twitter.client.ITwitterClient;
import hu.rycus.tweetwear.twitter.client.RequestBuilder;
import hu.rycus.tweetwear.twitter.client.callbacks.UsernameCallback;

public class UserInformationLoaderTask extends AsyncTask<Token, Void, User> {

    private static final String TAG = UserInformationLoaderTask.class.getSimpleName();

    private final Context context;
    private final OAuthService service;
    private final UsernameCallback callback;

    public UserInformationLoaderTask(final Context context, final OAuthService service,
                                     final UsernameCallback callback) {
        this.context = context;
        this.service = service;
        this.callback = callback;
    }

    @Override
    protected User doInBackground(final Token... params) {
        final User savedUser = getSavedUserInformation();
        if (savedUser != null) {
            return savedUser;
        }

        try {
            final Token accessToken = params[0];
            return RequestBuilder
                    .get(ITwitterClient.Uri.VERIFY_CREDENTIALS.get())
                    .send(service, accessToken)
                    .respond(User.class);
        } catch (Exception ex) {
            Log.e(TAG, "Failed to load user information", ex);
        }

        return null;
    }

    @Override
    protected void onPostExecute(final User user) {
        if (user == null) {
            if (callback != null) {
                callback.onUsernameLoadError();
            }
            return;
        }

        Preferences.saveUserId(context, user.getId());

        final boolean nameSaved = Preferences.saveUserName(context, user.getScreenName());
        if (callback != null) {
            if (nameSaved) {
                callback.onUsernameLoaded(user.getScreenName());
            } else {
                callback.onUsernameLoadError();
            }
        }
    }

    private User getSavedUserInformation() {
        final long savedUserId = Preferences.getUserId(context);
        final String savedUsername = Preferences.getUserName(context);
        if (savedUserId >= 0L && savedUsername != null) {
            final User savedUser = new User();
            savedUser.setId(savedUserId);
            savedUser.setScreenName(savedUsername);
            return savedUser;
        }

        return null;
    }

}
