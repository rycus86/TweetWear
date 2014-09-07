package hu.rycus.tweetwear.tasks;

import android.content.Context;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
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
        mockClient = MockTwitterClientHandler.create();
    }

    @Test
    public void preloadedListHasTheCorrectSize() {
        assertEquals(20, tweets.size());
    }

    @Test
    public void testMergingNewTweets() {
        final FetchTweetsTask task = new FetchTweetsTask(null, null);
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

        final FetchTweetsTask task0 = createTask(limit);

        mockClient.addAvailableTweets(tweets.subList(0, 10));

        task0.loadNewTweets(mockContext);
        assertOrderedEquals(tweets.subList(0, 10), task0.getNewTweets());

        final FetchTweetsTask task1 = createTask(limit);
        task1.setExistingTweets(task0.getNewTweets());

        mockClient.addAvailableTweets(tweets.subList(10, 15));

        task1.loadNewTweets(mockContext);
        assertOrderedEquals(tweets.subList(10, 15), task1.getNewTweets());
        assertOrderedEquals(tweets.subList(0, 5), task1.getTweetsToRemove());

        final FetchTweetsTask task2 = createTask(limit);
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

        final FetchTweetsTask task0 = createTask(limit);

        mockClient.addAvailableTweets(tweets.subList(0, 7));

        task0.loadNewTweets(mockContext);
        assertOrderedEquals(tweets.subList(0, 7), task0.getNewTweets());

        final FetchTweetsTask task1 = createTask(limit);
        task1.setExistingTweets(task0.getNewTweets());

        mockClient.addAvailableTweets(tweets.subList(7, 12));

        task1.loadNewTweets(mockContext);
        assertOrderedEquals(tweets.subList(7, 12), task1.getNewTweets());
        assertOrderedEquals(tweets.subList(0, 2), task1.getTweetsToRemove());
    }

    @Test
    public void testLoadingRemovesNothing() {
        final int limit = 10;

        final FetchTweetsTask task = createTask(limit);
        task.setExistingTweets(tweets.subList(0, 2));

        mockClient.addAvailableTweets(tweets.subList(0, 5));

        task.loadNewTweets(mockContext);
        assertOrderedEquals(tweets.subList(2, 5), task.getNewTweets());
        assertOrderedEquals(Collections.<Tweet>emptyList(), task.getTweetsToRemove());
    }

    @Test
    public void testLoadingRemovesAndAddsNothing() {
        final int limit = 10;

        final FetchTweetsTask task = createTask(limit);
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

    private FetchTweetsTask createTask(final int tweetCountLimit) {
        final IAccountProvider mockProvider = mock(IAccountProvider.class);
        final Account mockAccount = mock(Account.class);

        when(mockProvider.getAccounts(mockContext)).thenReturn(Collections.singleton(mockAccount));

        final FetchTweetsTask task = new FetchTweetsTask(mockProvider, mockClient);
        task.setTweetCountLimit(tweetCountLimit);

        return task;
    }

    private interface MockTwitterClient extends ITwitterClient {

        void addAvailableTweets(final Collection<Tweet> tweets);

    }

    private static class MockTwitterClientHandler implements InvocationHandler {

        private final TreeSet<Tweet> availableTweets = new TreeSet<Tweet>();

        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
            if (method.getName().equals("getTimeline")) {
                return getTimeline((Integer) args[1], (Long) args[2], (Long) args[3]);
            } else if (method.getName().equals("addAvailableTweets")) {
                @SuppressWarnings("unchecked")
                Collection<Tweet> arguments = (Collection<Tweet>) args[0];
                addAvailableTweets(arguments);
            }

            throw new UnsupportedOperationException("This mock does not support authorization");
        }

        private void addAvailableTweets(final Collection<Tweet> tweets) {
            availableTweets.addAll(tweets);
        }

        private Tweet[] getTimeline(final Integer count, final Long sinceId, final Long maxId) {
            final List<Tweet> tweets = new LinkedList<Tweet>();
            for (final Tweet tweet : availableTweets) {
                if (maxId != null && maxId <= tweet.getId()) continue;
                if (sinceId != null && sinceId >= tweet.getId()) continue;

                tweets.add(tweet);

                if (count != null && tweets.size() >= count) break;
            }
            return tweets.toArray(new Tweet[tweets.size()]);
        }

        public static MockTwitterClient create() {
            return (MockTwitterClient) Proxy.newProxyInstance(
                    MockTwitterClient.class.getClassLoader(),
                    new Class[] { MockTwitterClient.class },
                    new MockTwitterClientHandler());
        }

    }

}
