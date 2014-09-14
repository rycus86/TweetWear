package hu.rycus.tweetwear.tasks;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;

import hu.rycus.tweetwear.common.api.ApiClientHelper;
import hu.rycus.tweetwear.common.api.ApiClientRunnable;
import hu.rycus.tweetwear.common.util.Constants;
import hu.rycus.tweetwear.promo.Promotion;
import hu.rycus.tweetwear.promo.Promotions;

public class SendPromotionTask extends ApiClientRunnable {

    private static final String TAG = SendPromotionTask.class.getSimpleName();

    @Override
    protected void run(final Context context, final GoogleApiClient apiClient) throws Exception {
        if (shouldAbortTask(context)) return;

        final Promotion promotion = Promotions.getActivePromotion();
        final String path = getPath(promotion);
        final String promotionText = getPromotionText(context, promotion);

        if (sendMessageIfNeeded(context, apiClient, path, promotionText)) {
            Log.d(TAG, String.format("Promotion sent to connected device: %s", promotionText));
            Promotions.setPromotionShown(context);
        }
    }

    private String getPath(final Promotion promotion) {
        return Constants.DataPath.PROMOTION.withId(promotion.getId());
    }

    private String getPromotionText(final Context context, final Promotion promotion) {
        return context.getString(promotion.getContentResource());
    }

    private boolean sendMessageIfNeeded(final Context context, final GoogleApiClient apiClient,
                                        final String path, final String promotionText) {
        if (shouldAbortTask(context)) return false;
        return ApiClientHelper.sendMessageToConnectedNode(apiClient, path, promotionText.getBytes());
    }

    /** Returns true if the promotion has been already shown therefore this task should abort. */
    private boolean shouldAbortTask(final Context context) {
        return !Promotions.shouldShowPromotion(context);
    }

}
