package hu.rycus.tweetwear.common.api;

import android.content.Context;

import com.google.android.gms.common.api.GoogleApiClient;

public abstract class ApiClientRunnable {

    protected abstract void run(final Context context,
                                final GoogleApiClient apiClient) throws Exception;

}
