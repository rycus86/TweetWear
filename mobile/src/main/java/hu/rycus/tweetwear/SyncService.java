package hu.rycus.tweetwear;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import hu.rycus.tweetwear.common.api.ApiClientHelper;
import hu.rycus.tweetwear.common.util.Constants;
import hu.rycus.tweetwear.preferences.Preferences;
import hu.rycus.tweetwear.tasks.ClearExistingTweetsTask;
import hu.rycus.tweetwear.tasks.FetchTimelineTask;
import hu.rycus.tweetwear.twitter.TwitterFactory;
import hu.rycus.tweetwear.twitter.account.IAccountProvider;
import hu.rycus.tweetwear.twitter.client.ITwitterClient;

public class SyncService extends Service {

    private static final String TAG = SyncService.class.getSimpleName();

    private PendingIntent scheduledIntent;

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("This service can not be bound");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        scheduledIntent = createScheduledIntent();
    }

    private PendingIntent createScheduledIntent() {
        final Intent schedulerIntent = createIntent(this, Constants.ACTION_START_SYNC);
        return PendingIntent.getService(this, 0, schedulerIntent, 0);
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        if (intent != null && intent.getAction() != null) {
            if (Constants.ACTION_START_ALARM.equals(intent.getAction())) {
                scheduleTask();
            } else if (Constants.ACTION_CANCEL_ALARM.equals(intent.getAction())) {
                cancelTask();
            } else if (Constants.ACTION_START_SYNC.equals(intent.getAction())) {
                requestFetchTimeline();
            } else if (Constants.ACTION_CLEAR_EXISTING.equals(intent.getAction())) {
                requestClearExistingTweets();
            } else {
                return super.onStartCommand(intent, flags, startId);
            }

            return START_REDELIVER_INTENT;
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private void scheduleTask() {
        final long interval = Preferences.getRefreshInterval(this);
        final AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        manager.cancel(scheduledIntent);

        Log.d(TAG, "Scheduling alarm for synchronization");
        manager.setInexactRepeating(
                AlarmManager.ELAPSED_REALTIME, 0L, interval, scheduledIntent);
    }

    private void cancelTask() {
        final AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        manager.cancel(scheduledIntent);
    }

    private void requestFetchTimeline() {
        final IAccountProvider provider = TwitterFactory.createProvider();
        final ITwitterClient twitterClient = TwitterFactory.createClient();
        ApiClientHelper.runAsynchronously(this, new FetchTimelineTask(provider, twitterClient));
    }

    private void requestClearExistingTweets() {
        ApiClientHelper.runAsynchronously(this, new ClearExistingTweetsTask());
    }

    public static void scheduleSync(final Context context) {
        final Intent intent = createIntent(context, Constants.ACTION_START_ALARM);
        context.startService(intent);
    }

    public static void cancelSync(final Context context) {
        final Intent intent = createIntent(context, Constants.ACTION_CANCEL_ALARM);
        context.startService(intent);
    }

    public static void startSync(final Context context) {
        final Intent intent = createIntent(context, Constants.ACTION_START_SYNC);
        context.startService(intent);
    }

    public static void clearExisting(final Context context) {
        final Intent intent = createIntent(context, Constants.ACTION_CLEAR_EXISTING);
        context.startService(intent);
    }

    private static Intent createIntent(final Context context, final String action) {
        final Intent intent = new Intent(context, SyncService.class);
        intent.setAction(action);
        return intent;
    }

}
