package proactive.core;

import java.util.concurrent.Flow;

public final class Util {

    /**
     * A predicate function which will return <code>true</code> if the <code>value</code> is not <code>null</code> and
     * not <code>false</code>.
     *
     * @param value This value to test.
     * @param <T>   This value type.
     * @return <code>true</code> if the value is "truthy".
     */
    public static <T> boolean isTruthy(T value) {
        if (value instanceof Boolean) {
            return value != Boolean.FALSE;
        } else {
            return value != null;
        }
    }

    /**
     * A predicate function which will return <code>true</code> if the <code>value</code> is <code>null</code> or
     * <code>false</code>.
     *
     * @param value This value to test.
     * @param <T>   This value type.
     * @return <code>true</code> if the value is "falsy".
     */
    public static <T> boolean isFalsy(T value) {
        return !isTruthy(value);
    }

    public static <T> boolean areEqual(T a, T b) {
        return a == b || a != null && a.equals(b);
    }

    public static <O> Flow.Subscriber<O> defaultSubscriber() {
        return null;
    }
}
