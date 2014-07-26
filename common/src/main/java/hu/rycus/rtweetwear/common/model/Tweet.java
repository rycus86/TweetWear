package hu.rycus.rtweetwear.common.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

import hu.rycus.rtweetwear.common.model.entities.Entities;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class Tweet implements Comparable<Tweet> {

    private static final String DATE_FORMAT = "EEE MMM dd HH:mm:ss Z yyyy";

    private long id;

    @JsonProperty("created_at")
    @JsonFormat(
            shape=JsonFormat.Shape.STRING,
            pattern=DATE_FORMAT,
            timezone="UTC",
            locale = "ENGLISH")
    private Date createdAt;

    private Contributor[] contributors;

    private Entities entities;

    @JsonProperty("favorite_count")
    private int favoriteCount;

    private boolean favorited = false;

    @JsonProperty("in_reply_to_screen_name")
    private String inReplyToScreenName;

    @JsonProperty("in_reply_to_status_id")
    private Long inReplyToStatusId;

    @JsonProperty("in_reply_to_user_id")
    private Long inReplyToUserId;

    private String lang;

    private Place place;

    @JsonProperty("retweet_count")
    private int retweetCount;

    private boolean retweeted = false;

    @JsonProperty("retweeted_status")
    private Tweet retweetedStatus;

    @JsonProperty("source")
    private String sourceHtml;

    private String text;

    private boolean truncated;

    private User user;

    @Override
    public int compareTo(final Tweet another) {
        if (this.getId() > another.getId()) {
            return -1;
        } else if (this.getId() < another.getId()) {
            return 1;
        } else {
            return 0;
        }
    }

}
