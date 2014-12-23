package hu.rycus.tweetwear.twitter.stream;

import android.util.Log;

import org.scribe.exceptions.OAuthConnectionException;

import java.util.concurrent.TimeUnit;

import hu.rycus.tweetwear.twitter.client.HttpResponseException;

public class StreamConnectionDelay {

    public long next(final Exception reasonHint) {
        return Reason.of(reasonHint).delay();
    }

    public void reset() {
        for (final Reason reason : Reason.values()) {
            reason.reset();
        }
    }

    public static String format(final long value) {
        final long oneMinute = TimeUnit.MINUTES.toMillis(1);
        final long minutes = value / oneMinute;
        if (minutes > 0) {
            return String.format("%d minutes", minutes);
        } else {
            final double oneSecond = TimeUnit.SECONDS.toMillis(1);
            final double seconds = value / oneSecond;
            return String.format("%.2f seconds", seconds);
        }
    }

    private enum Reason {

        TCP_IP {
            final long MAX_DELAY = TimeUnit.SECONDS.toMillis(16);

            @Override
            protected long delay() {
                /*
                 * Back off linearly for TCP/IP level network errors. These problems are generally
                 * temporary and tend to clear quickly. Increase the delay in reconnects by 250ms
                 * each attempt, up to 16 seconds.
                 */
                final long delay = (nextAttempt() + 1) * 250L;
                return Math.min(delay, MAX_DELAY);
            }
        },

        HTTP {
            final long FIVE_SECONDS = TimeUnit.SECONDS.toMillis(5);
            final long MAX_DELAY = TimeUnit.SECONDS.toMillis(320);

            @Override
            protected long delay() {
                /*
                 * Back off exponentially for HTTP errors for which reconnecting would be
                 * appropriate. Start with a 5 second wait, doubling each attempt, up to 320
                 * seconds.
                 */
                final long delay = (long) Math.pow(2, nextAttempt()) * FIVE_SECONDS;
                return Math.min(delay, MAX_DELAY);
            }
        },

        HTTP_420 {
            final long ONE_MINUTE = TimeUnit.MINUTES.toMillis(1);
            final long MAX_DELAY = TimeUnit.MINUTES.toMillis(10);

            @Override
            protected long delay() {
                /*
                 * Back off exponentially for HTTP 420 errors. Start with a 1 minute wait and
                 * double each attempt. Note that every HTTP 420 received increases the time you
                 * must wait until rate limiting will no longer will be in effect for your account.
                 */
                final long delay = (long) Math.pow(2, nextAttempt()) * ONE_MINUTE;
                return Math.min(delay, MAX_DELAY);
            }
        };

        private int attempt = 0;

        protected abstract long delay();

        protected int nextAttempt() {
            return attempt++;
        }

        public void reset() {
            attempt = 0;
        }

        public static Reason of(final Exception exception) {
            Log.d("CONN", "Reason: " + exception + " " + exception.getClass());
            if (exception instanceof HttpResponseException) {
                final HttpResponseException httpException = (HttpResponseException) exception;
                if (httpException.getCode() == 420) {
                    return HTTP_420;
                } else {
                    return HTTP;
                }
            } else if (exception instanceof OAuthConnectionException) {
                return HTTP;
            } else {
                return TCP_IP;
            }
        }

    }

}
