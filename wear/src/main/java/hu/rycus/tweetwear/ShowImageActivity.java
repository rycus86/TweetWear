package hu.rycus.tweetwear;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;

public class ShowImageActivity extends Activity {

    private static final String TAG = ShowImageActivity.class.getSimpleName();

    private static final String EXTRA_PREFIX = ShowImageActivity.class.getCanonicalName();
    private static final String EXTRA_MEDIA_DATA = EXTRA_PREFIX + ".MediaData";
    private static final String EXTRA_FAILURE = EXTRA_PREFIX + ".Failure";

    private ImageView imageView;
    private View loadingContainer;

    @Override
    protected void onStop() {
        super.onStop();

        Log.d(TAG, "Finishing image activity");
        finish();
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_image);

        imageView = (ImageView) findViewById(R.id.img_media);
        loadingContainer = findViewById(R.id.media_loading_container);

        final Intent intent = getIntent();
        checkIntent(intent);
    }

    @Override
    protected void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);
        checkIntent(intent);
    }

    private void checkIntent(final Intent intent) {
        if (intent != null && intent.getExtras() != null) {
            final Bundle extras = intent.getExtras();

            final Bitmap bitmap = extras.getParcelable(EXTRA_MEDIA_DATA);
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
                animateContentChange();

                Log.d(TAG, "Bitmap received, changing content");
                return;
            }

            final boolean failure = extras.getBoolean(EXTRA_FAILURE, false);
            if (failure) {
                final String message = getString(R.string.show_image_failure);
                FinishingConfirmationActivity.show(this, false, message);

                finish();
                return;
            }
        }

        Log.d(TAG, "Image activity is loading");
    }

    private void animateContentChange() {
        final Animation hideAnimation = new AlphaAnimation(1f, 0f);
        hideAnimation.setDuration(350);
        hideAnimation.setFillAfter(true);

        final Animation showAnimation = new AlphaAnimation(0f, 1f);
        showAnimation.setDuration(500);
        showAnimation.setFillAfter(true);

        loadingContainer.startAnimation(hideAnimation);
        imageView.startAnimation(showAnimation);
    }

    public static void start(final Context context) {
        final Intent intent = getStartIntent(context);
        startForIntent(context, intent);
    }

    public static void startForImage(final Context context, final Bitmap bitmap) {
        final Intent intent = getStartIntent(context);
        intent.putExtra(EXTRA_MEDIA_DATA, bitmap);
        startForIntent(context, intent);
    }

    public static void startForFailure(final Context context) {
        final Intent intent = getStartIntent(context);
        intent.putExtra(EXTRA_FAILURE, true);
        startForIntent(context, intent);
    }

    private static Intent getStartIntent(final Context context) {
        final Intent intent = new Intent(context, ShowImageActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return intent;
    }

    private static Bundle getStartOptions(final Context context) {
        final int animIn = android.R.anim.fade_in;
        final int animOut = android.R.anim.fade_out;
        final ActivityOptions activityOptions =
                ActivityOptions.makeCustomAnimation(context, animIn, animOut);
        return activityOptions.toBundle();
    }

    private static void startForIntent(final Context context, final Intent intent) {
        final Bundle options = getStartOptions(context);
        context.startActivity(intent, options);
    }

}
