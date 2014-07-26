package hu.rycus.tweetwear.twitter.account;

import org.scribe.model.Token;

import lombok.Getter;

@Getter
public class Account {

    private final String username;
    private final Token accessToken;

    public Account(final String username, final Token accessToken) {
        this.username = username;
        this.accessToken = accessToken;
    }

}
