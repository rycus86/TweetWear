package hu.rycus.tweetwear.twitter.client.callbacks;

public interface AccessTokenCallback {

    void onAccessTokenSaved();

    void onAccessTokenError();

}
