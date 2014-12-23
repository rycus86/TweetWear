package hu.rycus.tweetwear.twitter.client;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.scribe.model.Token;
import org.scribe.oauth.OAuthService;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import hu.rycus.tweetwear.common.api.ApiClientHelper;
import hu.rycus.tweetwear.tasks.FetchTweetsTask;
import hu.rycus.tweetwear.twitter.TwitterFactory;
import hu.rycus.tweetwear.twitter.account.IAccountProvider;
import hu.rycus.tweetwear.twitter.stream.StreamConnectionDelay;
import hu.rycus.tweetwear.twitter.stream.StreamEventHandler;

public enum StreamingClient implements IStreamingClient {

    INSTANCE;

    private static final String TAG = StreamingClient.class.getSimpleName();

    private final OAuthService service = OAuthServiceProvider.INSTANCE.get();

    private final ConcurrentHashMap<Token, StreamEventHandler> timelineHandlers;

    private final StreamConnectionDelay delay = new StreamConnectionDelay();

    private StreamingClient() {
        this.timelineHandlers = new ConcurrentHashMap<Token, StreamEventHandler>();
    }

    @Override
    public void streamTimeline(final Context context, final Token accessToken) {
        if (isOnMainThread()) {
            final Thread thread = new Thread() {
                @Override
                public void run() {
                    streamTimeline(context, accessToken);
                }
            };
            thread.start();
            return;
        }

        boolean streamingStopped = false;
        Exception errorReason = null;

        try {
            if (accessToken == null) {
                throw new IllegalArgumentException("No access token present");
            }

            // register dummy stream
            registerStream(accessToken, new ByteArrayInputStream(new byte[0]));

            final InputStream inputStream = RequestBuilder.post(Uri.STREAM_TIMELINE.get())
//            final InputStream inputStream = RequestBuilder.post(Uri.STREAM_SAMPLE.get())
                    .setConnectTimeout(30, TimeUnit.SECONDS)
                    .setReadTimeout(90, TimeUnit.SECONDS)
                    .setKeepAlive(true)
                    .stream(service, accessToken);

            onStreamConnected();
            checkForMissedTimelineTweets(context);

            final StreamEventHandler handler = registerStream(accessToken, inputStream);
            handler.process(context);

            streamingStopped = handler.isStopped();
        } catch (Exception ex) {
            errorReason = ex;
            Log.e(TAG, "Failed to stream timeline", ex);
        } finally {
            if (!streamingStopped) {
                restartTimelineStreaming(context, accessToken, errorReason);
            }
        }
    }

    @Override
    public void stopTimelineStreaming(final Token accessToken) {
        if (isOnMainThread()) {
            final Thread thread = new Thread() {
                @Override
                public void run() {
                    stopTimelineStreaming(accessToken);
                }
            };
            thread.start();
            return;
        }

        try {
            if (accessToken == null) {
                throw new IllegalArgumentException("No access token present");
            }

            final StreamEventHandler handler = timelineHandlers.remove(accessToken);
            if (handler != null) {
                Log.d(TAG, "Stopping timeline streaming");
                handler.stop();
            }
        } catch (Exception ex) {
            Log.e(TAG, "Failed to stop timeline streaming", ex);
        }
    }

    private boolean isOnMainThread() {
        return Looper.getMainLooper().equals(Looper.myLooper());
    }

    public void test$abortStreaming(final Token token) {
        if (isOnMainThread()) {
            final Thread thread = new Thread() {
                @Override
                public void run() {
                    test$abortStreaming(token);
                }
            };
            thread.start();
            return;
        }

        final StreamEventHandler handler = timelineHandlers.get(token);
        if (handler != null) {
            handler.test$abort();
        }
    }

    private void onStreamConnected() {
        Log.d(TAG, "Starting timeline streaming");
        delay.reset();
    }

    private void checkForMissedTimelineTweets(final Context context) {
        final IAccountProvider provider = TwitterFactory.createProvider();
        final ITwitterClient client = TwitterFactory.restClient();
        ApiClientHelper.runAsynchronously(context, new FetchTweetsTask(provider, client));
    }

    private StreamEventHandler registerStream(final Token accessToken,
                                              final InputStream inputStream) {
        final StreamEventHandler handler = new StreamEventHandler(inputStream);
        timelineHandlers.put(accessToken, handler);
        return handler;
    }

    private void restartTimelineStreaming(final Context context, final Token accessToken,
                                          final Exception reasonHint) {
        final RestartTask restartTask = new RestartTask(context, accessToken);
        final Handler handler = new Handler(context.getMainLooper());
        final long taskDelay = delay.next(reasonHint);
        handler.postDelayed(restartTask, taskDelay);

        final String retryMessage = String.format(
                "Retrying streaming in %s", StreamConnectionDelay.format(taskDelay));
        Log.d(TAG, retryMessage);
    }

    private class RestartTask implements Runnable {

        private final Context context;
        private final Token token;

        private RestartTask(final Context context, final Token token) {
            this.context = context;
            this.token = token;
        }

        @Override
        public void run() {
            Log.d(TAG, "Trying to restart timeline streaming");
            Log.d(TAG, "Current: " + timelineHandlers);
            Log.d(TAG, "Token  : " + token);

            if (isStreamingStillWanted()) {
                streamTimeline(context, token);
            } else {
                Log.d(TAG, "Timeline streaming no longer needed, skipping restart");
            }
        }

        private boolean isStreamingStillWanted() {
            return timelineHandlers.containsKey(token);
        }

    }

}
