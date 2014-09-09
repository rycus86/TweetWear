package hu.rycus.tweetwear.task;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;

import hu.rycus.tweetwear.common.api.ApiClientHelper;
import hu.rycus.tweetwear.common.api.ApiClientRunnable;
import hu.rycus.tweetwear.common.util.Constants;

public class MarkAsReadTask extends ApiClientRunnable {

    private static final String TAG = MarkAsReadTask.class.getSimpleName();

    @Override
    protected void run(final Context context, final GoogleApiClient apiClient) throws Exception {
        final String path = Constants.DataPath.MARK_AS_READ.get();
        if (!ApiClientHelper.sendMessageToConnectedNode(apiClient, path, null)) {
            Log.e(TAG, "Failed to send mark-as-read request");
        }
    }

}
