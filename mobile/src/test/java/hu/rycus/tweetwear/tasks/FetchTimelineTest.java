package hu.rycus.tweetwear.tasks;

import android.content.Context;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.scribe.model.Token;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import hu.rycus.tweetwear.common.model.Tweet;
import hu.rycus.tweetwear.common.util.Mapper;
import hu.rycus.tweetwear.twitter.account.Account;
import hu.rycus.tweetwear.twitter.account.IAccountProvider;
import hu.rycus.tweetwear.twitter.client.ITwitterClient;
import hu.rycus.tweetwear.twitter.client.callbacks.AccessLevelCallback;
import hu.rycus.tweetwear.twitter.client.callbacks.AccessTokenCallback;
import hu.rycus.tweetwear.twitter.client.callbacks.UsernameCallback;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FetchTimelineTest {

    private static List<Tweet> tweets;

    private Context mockContext;
    private MockTwitterClient mockClient;

    @BeforeClass
    public static void loadDemoTimeline() throws IOException {
        final InputStream inputStream =
                FetchTimelineTest.class.getResourceAsStream("/demo_timeline.json");
        assertNotNull(inputStream);

        final List<Tweet> tweets = Arrays.asList(Mapper.readObject(inputStream, Tweet[].class));
        assertNotNull(tweets);

        Collections.reverse(tweets); // this will be older -> newer
        FetchTimelineTest.tweets = Collections.unmodifiableList(tweets);
    }

    @Before
    public void setupMocks() {
        mockContext = mock(Context.class);
        mockClient = new MockTwitterClient();
    }

    @Test
    public void preloadedListHasTheCorrectSize() {
        assertEquals(20, tweets.size());
    }

    @Test
    public void testMergingNewTweets() {
        final FetchTimelineTask task = new FetchTimelineTask(null, null);
        task.setTweetCountLimit(10);
        task.setExistingTweets(tweets.subList(0, 10));
        task.setNewTweets(tweets.subList(10, 12));

        final TreeSet<Tweet> sortedSet = new TreeSet<Tweet>(Collections.reverseOrder());
        sortedSet.addAll(tweets);

        final LinkedList<Tweet> sortedTweets = new LinkedList<Tweet>(sortedSet);
        final Collection<Tweet> toRemove = task.getTweetsToRemove();
        assertEquals(2, toRemove.size());
        assertEquals(sortedTweets.subList(0, 2), toRemove);
    }

    @Test
    public void testLoadingNewTweets() {
        final int limit = 10;

        final FetchTimelineTask task0 = createTask(limit);

        mockClient.addAvailableTweets(tweets.subList(0, 10));

        task0.loadNewTweets(mockContext);
        assertOrderedEquals(tweets.subList(0, 10), task0.getNewTweets());

        final FetchTimelineTask task1 = createTask(limit);
        task1.setExistingTweets(task0.getNewTweets());

        mockClient.addAvailableTweets(tweets.subList(10, 15));

        task1.loadNewTweets(mockContext);
        assertOrderedEquals(tweets.subList(10, 15), task1.getNewTweets());
        assertOrderedEquals(tweets.subList(0, 5), task1.getTweetsToRemove());

        final FetchTimelineTask task2 = createTask(limit);
        final Set<Tweet> combinedTweets = new HashSet<Tweet>();
        combinedTweets.addAll(task1.getNewTweets());
        combinedTweets.addAll(task1.getExistingTweets());
        combinedTweets.removeAll(task1.getTweetsToRemove());
        task2.setExistingTweets(combinedTweets);

        assertEquals(limit, combinedTweets.size());

        mockClient.addAvailableTweets(tweets.subList(15, 20));

        task2.loadNewTweets(mockContext);
        assertOrderedEquals(tweets.subList(15, 20), task2.getNewTweets());
        assertOrderedEquals(tweets.subList(5, 10), task2.getTweetsToRemove());
    }

    @Test
    public void testLoadingNewTweetsLessThanLimit() {
        final int limit = 10;

        final FetchTimelineTask task0 = createTask(limit);

        mockClient.addAvailableTweets(tweets.subList(0, 7));

        task0.loadNewTweets(mockContext);
        assertOrderedEquals(tweets.subList(0, 7), task0.getNewTweets());

        final FetchTimelineTask task1 = createTask(limit);
        task1.setExistingTweets(task0.getNewTweets());

        mockClient.addAvailableTweets(tweets.subList(7, 12));

        task1.loadNewTweets(mockContext);
        assertOrderedEquals(tweets.subList(7, 12), task1.getNewTweets());
        assertOrderedEquals(tweets.subList(0, 2), task1.getTweetsToRemove());
    }

    @Test
    public void testLoadingRemovesNothing() {
        final int limit = 10;

        final FetchTimelineTask task = createTask(limit);
        task.setExistingTweets(tweets.subList(0, 2));

        mockClient.addAvailableTweets(tweets.subList(0, 5));

        task.loadNewTweets(mockContext);
        assertOrderedEquals(tweets.subList(2, 5), task.getNewTweets());
        assertOrderedEquals(Collections.<Tweet>emptyList(), task.getTweetsToRemove());
    }

    @Test
    public void testLoadingRemovesAndAddsNothing() {
        final int limit = 10;

        final FetchTimelineTask task = createTask(limit);
        task.setExistingTweets(tweets.subList(0, 5));

        mockClient.addAvailableTweets(tweets.subList(0, 5));

        task.loadNewTweets(mockContext);
        assertOrderedEquals(Collections.<Tweet>emptyList(), task.getNewTweets());
        assertOrderedEquals(Collections.<Tweet>emptyList(), task.getTweetsToRemove());
    }

    private <T extends Comparable<?>> void assertOrderedEquals(
            final Collection<T> collection1, final Collection<T> collection2) {
        assertEquals(
                String.format("Different collections, sizes: %d and %d",
                        collection1.size(), collection2.size()),
                new TreeSet<T>(collection1), new TreeSet<T>(collection2));
    }

    private FetchTimelineTask createTask(final int tweetCountLimit) {
        final IAccountProvider mockProvider = mock(IAccountProvider.class);
        final Account mockAccount = mock(Account.class);

        when(mockProvider.getAccounts(mockContext)).thenReturn(Collections.singleton(mockAccount));

        final FetchTimelineTask task = new FetchTimelineTask(mockProvider, mockClient);
        task.setTweetCountLimit(tweetCountLimit);

        return task;
    }

    private class MockTwitterClient implements ITwitterClient {

        private final TreeSet<Tweet> availableTweets = new TreeSet<Tweet>();

        private void addAvailableTweets(final Collection<Tweet> tweets) {
            availableTweets.addAll(tweets);
        }

        @Override
        public Tweet[] getTimeline(final Token accessToken, final Integer count,
                                   final Long sinceId, final Long maxId, final Boolean trimUser,
                                   final Boolean excludeReplies, final Boolean contributorDetails,
                                   final Boolean includeEntities) {

            final List<Tweet> tweets = new LinkedList<Tweet>();
            for (final Tweet tweet : availableTweets) {
                if (maxId != null && maxId <= tweet.getId()) continue;
                if (sinceId != null && sinceId >= tweet.getId()) continue;

                tweets.add(tweet);

                if (count != null && tweets.size() >= count) break;
            }
            return tweets.toArray(new Tweet[tweets.size()]);
        }

        @Override
        public Tweet retweet(final Token accessToken, final long id, final Boolean trimUser) {
            return null;
        }

        @Override
        public Tweet favorite(final Token accessToken, final long id, final Boolean includeEntities) {
            return null;
        }

        @Override
        public void authorize(final Context context) {
            throw new UnsupportedOperationException("This mock does not support authorization");
        }

        @Override
        public void processAccessToken(final Context context, final String oauthVerifier,
                                       final AccessTokenCallback callback) {
            throw new UnsupportedOperationException("This mock does not support authorization");
        }

        @Override
        public void loadUsername(final Context context, final UsernameCallback callback) {
            throw new UnsupportedOperationException("This mock does not support authorization");
        }

        @Override
        public void checkAccessLevel(final Context context, final AccessLevelCallback callback) {
            throw new UnsupportedOperationException("This mock does not support authorization");
        }

    }

}
