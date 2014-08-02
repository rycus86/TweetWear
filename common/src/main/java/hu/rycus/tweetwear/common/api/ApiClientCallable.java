package hu.rycus.tweetwear.common.api;

import android.content.Context;

import com.google.android.gms.common.api.GoogleApiClient;

public abstract class ApiClientCallable<T> {

    protected abstract T call(final Context context,
                              final GoogleApiClient apiClient) throws Exception;

}
