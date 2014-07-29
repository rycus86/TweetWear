package hu.rycus.tweetwear.tasks;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataItemBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

import hu.rycus.tweetwear.common.model.Tweet;
import hu.rycus.tweetwear.common.util.Constants;
import hu.rycus.tweetwear.common.util.Mapper;
import hu.rycus.tweetwear.twitter.account.Account;
import hu.rycus.tweetwear.twitter.account.IAccountProvider;
import hu.rycus.tweetwear.twitter.client.ITwitterClient;

public class FetchTimelineTask implements Runnable {

    private static final String TAG = FetchTimelineTask.class.getSimpleName();

    private static final int DEFAULT_TWEET_LIMIT = 10;

    private final Context context;
    private final GoogleApiClient apiClient;
    private final IAccountProvider accountProvider;
    private final ITwitterClient client;

    private Collection<Tweet> existingTweets;
    private Collection<Tweet> newTweets;

    private Long sinceId;

    private int tweetCountLimit = DEFAULT_TWEET_LIMIT;

    private final ResultCallback<DataApi.DataItemResult> sendCallback =
            new ResultCallback<DataApi.DataItemResult>() {
                @Override
                public void onResult(final DataApi.DataItemResult dataItemResult) {
                    if (!dataItemResult.getStatus().isSuccess()) {
                        Log.e(TAG, String.format("Failed to send message [%s]: %s",
                                dataItemResult.getDataItem().getUri(),
                                dataItemResult.getStatus()));
                    }
                }
            };

    public FetchTimelineTask(final Context context,
                             final GoogleApiClient apiClient,
                             final IAccountProvider accountProvider,
                             final ITwitterClient client) {
        this.context = context;
        this.apiClient = apiClient;
        this.accountProvider = accountProvider;
        this.client = client;
    }

    @Override
    public void run() {
        try {
            if (!checkIfApiClientIsConnected()) return;

            loadExistingTweets();
            loadNewTweets();

            if (!newTweets.isEmpty()) {
                for (final Tweet tweet : newTweets) {
                    sendTweet(tweet);
                }

                final Collection<Tweet> toRemove = getTweetsToRemove();
                if (!toRemove.isEmpty()) {
                    Log.d(TAG, String.format("About to remove %d tweets, existing: %d, new: %d",
                            toRemove.size(), existingTweets.size(), newTweets.size()));

                    for (final Tweet tweet : toRemove) {
                        removeOldTweet(tweet);
                    }
                }
            } else {
                Log.d(TAG, "There are no tweets to send at this time");
            }
        } finally {
            apiClient.disconnect();
        }
    }

    protected boolean checkIfApiClientIsConnected() {
        if (apiClient.isConnected()) {
            return true;
        } else {
            Log.e(TAG, "Google API client is not connected");
            return false;
        }
    }

    protected void loadNewTweets() {
        final TreeSet<Tweet> tweets = new TreeSet<Tweet>();
        for (final Account account : accountProvider.getAccounts(context)) {
            final Tweet[] timelineTweets = client.getTimeline(
                    account.getAccessToken(),
                    tweetCountLimit, sinceId, null, null, null, null, null);

            Log.d(TAG, String.format("Tweets retrieved for account (%s): %d",
                    account.getUsername(), timelineTweets.length));

            tweets.addAll(Arrays.asList(timelineTweets));
        }

        setNewTweets(tweets);
    }

    protected void sendTweet(final Tweet tweet) {
        if (!checkIfApiClientIsConnected()) return;

        final PutDataMapRequest mapRequest = PutDataMapRequest.create(
                Constants.DataPath.TWEETS.withId(tweet.getId()));
        mapRequest.getDataMap().putByteArray(
                Constants.DataKey.CONTENT.get(),
                Mapper.writeObject(tweet));
        final PutDataRequest request = mapRequest.asPutDataRequest();
        Wearable.DataApi
                .putDataItem(apiClient, request)
                .setResultCallback(sendCallback);
    }

    protected void loadExistingTweets() {
        if (!checkIfApiClientIsConnected()) {
            setExistingTweets(Collections.<Tweet>emptyList());
        }

        final DataItemBuffer buffer = Wearable.DataApi.getDataItems(apiClient).await();
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
                setExistingTweets(tweets);
            } else {
                setExistingTweets(Collections.<Tweet>emptyList());
            }
        } finally {
            buffer.release();
        }
    }

    protected Collection<Tweet> getTweetsToRemove() {
        if (newTweets.isEmpty()) {
            return Collections.emptyList();
        } else {
            final List<Tweet> candidates = new ArrayList<Tweet>(new TreeSet<Tweet>(existingTweets));
            Collections.reverse(candidates);
            final int toRemove = existingTweets.size() + newTweets.size() - tweetCountLimit;
            return candidates.subList(0, Math.max(0, toRemove));
        }
    }

    protected void removeOldTweet(final Tweet tweet) {
        if (!checkIfApiClientIsConnected()) return;

        Wearable.DataApi.deleteDataItems(apiClient,
                new Uri.Builder()
                        .scheme(PutDataRequest.WEAR_URI_SCHEME)
                        .path(Constants.DataPath.TWEETS.withId(tweet.getId()))
                        .build())
                .setResultCallback(new ResultCallback<DataApi.DeleteDataItemsResult>() {
            @Override
            public void onResult(final DataApi.DeleteDataItemsResult deleteDataItemsResult) {
                if (!deleteDataItemsResult.getStatus().isSuccess()) {
                    Log.w(TAG, String.format("Failed to delete Tweet #%d", tweet.getId()));
                }
            }
        });
    }

    protected void setTweetCountLimit(final int tweetCountLimit) {
        this.tweetCountLimit = tweetCountLimit;
    }

    protected Collection<Tweet> getNewTweets() {
        return newTweets;
    }

    protected void setNewTweets(final Collection<Tweet> tweets) {
        this.newTweets = tweets;
    }

    protected Collection<Tweet> getExistingTweets() {
        return existingTweets;
    }

    protected void setExistingTweets(final Collection<Tweet> tweets) {
        this.existingTweets = tweets;
        checkSinceIdInTweets(tweets);
    }

    protected void checkSinceIdInTweets(final Collection<Tweet> tweets) {
        for (final Tweet tweet : tweets) {
            checkSinceId(tweet);
        }
    }

    protected void checkSinceId(final Tweet tweet) {
        if (sinceId == null || sinceId < tweet.getId()) {
            sinceId = tweet.getId();
        }
    }

}
