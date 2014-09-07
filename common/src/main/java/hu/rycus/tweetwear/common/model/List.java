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
public class List implements Comparable<List> {

    private long id;

    private String name;

    @JsonProperty("full_name")
    private String fullName;

    private String description;

    private String uri;

    private User user;

    @Override
    public int compareTo(final List another) {
        return fullName.compareToIgnoreCase(another.fullName);
    }
}
