package hu.rycus.tweetwear.twitter.client;

import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;

import java.io.IOException;

import hu.rycus.tweetwear.common.util.Mapper;
import hu.rycus.tweetwear.common.util.Value;

public class RequestBuilder {

    private final OAuthRequest request;

    private RequestBuilder(final Verb verb, final String baseUri) {
        this.request = new OAuthRequest(verb, baseUri);
    }

    public RequestBuilder queryParam(final String key, final Object value) {
        if (key == null || value == null) return this;
        request.addQuerystringParameter(key, value.toString());
        return this;
    }

    public RequestBuilder bodyParam(final String key, final Object value) {
        if (key == null || value == null) return this;
        request.addBodyParameter(key, value.toString());
        return this;
    }

    public RequestBuilder oauthParam(final String key, final Object value) {
        if (key == null || value == null) return this;
        request.addOAuthParameter(key, value.toString());
        return this;
    }

    public ResponseBuilder send(final OAuthService service, final Token accessToken) {
        service.signRequest(accessToken, request);
        return new ResponseBuilder(request.send());
    }

    public static RequestBuilder get(final String baseUri) {
        return new RequestBuilder(Verb.GET, baseUri);
    }

    public static RequestBuilder post(final String baseUri) {
        return new RequestBuilder(Verb.POST, baseUri);
    }

    public static RequestBuilder head(final String baseUri) {
        return new RequestBuilder(Verb.HEAD, baseUri);
    }

    public class ResponseBuilder {

        private final Response response;

        private ResponseBuilder(final Response response) {
            checkHttpStatus(response);
            this.response = response;
        }

        public ResponseBuilder header(final String name, final Value<String> value) {
            final String headerValue = response.getHeader(name);
            value.set(headerValue);
            return this;
        }

        public ResponseBuilder checkStatus(final int expected, final String errorMessage) {
            final int code = response.getCode();
            if (code != expected) {
                throw new IllegalArgumentException(
                        String.format("%s (Status: %d)", errorMessage, code));
            }
            return this;
        }

        public <T> T respond(final Class<T> responseClass) throws IOException {
            return Mapper.readObject(response.getStream(), responseClass);
        }

    }

    private static void checkHttpStatus(final Response response) {
        if (response.getCode() != 200) {
            throw new IllegalArgumentException(
                    String.format("HTTP Request was unsuccessful: %d - %s",
                            response.getCode(), response.getMessage()));
        }
    }

}
