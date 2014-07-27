package hu.rycus.tweetwear.common.util;

import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;

public class Mapper {

    private static final String TAG = Mapper.class.getSimpleName();

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private Mapper() {} // singleton

    public static <T> T readObject(final byte[] data, final Class<T> targetClass) {
        try {
            return OBJECT_MAPPER.readValue(data, targetClass);
        } catch (IOException ex) {
            Log.e(TAG, String.format("Failed to read object from byte data"), ex);
        }

        return null;
    }

    public static <T> T readObject(final InputStream inputStream, final Class<T> targetClass) {
        try {
            return OBJECT_MAPPER.readValue(inputStream, targetClass);
        } catch (IOException ex) {
            Log.e(TAG, String.format("Failed to read object from input stream"), ex);
        }

        return null;
    }

    public static byte[] writeObject(final Object object) {
        try {
            return OBJECT_MAPPER.writeValueAsBytes(object);
        } catch (IOException ex) {
            Log.e(TAG, String.format("Failed to write object to byte data"), ex);
        }

        return null;
    }

}
