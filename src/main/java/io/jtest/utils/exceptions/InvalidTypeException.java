package io.jtest.utils.exceptions;

/**
 * Thrown internally by the matchers when an input cannot be converted to the type the
 * matcher needs (e.g. a string that does not parse as JSON or XML). It is a checked
 * exception; the public {@link io.jtest.utils.matcher.ObjectMatcher} facade catches it
 * and rethrows as a {@link RuntimeException} to keep the call sites uncluttered.
 *
 * <p>Test code does not normally see this type directly — encountering it usually means
 * a fixture is malformed.
 */
public class InvalidTypeException extends Exception {
    /** Creates an exception with a description of the type-conversion failure. */
    public InvalidTypeException(String message) {
        super(message);
    }

    /** Creates an exception that wraps the underlying parser/converter failure. */
    public InvalidTypeException(String message, Throwable cause) {
        super(message, cause);
    }
}
