package proactive.core;

import java.time.Duration;
import java.util.function.Consumer;

import static proactive.core.ProactiveDriver.defaultDriver;

/**
 * An abstract base class implementation of {@link Statement}.
 *
 * @param <O> The type of value produced by the statement.
 */
public abstract class BaseStatement<O> implements Statement<O> {

    @Override
    public void now() {
        defaultDriver().subscribe(get());
    }

    /**
     * Executes
     *
     * @param nextConsumer Handles each 'next' value as it arrives.
     * @param errorConsumer Handles any exceptions.
     * @param completeConsumer Called once the {@link Statement} completes.
     * @param subscribeConsumer Handles the 'onSubscribe' signal.
     */
    @Override
    public void now(Consumer<? super O> nextConsumer, Consumer<? super Throwable> errorConsumer, Runnable completeConsumer, Runnable subscribeConsumer) {
        defaultDriver().subscribe(get(), nextConsumer, errorConsumer, completeConsumer, subscribeConsumer);
    }

    @Override
    public void after(Duration delay) {
        // TODO: Schedule the delay
        now();
    }

    @Override
    public void after(Duration delay, Consumer<? super O> nextConsumer, Consumer<? super Throwable> errorConsumer, Runnable completeConsumer, Runnable subscribeConsumer) {
        // TODO: Schedule the delay
        now(nextConsumer, errorConsumer, completeConsumer, subscribeConsumer);
    }
}
