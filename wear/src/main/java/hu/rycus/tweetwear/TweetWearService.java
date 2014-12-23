package hu.rycus.tweetwear;

import android.content.Intent;
import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import hu.rycus.tweetwear.message.ActionHandler;
import hu.rycus.tweetwear.message.MessageHandler;

public class TweetWearService extends WearableListenerService {

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        if (intent != null && intent.getAction() != null) {
            if ("NOTIF".equals(intent.getAction())) {
                Log.d("NOTIF", intent + " - " + intent.getExtras());
                return START_REDELIVER_INTENT;
            }
            ActionHandler.handle(this, intent);
        }

        return START_REDELIVER_INTENT;
    }

    @Override
    public void onMessageReceived(final MessageEvent messageEvent) {
        MessageHandler.handle(this, messageEvent);
    }

}
