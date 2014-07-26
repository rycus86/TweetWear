package hu.rycus.rtweetwear.common.model.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class Sizes {

    private Size thumb;

    private Size large;

    private Size medium;

    private Size small;

}
