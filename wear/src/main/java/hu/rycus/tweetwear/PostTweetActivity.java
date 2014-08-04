package hu.rycus.tweetwear;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.support.v4.content.LocalBroadcastManager;
import android.support.wearable.view.CardFrame;
import android.support.wearable.view.CardScrollView;
import android.support.wearable.view.DelayedConfirmationView;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

import hu.rycus.tweetwear.common.api.ApiClientHelper;
import hu.rycus.tweetwear.common.util.Constants;
import hu.rycus.tweetwear.task.PostTweetTask;

public class PostTweetActivity extends Activity
        implements DelayedConfirmationView.DelayedConfirmationListener {

    private static final String TAG = PostTweetActivity.class.getSimpleName();

    private static final String ACTION_TASK_FINISHED =
            BuildConfig.PACKAGE_NAME + ".PostTaskFinished";

    private static final int REQUEST_NEW_TWEET = 100;
    private static final int REQUEST_REPLY = 101;

    private View promptView;
    private View mainContentView;

    private TextView txtContent;
    private DelayedConfirmationView delayedConfirmationView;
    private TextView txtCancelLabel;

    private Long originalTweetId;
    private String originalTweetUserScreenname;

    private Mode mode;
    private String contentToSend;
    private boolean cancelled = false;

    private Handler handler;

    private enum Mode {
        NEW_TWEET, REPLY
    }

    private final Runnable startRecognizerRunnable = new Runnable() {
        @Override
        public void run() {
            startRecognition();
            handler.postDelayed(switchToMainContentRunnable, 500);
        }
    };

    private final Runnable switchToMainContentRunnable = new Runnable() {
        @Override
        public void run() {
            promptView.setVisibility(View.GONE);
            mainContentView.setVisibility(View.VISIBLE);
        }
    };

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_tweet);

        final CardScrollView scrollView = (CardScrollView) findViewById(R.id.card_scroller);
        scrollView.setCardGravity(Gravity.BOTTOM);

        final CardFrame cardFrame = (CardFrame) findViewById(R.id.card_frame);
        cardFrame.setExpansionFactor(1f);
        cardFrame.setExpansionDirection(CardFrame.EXPAND_UP);

        promptView = findViewById(R.id.prompt);
        mainContentView = findViewById(R.id.main_content);

        txtContent = (TextView) findViewById(R.id.txt_content);
        delayedConfirmationView = (DelayedConfirmationView) findViewById(R.id.delayed_confirmation);
        delayedConfirmationView.setTotalTimeMs(5000L);
        txtCancelLabel = (TextView) findViewById(R.id.txt_cancel_notice);

        handler = new Handler();

        checkStartMode();
    }

    @Override
    protected void onStart() {
        super.onStart();
        delayedStartRecognition();
    }

    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(taskResultReceiver);
        super.onStop();
    }

    private void checkStartMode() {
        mode = Mode.NEW_TWEET;

        final Intent startIntent = getIntent();
        if (startIntent != null) {
            Log.d(TAG, String.format(
                    "Started activity with intent action: %s", startIntent.getAction()));

            if (Constants.ACTION_CAPTURE_REPLY.equals(startIntent.getAction())) {
                mode = Mode.REPLY;
                originalTweetId = startIntent.getLongExtra(Constants.EXTRA_TWEET_ID, -1L);
                originalTweetUserScreenname =
                        startIntent.getStringExtra(Constants.EXTRA_REPLY_TO_NAME);
            }
        }
    }

    private void delayedStartRecognition() {
        handler.postDelayed(startRecognizerRunnable, 2500L);
    }

    private void startRecognition() {
        final int requestId;
        switch (mode) {
            case NEW_TWEET:
                requestId = REQUEST_NEW_TWEET;
                break;
            case REPLY:
                requestId = REQUEST_REPLY;
                break;
            default:
                Log.w(TAG, String.format("Wrong recognition mode: %s", mode));
                return;
        }

        startRecognitionWithRequestId(requestId);
    }

    private void startRecognitionWithRequestId(final int id) {
        final Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        startActivityForResult(intent, id);
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_NEW_TWEET || requestCode == REQUEST_REPLY) {
                final ArrayList<String> results =
                        data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                if (results != null && !results.isEmpty()) {
                    final String input = results.iterator().next().trim();
                    if (!input.isEmpty()) {
                        onSuccessfulInput(input);
                        return;
                    }
                }
            }
        }

        onFailedInput();
    }

    private void onSuccessfulInput(final String content) {
        Log.d(TAG, String.format("Received voice input: %s", content));

        contentToSend = processContent(content);

        txtContent.setText(contentToSend);
        delayedConfirmationView.setListener(this);
        delayedConfirmationView.start();
    }

    private String processContent(final String originalContent) {
        String content = originalContent;

        // capitalize the first letter
        content = content.substring(0, 1).toUpperCase() + content.substring(1);
        // replace hashtags with the # character
        content = content.replaceAll("hashtag\\s+([^\\s]+)", "#$1");

        if (mode == Mode.REPLY) {
            return String.format("@%s %s", originalTweetUserScreenname, content);
        } else {
            return content;
        }
    }

    private void onFailedInput() {
        FinishingConfirmationActivity.show(this, false, getString(R.string.post_tweet_capture_failed));
        finish();
    }

    @Override
    public void onTimerFinished(final View view) {
        if (cancelled) return;

        txtCancelLabel.setVisibility(View.INVISIBLE);

        executeTask();
    }

    @Override
    public void onTimerSelected(final View view) {
        cancelled = true;

        FinishingConfirmationActivity.show(this, false, getString(R.string.post_tweet_cancelled));
        finish();
    }

    private void executeTask() {
        final IntentFilter filter = new IntentFilter(ACTION_TASK_FINISHED);
        LocalBroadcastManager.getInstance(this).registerReceiver(taskResultReceiver, filter);

        final PostTweetTask task = getTask();
        ApiClientHelper.runAsynchronously(this, task);
    }

    private PostTweetTask getTask() {
        switch (mode) {
            case NEW_TWEET:
                return PostTweetTask.newTweet(contentToSend);
            case REPLY:
                return PostTweetTask.reply(contentToSend, originalTweetId);
            default:
                Log.w(TAG, String.format("Wrong recognition mode: %s", mode));
                return null;
        }
    }

    private final BroadcastReceiver taskResultReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            if (intent != null && intent.getAction() != null) {
                if (ACTION_TASK_FINISHED.equals(intent.getAction())) {
                    finish();
                }
            }
        }
    };

    public static void notifyTaskFinished(final Context context) {
        final Intent intent = new Intent(ACTION_TASK_FINISHED);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

}
