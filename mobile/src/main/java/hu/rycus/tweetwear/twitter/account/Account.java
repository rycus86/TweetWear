package hu.rycus.tweetwear.twitter.account;

import org.scribe.model.Token;

import hu.rycus.tweetwear.preferences.ListSettings;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(suppressConstructorProperties = true)
public class Account {

    private final String username;
    private final Token accessToken;
    private final ListSettings listSettings;

}
