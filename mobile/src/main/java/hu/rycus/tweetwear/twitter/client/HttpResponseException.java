package hu.rycus.tweetwear.twitter.client;

public class HttpResponseException extends RuntimeException {

    private final int code;
    private final String httpMessage;

    public HttpResponseException(final int code, final String message) {
        super("HTTP Request was unsuccessful");
        this.code = code;
        this.httpMessage = message;
    }

    public int getCode() {
        return code;
    }

    public String getHttpMessage() {
        return httpMessage;
    }

    @Override
    public String toString() {
        return String.format("%s: %d - %s", super.toString(), this.code, this.httpMessage);
    }

}
