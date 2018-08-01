package proactive.core;

import java.time.Duration;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A {@link Statement} is a base type for statements that may execute asynchronously.
 * Once constructed, a statement is triggered by calling one of the {@link #now()} or {@link #after(Duration)}
 * methods.
 *
 * @param <O> The type of value produced by the {@link Statement}.
 */
public interface Statement<O> extends Supplier<Context<O>> {
    /**
     * Requests the {@link Statement} to execute immediately.
     */
    void now();

    /**
     * Requests the {@link Statement} to execute immediately.
     *
     * @param nextConsumer Handles any <code>onNext</code> signals. May be <code>null</code>.
     */
    default void now(Consumer<? super O> nextConsumer) {
        now(nextConsumer, null);
    }

    /**
     * Requests the {@link Statement} to execute immediately.
     *
     * @param nextConsumer Handles any <code>onNext</code> signals. May be <code>null</code>.
     * @param errorConsumer Handles the <code>onError</code> signal if it occurs. May be <code>null</code>.
     */
    default void now(Consumer<? super O> nextConsumer, Consumer<? super Throwable> errorConsumer) {
        now(nextConsumer, errorConsumer, null);
    }

    /**
     * Requests the {@link Statement} to execute immediately.
     *
     * @param nextConsumer Handles any <code>onNext</code> signals. May be <code>null</code>.
     * @param errorConsumer Handles the <code>onError</code> signal if it occurs. May be <code>null</code>.
     * @param completeConsumer Handles the <code>onComplete</code> signal if it occurs. May be <code>null</code>.
     */
    default void now(Consumer<? super O> nextConsumer, Consumer<? super Throwable> errorConsumer, Runnable completeConsumer) {
        now(nextConsumer, errorConsumer, completeConsumer, null);
    }

    /**
     * Requests the {@link Statement} to execute immediately.
     *
     * @param nextConsumer Handles any <code>onNext</code> signals. May be <code>null</code>.
     * @param errorConsumer Handles the <code>onError</code> signal if it occurs. May be <code>null</code>.
     * @param completeConsumer Handles the <code>onComplete</code> signal if it occurs. May be <code>null</code>.
     * @param subscribeConsumer Handles <code>onSubscribe</code> signals. May be <code>null</code>.
     */
    void now(Consumer<? super O> nextConsumer, Consumer<? super Throwable> errorConsumer, Runnable completeConsumer, Runnable subscribeConsumer);

    /**
     * Requests the {@link Statement} to execute after the specified delay.
     *
     * @param delay The amount of time to delay.
     */
    void after(Duration delay);

    /**
     * Requests the {@link Statement} to execute after the specified delay.
     *
     * @param delay The amount of time to delay.
     * @param nextConsumer Handles any <code>onNext</code> signals. May be <code>null</code>.
     * @param errorConsumer Handles the <code>onError</code> signal if it occurs. May be <code>null</code>.
     */
    default void after(Duration delay, Consumer<? super O> nextConsumer, Consumer<? super Throwable> errorConsumer) {
        after(delay, nextConsumer, errorConsumer, null);
    }

    /**
     * Requests the {@link Statement} to execute after the specified delay.
     *
     * @param delay The amount of time to delay.
     * @param nextConsumer Handles any <code>onNext</code> signals. May be <code>null</code>.
     * @param errorConsumer Handles the <code>onError</code> signal if it occurs. May be <code>null</code>.
     * @param completeConsumer Handles the <code>onComplete</code> signal if it occurs. May be <code>null</code>.
     */
    default void after(Duration delay, Consumer<? super O> nextConsumer, Consumer<? super Throwable> errorConsumer, Runnable completeConsumer) {
        after(delay, nextConsumer, errorConsumer, completeConsumer, null);
    }

    /**
     * Requests the {@link Statement} to execute after the specified delay.
     *  @param delay The amount of time to delay.
     * @param nextConsumer Handles any <code>onNext</code> signals. May be <code>null</code>.
     * @param errorConsumer Handles the <code>onError</code> signal if it occurs. May be <code>null</code>.
     * @param completeConsumer Handles the <code>onComplete</code> signal if it occurs. May be <code>null</code>.
     * @param subscribeConsumer Handles <code>onSubscribe</code> signals. May be <code>null</code>.
     */
    void after(Duration delay, Consumer<? super O> nextConsumer, Consumer<? super Throwable> errorConsumer, Runnable completeConsumer, Runnable subscribeConsumer);
}