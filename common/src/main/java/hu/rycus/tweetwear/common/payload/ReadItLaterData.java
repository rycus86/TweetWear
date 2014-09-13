package hu.rycus.tweetwear.common.payload;

import hu.rycus.tweetwear.common.model.Tweet;
import hu.rycus.tweetwear.common.util.Mapper;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ReadItLaterData {

    private Tweet tweet;
    private String url;

    private ReadItLaterData() {
        // To instantiate when parsing from JSON data
    }

    public ReadItLaterData(final Tweet tweet, final String url) {
        this.tweet = tweet;
        this.url = url;
    }

    public static ReadItLaterData parse(final byte[] data) {
        return Mapper.readObject(data, ReadItLaterData.class);
    }

}
