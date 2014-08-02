package hu.rycus.tweetwear.task;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;

import hu.rycus.tweetwear.common.api.ApiClientHelper;
import hu.rycus.tweetwear.common.api.ApiClientRunnable;
import hu.rycus.tweetwear.common.util.Constants;

public class FavoriteTask extends ApiClientRunnable {

    private static final String TAG = FavoriteTask.class.getSimpleName();

    private final long tweetId;

    public FavoriteTask(final long tweetId) {
        this.tweetId = tweetId;
    }

    @Override
    protected void run(final Context context, final GoogleApiClient apiClient) throws Exception {
        final String path = Constants.DataPath.FAVORITE.withId(tweetId);
        if (!ApiClientHelper.sendMessageToConnectedNode(apiClient, path, null)) {
            Log.e(TAG, "Failed to send favorite");
        }
    }

}
