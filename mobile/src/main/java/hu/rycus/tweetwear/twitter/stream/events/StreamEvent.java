package hu.rycus.tweetwear.twitter.stream.events;

import android.content.Context;

public interface StreamEvent {

    void process(Context context);

}
