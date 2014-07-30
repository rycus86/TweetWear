package hu.rycus.tweetwear.common.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class Place {

    private String id;

    private final Map<String, String> attributes = new HashMap<String, String>();

    private String country;

    @JsonProperty("country_code")
    private String countryCode;

    @JsonProperty("full_name")
    private String fullName;

    private String name;

    @JsonProperty("place_type")
    private String placeType;

    private String url;

}
