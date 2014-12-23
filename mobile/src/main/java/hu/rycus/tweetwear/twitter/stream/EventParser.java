package hu.rycus.tweetwear.twitter.stream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectReader;

import java.io.IOException;

import hu.rycus.tweetwear.common.util.Mapper;
import hu.rycus.tweetwear.twitter.stream.events.DeleteEvent;
import hu.rycus.tweetwear.twitter.stream.events.FavoriteEvent;
import hu.rycus.tweetwear.twitter.stream.events.StreamEvent;
import hu.rycus.tweetwear.twitter.stream.events.TweetEvent;
import hu.rycus.tweetwear.twitter.stream.events.UnfavoriteEvent;

public final class EventParser {

    private static final ObjectReader reader = Mapper.reader();

    public static StreamEvent parse(final String event) throws IOException {
        final JsonNode rootNode = reader.readTree(event);
        if (TweetEvent.matches(rootNode)) {
            return Mapper.readObject(event, TweetEvent.class);
        } else if (DeleteEvent.matches(rootNode)) {
            return Mapper.readObject(event, DeleteEvent.class);
        } else if (FavoriteEvent.matches(rootNode)) {
            return Mapper.readObject(event, FavoriteEvent.class);
        } else if (UnfavoriteEvent.matches(rootNode)) {
            return Mapper.readObject(event, UnfavoriteEvent.class);
        } else {
            return null;
        }
    }

}
