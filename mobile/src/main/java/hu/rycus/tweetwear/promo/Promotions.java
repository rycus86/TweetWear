package hu.rycus.tweetwear.promo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.util.Log;

import hu.rycus.tweetwear.R;

public class Promotions {

    private static final String TAG = Promotions.class.getSimpleName();

    private static final String PREFERENCES_NAME = "promotions";

    private static final Promotion ACTIVE_PROMO =
            new Promotion("read_it_later", R.string.promo_read_later);

    private static AlertDialog activeDialog = null;

    public static Promotion getActivePromotion() {
        return ACTIVE_PROMO;
    }

    public static void showDialogIfNeeded(final Context context) {
        if (!shouldShowPromotion(context)) return;

        final Promotion promotion = getActivePromotion();
        final AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(R.string.promo_title)
                .setMessage(promotion.getContentResource())
                .setCancelable(true)
                .setOnDismissListener(createPromotionDismissListener(context))
                .setPositiveButton(android.R.string.ok, null);
        activeDialog = builder.show();
    }

    public static void dismissDialog() {
        if (activeDialog != null) {
            activeDialog.setOnDismissListener(null);
            activeDialog.dismiss();
            activeDialog = null;
        }
    }

    public static boolean shouldShowPromotion(final Context context) {
        final String promotionId = getActivePromotion().getId();
        final boolean alreadyShown = getPreferences(context).getBoolean(promotionId, false);
        return !alreadyShown;
    }

    public static boolean setPromotionShown(final Context context) {
        final String promotionId = getActivePromotion().getId();
        Log.d(TAG, String.format("Marking promotion as shown: %s", promotionId));

        return getPreferences(context)
                .edit().putBoolean(promotionId, true).commit();
    }

    private static DialogInterface.OnDismissListener createPromotionDismissListener(
            final Context context) {
        return new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(final DialogInterface dialog) {
                setPromotionShown(context);
                activeDialog = null;
            }
        };
    }

    private static SharedPreferences getPreferences(final Context context) {
        return context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

}
