package hu.rycus.tweetwear;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.wearable.activity.ConfirmationActivity;

import hu.rycus.tweetwear.common.util.Constants;

public class ActionResponseReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        if (intent != null && intent.getAction() != null) {
            if (Constants.ACTION_TASK_RESULT.equals(intent.getAction())) {
                final boolean success = intent.getBooleanExtra(Constants.EXTRA_SUCCESS_FLAG, false);
                final String message = intent.getStringExtra(Constants.EXTRA_CONFIRMATION_MESSAGE);

                final int flags = Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION;
                final int animationType;
                if (success) {
                    animationType = ConfirmationActivity.SUCCESS_ANIMATION;
                } else {
                    animationType = ConfirmationActivity.FAILURE_ANIMATION;
                }

                final Intent confirmIntent = new Intent(context, ConfirmationActivity.class)
                        .setFlags(flags)
                        .putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE, animationType)
                        .putExtra(ConfirmationActivity.EXTRA_MESSAGE, message);
                context.startActivity(confirmIntent);
            }
        }
    }
}
