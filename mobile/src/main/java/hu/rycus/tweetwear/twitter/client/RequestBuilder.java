package hu.rycus.tweetwear.twitter.client;

import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;

import java.io.IOException;

import hu.rycus.rtweetwear.common.util.Mapper;

public class RequestBuilder {

    private final StringBuilder requestUri;

    private boolean hasQueryParameter = false;

    private RequestBuilder(final String baseUri) {
        this.requestUri = new StringBuilder(baseUri);
    }

    public RequestBuilder param(final String key, final Object value) {
        if (key == null || value == null) return this;

        if (hasQueryParameter) {
            requestUri.append("&");
        } else {
            requestUri.append("?");
            hasQueryParameter = true;
        }

        requestUri.append(key).append("=").append(value);

        return this;
    }

    public ResponseBuilder get(final OAuthService service, final Token accessToken) {
        return build(service, accessToken, Verb.GET);
    }

    private ResponseBuilder build(final OAuthService service, final Token accessToken,
                                  final Verb verb) {
        final OAuthRequest request = new OAuthRequest(verb, requestUri.toString());
        service.signRequest(accessToken, request);
        return new ResponseBuilder(request.send());
    }

    public static RequestBuilder start(final String baseUri) {
        return new RequestBuilder(baseUri);
    }

    public class ResponseBuilder {

        private final Response response;

        private ResponseBuilder(final Response response) {
            this.response = response;
        }

        public <T> T respond(final Class<T> responseClass) throws IOException {
            return Mapper.readObject(response.getStream(), responseClass);
        }

    }

}
