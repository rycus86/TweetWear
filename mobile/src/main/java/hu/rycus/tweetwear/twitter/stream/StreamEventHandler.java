package hu.rycus.tweetwear.twitter.stream;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import hu.rycus.tweetwear.twitter.stream.events.StreamEvent;

public class StreamEventHandler {

    private static final String TAG = StreamEventHandler.class.getSimpleName();

    private final AtomicBoolean stopped = new AtomicBoolean(false);
    private final TwitterStream stream;

    public StreamEventHandler(final InputStream inputStream) {
        this.stream = new TwitterStream(inputStream);
    }

    public void test$abort() {
        try {
            stream.source.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() throws IOException {
        stopped.set(true);
        stream.close();
    }

    public boolean isStopped() {
        return stopped.get();
    }

    public void process(final Context context) throws IOException {
        final ExecutorService executor = Executors.newSingleThreadExecutor();

        try {
            String line;
            while ((line = stream.readLine()) != null) {
                final String event = line;
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            handle(context, event);
                        } catch (Exception ex) {
                            Log.e(TAG, String.format("Failed to handle stream event: %s", event), ex);
                        }
                    }
                });
            }
        } finally {
            tryClosingStream();
            executor.shutdown();
        }
    }

    private void handle(final Context context, final String event) throws IOException {
        final StreamEvent parsedEvent = EventParser.parse(event);
        if (parsedEvent != null) {
            parsedEvent.process(context);
        }
    }

    private void tryClosingStream() {
        try {
            stream.source.close();
        } catch (IOException ex) {
            Log.w(TAG, "Failed to close stream", ex);
        }
    }

    private static class TwitterStream extends InputStream {

        private final InputStream source;
        private final StringBuilder builder = new StringBuilder();
        private final AtomicBoolean closed = new AtomicBoolean(false);

        private TwitterStream(final InputStream source) {
            this.source = source;
        }

        @Override
        public int read() throws IOException {
            return source.read();
        }

        public String readLine() throws IOException {
            for (;;) {
                final int next;
                try {
                    next = this.read();
                    if (next < 0) {
                        throw new IOException("Stream closed (-1 received)");
                    }
                } catch (Exception ex) {
                    if (closed.get()) {
                        Log.d(TAG, String.format("Streaming finished: %s", ex));
                        return null;
                    } else {
                        if (ex instanceof IOException) {
                            throw (IOException) ex;
                        } else {
                            throw new IOException("Failed to process stream", ex);
                        }
                    }
                }

                if (next == '\r') {
                    final int newLine = this.read();
                    if (newLine != '\n') {
                        Log.w(TAG, String.format("New-line character expected, " +
                                "'%c (0x%X)' found instead", (char) newLine, newLine));
                    }

                    if (builder.length() == 0) {
                        continue; // keep-alive
                    } else {
                        break;
                    }
                }

                builder.append((char) next);
            }

            final String line = builder.toString();
            builder.setLength(0);
            return line;
        }

        @Override
        public void close() throws IOException {
            closed.set(true);
            source.close();
            super.close();
        }
    }

}
