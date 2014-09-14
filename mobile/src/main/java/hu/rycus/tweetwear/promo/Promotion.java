package hu.rycus.tweetwear.promo;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor(suppressConstructorProperties = true)
public class Promotion {

    private final String id;
    private final int contentResource;

}
