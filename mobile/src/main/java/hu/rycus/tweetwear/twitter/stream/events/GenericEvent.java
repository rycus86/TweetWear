package hu.rycus.tweetwear.twitter.stream.events;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class GenericEvent<S, T, O> implements StreamEvent {

    protected static final String EVENT_NODE = "event";

    private static final String DATE_FORMAT = "EEE MMM dd HH:mm:ss Z yyyy";

    private String event;

    private S source;

    private T target;

    @JsonProperty("target_object")
    private O targetObject;

    @JsonProperty("created_at")
    @JsonFormat(
            shape=JsonFormat.Shape.STRING,
            pattern=DATE_FORMAT,
            timezone="UTC",
            locale = "ENGLISH")
    private Date createdAt;

    public static boolean matches(final JsonNode rootNode) {
        return rootNode.has(EVENT_NODE);
    }

}
