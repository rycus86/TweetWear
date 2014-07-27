package hu.rycus.tweetwear.common.model.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class Entities {

    private Hashtag[] hashtags;

    private Media[] media;

    private Url[] urls;

    @JsonProperty("user_mentions")
    private UserMention[] userMentions;

}
