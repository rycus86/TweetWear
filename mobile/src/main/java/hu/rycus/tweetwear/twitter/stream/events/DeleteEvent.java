package hu.rycus.tweetwear.twitter.stream.events;


import android.content.Context;
import android.util.Log;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import hu.rycus.tweetwear.common.api.ApiClientHelper;
import hu.rycus.tweetwear.tasks.DeleteTweetTask;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeleteEvent implements StreamEvent {

    private static final String TAG = DeleteEvent.class.getSimpleName();

    private DeleteNode delete;

    public static boolean matches(final JsonNode rootNode) {
        return rootNode.has("delete");
    }

    @Override
    public void process(final Context context) {
        Log.d(TAG, String.format("Deleting tweet #%d from @%s",
                getStatus().getId(), getStatus().getUserId()));

        ApiClientHelper.runAsynchronously(context, new DeleteTweetTask(getStatus().getId()));
    }

    public DeleteNode.Status getStatus() {
        return getDelete().getStatus();
    }

    @Getter
    @Setter
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public class DeleteNode {

        private Status status;

        @Getter
        @Setter
        @ToString
        @JsonIgnoreProperties(ignoreUnknown = true)
        public class Status {

            private long id;

            @JsonProperty("user_id")
            private long userId;

        }

    }

}
