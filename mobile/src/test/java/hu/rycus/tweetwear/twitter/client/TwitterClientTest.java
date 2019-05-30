package hu.rycus.tweetwear.twitter.client;

import org.junit.Ignore;
import org.junit.Test;
import org.scribe.model.Token;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URI;

import hu.rycus.tweetwear.common.model.Tweet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TwitterClientTest {

    private final OAuthService service = TwitterClient.createOAuthService();

    @Test
    @Ignore
    public void testGenerateOauthToken() throws Exception {
        final Token requestToken = service.getRequestToken();
        assertNotNull(requestToken);

        final String authUrl = service.getAuthorizationUrl(requestToken);
        assertNotNull(authUrl);

        System.out.println(authUrl);

        while (!new File("/tmp/auth.token").exists()) {
            Thread.sleep(100);
        }

        String callback;
        try (FileInputStream inputStream = new FileInputStream("/tmp/auth.token")) {
            callback = new BufferedReader(new InputStreamReader(inputStream)).readLine();
        }

        final URI uri = new URI(callback);

        final String oauthVerifier = uri.getQuery().replaceFirst(".*[?&]oauth_verifier=([^?&]+)", "$1");
        assertNotNull(oauthVerifier);
        assertTrue(oauthVerifier.length() > 0);

        final Verifier verifier = new Verifier(oauthVerifier);
        final Token accessToken = service.getAccessToken(requestToken, verifier);
        assertNotNull(accessToken);

        System.out.println(accessToken);
    }

    @Test
    public void fetchTimeline() {
        final Token token = new Token(
                "token",
                "secret");

        final Tweet[] tweets = new TwitterClient().getTimeline(token, 5, null, null, null, null, null, null);
        assertNotNull(tweets);
        assertEquals(5, tweets.length);

        for (Tweet tweet : tweets) {
            System.out.println(tweet.getUser().getScreenName() + " - " + tweet.getText());
        }
    }

}