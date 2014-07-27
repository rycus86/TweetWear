package hu.rycus.tweetwear.tasks;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataItemBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import hu.rycus.tweetwear.common.model.Tweet;
import hu.rycus.tweetwear.common.util.Constants;
import hu.rycus.tweetwear.common.util.Mapper;

public class ClearExistingTweetsTask extends AsyncTask<Void, Void, Void> {

    private static final String TAG = ClearExistingTweetsTask.class.getSimpleName();

    private GoogleApiClient client;

    public ClearExistingTweetsTask(final Context context) {
        this.client = new GoogleApiClient.Builder(context)
                .addApi(Wearable.API)
                .build();
    }

    @Override
    protected Void doInBackground(final Void... params) {
        client.blockingConnect();

        try {
            final Collection<Tweet> tweets = getExistingTweets();
            for (final Tweet tweet : tweets) {
                removeTweet(tweet);
            }
        } finally {
            client.disconnect();
        }

        return null;
    }

    private Collection<Tweet> getExistingTweets() {
        final DataItemBuffer buffer = Wearable.DataApi.getDataItems(client).await();
        try {
            if (buffer.getStatus().isSuccess()) {
                final List<Tweet> tweets = new ArrayList<Tweet>(buffer.getCount());
                for (final DataItem item : FreezableUtils.freezeIterable(buffer)) {
                    final DataMapItem mapItem = DataMapItem.fromDataItem(item);
                    final String path = mapItem.getUri().getPath();
                    if (path.matches(Constants.DataPath.TWEETS.pattern())) {
                        final byte[] content = mapItem.getDataMap().getByteArray(
                                Constants.DataKey.CONTENT.get());
                        final Tweet tweet = Mapper.readObject(content, Tweet.class);
                        tweets.add(tweet);
                    }
                }
                return tweets;
            } else {
                return Collections.emptyList();
            }
        } finally {
            buffer.release();
        }
    }

    private void removeTweet(final Tweet tweet) {
        final Uri uri = new Uri.Builder()
                .scheme(PutDataRequest.WEAR_URI_SCHEME)
                .path(Constants.DataPath.TWEETS.withId(tweet.getId()))
                .build();
        Wearable.DataApi.deleteDataItems(client, uri)
                .setResultCallback(new ResultCallback<DataApi.DeleteDataItemsResult>() {
                    @Override
                    public void onResult(final DataApi.DeleteDataItemsResult deleteDataItemsResult) {
                        if (!deleteDataItemsResult.getStatus().isSuccess()) {
                            Log.w(TAG, String.format(
                                    "Failed to delete Tweet: %d", tweet.getId()));
                        } else {
                            Log.w(TAG, String.format("%d tweet deleted: %d",
                                    deleteDataItemsResult.getNumDeleted(),
                                    tweet.getId()));
                        }
                    }
                });
    }

}
