package hu.rycus.tweetwear.common.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class Lists {

    public static final long CURSOR_FINISHED = 0L;
    public static final int MAX_COUNT = 1000;

    private List[] lists;

    @JsonProperty("next_cursor")
    private long nextCursor;

    @JsonProperty("previous_cursor")
    private long previousCursor;

}
