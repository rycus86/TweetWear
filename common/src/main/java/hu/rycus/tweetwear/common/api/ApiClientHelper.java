package hu.rycus.tweetwear.common.api;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.List;

public class ApiClientHelper {

    private static final String TAG = ApiClientHelper.class.getSimpleName();

    public static <T> T runSynchronously(final Context context,
                                         final ApiClientCallable<T> task) throws Exception {
        final GoogleApiClient apiClient = createApiClient(context);
        try {
            final ConnectionResult connectionResult = apiClient.blockingConnect();
            if (connectionResult.isSuccess()) {
                return task.call(context, apiClient);
            } else {
                throw new IllegalArgumentException("Failed to connect to ApiClient");
            }
        } finally {
            apiClient.disconnect();
        }
    }

    public static void runAsynchronously(final Context context, final ApiClientRunnable task) {
        final GoogleApiClient apiClient = createApiClient(context);
        apiClient.registerConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
            @Override
            public void onConnected(final Bundle connectionHint) {
                executeTask(context, apiClient, task);
            }

            @Override
            public void onConnectionSuspended(final int cause) {
                final String reason = getCause(cause);
                Log.w(TAG, String.format("ApiClient connection suspended: %s", reason));
            }

            private String getCause(int cause) {
                switch (cause) {
                    case CAUSE_NETWORK_LOST:
                        return "Network lost";
                    case CAUSE_SERVICE_DISCONNECTED:
                        return "Service disconnected";
                    default:
                        return "Unknown cause";
                }
            }
        });
        apiClient.connect();
    }

    public static Node getConnectedNode(final GoogleApiClient apiClient) {
        final NodeApi.GetConnectedNodesResult connectedNodesResult =
                Wearable.NodeApi.getConnectedNodes(apiClient).await();

        if (connectedNodesResult.getStatus().isSuccess()) {
            final List<Node> nodes = connectedNodesResult.getNodes();
            if (!nodes.isEmpty()) {
                return nodes.iterator().next();
            }
        } else {
            Log.w(TAG, "Get connected nodes request failed");
        }

        return null;
    }

    public static boolean sendMessageToConnectedNode(final GoogleApiClient apiClient,
                                                     final String path, final byte[] payload) {
        final Node node = ApiClientHelper.getConnectedNode(apiClient);
        if (node != null) {
            final MessageApi.SendMessageResult sendResult = Wearable.MessageApi
                    .sendMessage(apiClient, node.getId(), path, payload).await();
            return sendResult.getStatus().isSuccess();
        }

        return false;
    }

    private static GoogleApiClient createApiClient(final Context context) {
        return new GoogleApiClient.Builder(context)
                .addApi(Wearable.API)
                .build();
    }

    private static void executeTask(final Context context, final GoogleApiClient apiClient,
                                    final ApiClientRunnable task) {
        final Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    task.run(context, apiClient);
                } catch (Exception ex) {
                    Log.e(TAG, "Failed to execute ApiClient task", ex);
                } finally {
                    apiClient.disconnect();
                }
            }
        };
        thread.start();
    }

}
