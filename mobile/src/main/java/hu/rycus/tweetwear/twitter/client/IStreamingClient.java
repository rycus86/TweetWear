package hu.rycus.tweetwear.twitter.client;

import android.content.Context;

import org.scribe.model.Token;

import hu.rycus.tweetwear.twitter.TwitterConstants;

public interface IStreamingClient extends TwitterConstants {

    void streamTimeline(Context context, Token accessToken);

    void stopTimelineStreaming(Token accessToken);

}
