package hu.rycus.tweetwear.twitter.client.callbacks;

public interface UsernameCallback {

    void onUsernameLoaded(String name);

    void onUsernameLoadError();

}
