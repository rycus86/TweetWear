package hu.rycus.tweetwear;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.wearable.activity.ConfirmationActivity;

/**
 * The built-in ConfirmationActivity does not
 * cancel itself after a failure confirmation
 * so we do it after 3 seconds.
 */
public class FinishingConfirmationActivity extends ConfirmationActivity {

    public static void show(final Context context, final boolean success, final String message) {
        final int flags = Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION;
        final int animationType;
        if (success) {
            animationType = SUCCESS_ANIMATION;
        } else {
            animationType = FAILURE_ANIMATION;
        }

        final Intent confirmIntent = new Intent(context, FinishingConfirmationActivity.class)
                .setFlags(flags)
                .putExtra(EXTRA_ANIMATION_TYPE, animationType)
                .putExtra(EXTRA_MESSAGE, message);
        context.startActivity(confirmIntent);
    }

    @Override
    protected void onStart() {
        super.onStart();

        final Intent startIntent = getIntent();
        if (startIntent != null) {
            final int type = startIntent.getIntExtra(EXTRA_ANIMATION_TYPE, -1);
            if (type == FAILURE_ANIMATION) {
                delayedFinish();
            }
        }
    }

    private void delayedFinish() {
        final Handler handler = new Handler();
        handler.postDelayed(finishRunnable, 3000L);
    }

    private final Runnable finishRunnable = new Runnable() {
        @Override
        public void run() {
            finish();
        }
    };

}
