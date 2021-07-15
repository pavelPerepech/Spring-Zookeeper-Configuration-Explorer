package ru.zsc.util;

/**
 * Created by Pavel Perepech on.
 */
public class ZooAccessException extends RuntimeException {

    public ZooAccessException(String message) {
        super(message);
    }

    public ZooAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
