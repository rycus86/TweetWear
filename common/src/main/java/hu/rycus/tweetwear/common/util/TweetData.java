package hu.rycus.tweetwear.common.util;

import android.net.Uri;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataItemBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import hu.rycus.tweetwear.common.model.Tweet;
import hu.rycus.tweetwear.common.model.User;
import hu.rycus.tweetwear.common.model.entities.Entities;
import hu.rycus.tweetwear.common.model.entities.Hashtag;
import hu.rycus.tweetwear.common.model.entities.Media;
import hu.rycus.tweetwear.common.model.entities.Size;
import hu.rycus.tweetwear.common.model.entities.Sizes;
import hu.rycus.tweetwear.common.model.entities.Url;
import hu.rycus.tweetwear.common.model.entities.UserMention;

public final class TweetData {

    private static final String TAG = TweetData.class.getSimpleName();

    private final Tweet tweet;

    private TweetData(final Tweet tweet) {
        this.tweet = tweet;
    }

    public static TweetData of(final Tweet tweet) {
        return new TweetData(tweet);
    }

    public static TweetData forId(final long id) {
        final Tweet tweet = new Tweet();
        tweet.setId(id);
        return TweetData.of(tweet);
    }

    public static Tweet parse(final byte[] data) {
        return Mapper.readObject(data, Tweet.class);
    }

    public static Tweet parse(final String json) {
        return Mapper.readObject(json, Tweet.class);
    }

    public static Collection<Tweet> loadAll(final GoogleApiClient apiClient) {
        final DataItemBuffer buffer = Wearable.DataApi.getDataItems(apiClient).await();
        try {
            if (buffer.getStatus().isSuccess()) {
                final List<Tweet> tweets = new ArrayList<Tweet>(buffer.getCount());
                for (final DataItem item : FreezableUtils.freezeIterable(buffer)) {
                    final DataMapItem mapItem = DataMapItem.fromDataItem(item);
                    final String path = mapItem.getUri().getPath();
                    if (Constants.DataPath.TWEETS.matches(path)) {
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

    public byte[] serialize() {
        if (tweet != null) {
            checkIncompleteTweet();
            return Mapper.writeObject(tweet);
        } else {
            return null;
        }
    }

    public String toJson() {
        if (tweet != null) {
            checkIncompleteTweet();
            return Mapper.writeObjectAsString(tweet);
        } else {
            return null;
        }
    }

    public String getTitle() {
        checkIncompleteTweet();

        if (tweet.getRetweetedStatus() != null) {
            return "@" + tweet.getRetweetedStatus().getUser().getScreenName();
        } else {
            return "@" + tweet.getUser().getScreenName();
        }
    }

    public String getTimestamp() {
        checkIncompleteTweet();

        final DateFormat dateFormat =
                DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
        return dateFormat.format(tweet.getCreatedAt());
    }

    public String toFormattedHtml() {
        checkIncompleteTweet();

        String content = tweet.getText();
        if (tweet.getEntities() != null) {
            content = processEntities(content);
        }
        return content;
    }

    private String processEntities(final String originalContent) {
        checkIncompleteTweet();

        final Entities entities = tweet.getEntities();

        String content = originalContent;

        if (entities.getHashtags() != null) {
            for (final Hashtag hashtag : entities.getHashtags()) {
                content = content.replace("#" + hashtag.getText(),
                        String.format("<i>#<b>%s</b></i>", hashtag.getText()));
            }
        }

        if (entities.getUrls() != null) {
            for (final Url url : entities.getUrls()) {
                content = content.replace(url.getUrl(),
                        String.format("<font color='#0099FF'><i>%s</i></font>",
                                url.getDisplayUrl()));
            }
        }

        if (entities.getUserMentions() != null) {
            for (final UserMention mention : entities.getUserMentions()) {
                content = content.replace(
                        String.format("@%s", mention.getScreenName()),
                        String.format("<font color='#0033CC'><i>@%s</i></font>",
                                mention.getName()));
            }
        }

        if (entities.getMedia() != null) {
            boolean showMediaIndicator = false;

            for (final Media media : entities.getMedia()) {
                content = content.replace(media.getUrl(), "");
                showMediaIndicator = true;
            }

            if (showMediaIndicator) {
                content += " <small><i>{x}</i></small>";
            }
        }

        return content;
    }

    public boolean sendBlocking(final GoogleApiClient apiClient) {
        return send(apiClient).await().getStatus().isSuccess();
    }

    public void sendAsync(final GoogleApiClient apiClient) {
        send(apiClient).setResultCallback(sendCallback);
    }

    private PendingResult<DataApi.DataItemResult> send(final GoogleApiClient apiClient) {
        checkIncompleteTweet();

        final PutDataMapRequest mapRequest = PutDataMapRequest.create(
                Constants.DataPath.TWEETS.withId(tweet.getId()));
        mapRequest.getDataMap().putByteArray(
                Constants.DataKey.CONTENT.get(),
                Mapper.writeObject(tweet));
        final PutDataRequest request = mapRequest.asPutDataRequest();
        return Wearable.DataApi.putDataItem(apiClient, request);
    }

    public boolean deleteBlocking(final GoogleApiClient apiClient) {
        return delete(apiClient).await().getStatus().isSuccess();
    }

    public void deleteAsync(final GoogleApiClient apiClient) {
        delete(apiClient).setResultCallback(deleteCallback);
    }

    private PendingResult<DataApi.DeleteDataItemsResult> delete(final GoogleApiClient apiClient) {
        return Wearable.DataApi.deleteDataItems(apiClient,
                new Uri.Builder()
                        .scheme(PutDataRequest.WEAR_URI_SCHEME)
                        .path(Constants.DataPath.TWEETS.withId(tweet.getId()))
                        .build());
    }

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

    private final ResultCallback<DataApi.DeleteDataItemsResult> deleteCallback =
            new ResultCallback<DataApi.DeleteDataItemsResult>() {
        @Override
        public void onResult(final DataApi.DeleteDataItemsResult dataItemsResult) {
            if (!dataItemsResult.getStatus().isSuccess()) {
                Log.w(TAG, String.format("Failed to delete Tweet #%d", tweet.getId()));
            }
        }
    };

    private void checkIncompleteTweet() {
        if (tweet instanceof IncompleteTweet) {
            throw new IllegalArgumentException("Can not process an incomplete tweet");
        }
    }

    public static Tweet demo(final long id) {
        final Tweet tweet = new Tweet();
        final User user = new User();
        user.setId(1234);
        user.setName("User name");
        user.setScreenName("ScreenName");
        tweet.setUser(user);
        tweet.setCreatedAt(new Date());
        tweet.setId(id);

        final Media media1 = new Media();
        media1.setId(12345);
        media1.setUrl("t.co/abcd");
        media1.setDisplayUrl("t.co/image");
        media1.setMediaUrl("http://pbs.twimg.com/media/Bxn2oniCQAAswZr.jpg");
        media1.setMediaUrlHttps("https://pbs.twimg.com/media/Bxn2oniCQAAswZr.jpg");
        final Media media2 = new Media();
        media2.setId(12350);
        media2.setUrl("t.co/efgh");
        media2.setDisplayUrl("t.co/image-2nd");
        media2.setMediaUrl("http://pbs.twimg.com/media/ByA3SlCCYAEv7iS.png");
        media2.setMediaUrlHttps("https://pbs.twimg.com/media/ByA3SlCCYAEv7iS.png");
        final Size size = new Size();
        size.setWidth(280);
        size.setHeight(280);
        size.setResize(Size.Resize.FIT);
        final Sizes sizes = new Sizes();
        sizes.setSmall(size);
        media1.setSizes(sizes);
        media2.setSizes(sizes);

        final Url url1 = new Url();
        url1.setUrl("goog.le");
        url1.setDisplayUrl("google.com");
        url1.setExpandedUrl("http://google.com");
        final Url url2 = new Url();
        url2.setUrl("twitt.er");
        url2.setDisplayUrl("twitter.com");
        url2.setExpandedUrl("http://twitter.com");

        final Entities entities = new Entities();
        entities.setMedia(new Media[]{media1, media2});
        entities.setUrls(new Url[] {url1, url2});
        tweet.setEntities(entities);

        tweet.setText(String.format("Test demo tweet %s %s %s %s",
                        url1.getUrl(), url2.getUrl(),
                        media1.getUrl(), media2.getUrl()));

        return tweet;
    }

    private static class IncompleteTweet extends Tweet {
    }

}
