package hu.rycus.tweetwear.common.util;

public class Value<T> {

    private T value;

    public Value() {
        this(null);
    }

    public Value(final T initialValue) {
        this.value = value;
    }

    public T get() {
        return value;
    }

    public void set(final T value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return String.format("Value(%s)", value);
    }

}
