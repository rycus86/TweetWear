package hu.rycus.rtweetwear.common.model.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class Size {

    @JsonProperty("w")
    private int width;

    @JsonProperty("h")
    private int height;

    private Resize resize;

    public enum Resize {
        FIT, CROP;

        @JsonValue
        public String asJsonValue() {
            return name().toLowerCase();
        }

        @JsonCreator
        public Resize fromJsonValue(final String value) {
            return valueOf(value.toUpperCase());
        }

    }

}
