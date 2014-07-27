package hu.rycus.tweetwear;

import android.app.Notification;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataItemBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import hu.rycus.tweetwear.common.model.Tweet;
import hu.rycus.tweetwear.common.util.Constants;
import hu.rycus.tweetwear.common.util.Mapper;
import hu.rycus.tweetwear.notification.NotificationHandler;

public class TweetWearService extends WearableListenerService {

    private static final String TAG = TweetWearService.class.getSimpleName();

    private ScheduledExecutorService executor;
    private ScheduledFuture<?> notifyFuture;

    @Override
    public void onCreate() {
        super.onCreate();
        executor = Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    public void onDestroy() {
        executor.shutdown();
        super.onDestroy();
    }

    @Override
    public void onDataChanged(final DataEventBuffer dataEvents) {
        Log.d(TAG, String.format("Data changed at %d", System.currentTimeMillis()));
        super.onDataChanged(dataEvents);

        final ArrayList<DataEvent> events = FreezableUtils.freezeIterable(dataEvents);
        for (final DataEvent event : events) {
            if (isNewTweetEvent(event)) {
                scheduleNotification();
            }
        }
    }

    private boolean isNewTweetEvent(final DataEvent event) {
        return event.getType() == DataEvent.TYPE_CHANGED &&
               event.getDataItem().getUri().getPath().matches(Constants.DataPath.TWEETS.pattern());
    }

    private void scheduleNotification() {
        final ScheduledFuture<?> previousFuture = notifyFuture;
        if (previousFuture != null) {
            previousFuture.cancel(false);
        }

        notifyFuture = executor.schedule(new NotifyTask(), 1, TimeUnit.SECONDS);
    }

    private class NotifyTask implements Runnable {

        private GoogleApiClient apiClient;

        @Override
        public void run() {
            try {
                Log.d(TAG, String.format("Notification task running at %d",
                        System.currentTimeMillis()));

                apiClient = new GoogleApiClient.Builder(TweetWearService.this)
                                .addApi(Wearable.API).build();
                try {
                    apiClient.blockingConnect();

                    final Collection<Tweet> existingTweets = getExistingTweets();
                    final List<Tweet> tweets = new ArrayList<Tweet>(existingTweets);
                    Collections.sort(tweets, Collections.reverseOrder());

                    Log.d(TAG, String.format("%d tweets found", tweets.size()));

                    final NotificationManagerCompat manager =
                            NotificationManagerCompat.from(TweetWearService.this);

                    int id = 100;
                    for (final Tweet tweet : tweets) {
                        Log.d(TAG, String.format("Notifying for tweet: %s - %s",
                                tweet.getUser().getScreenName(), tweet.getText()));
                        final Notification notification =
                                NotificationHandler.buildTweetNotification(
                                        TweetWearService.this, tweet);
                        manager.notify(id++, notification);
                    }

                    manager.notify(id, NotificationHandler.buildSummaryNotification(
                            TweetWearService.this, tweets.size()));
                } finally {
                    apiClient.disconnect();
                }
            } finally {
                notifyFuture = null;
            }
        }

        private Collection<Tweet> getExistingTweets() {
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
                    return tweets;
                } else {
                    return Collections.emptyList();
                }
            } finally {
                buffer.release();
            }
        }

    }

}
