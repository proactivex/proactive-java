package proactive.core;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.concurrent.Flow;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * This interface provides cross-implementation support for working with specific Reactive Stream APIs.
 */
public interface ProactiveDriver {

    /**
     * Retrieves the default {@link ProactiveDriver} instance. If not is available a {@link IllegalStateException}
     * is thrown.
     *
     * @return The default {@link ProactiveDriver}.
     * @throws IllegalStateException if none is available.
     */
    static ProactiveDriver defaultDriver() {
        return Find.defaultDriver();
    }

    /**
     * Sets the default {@link ProactiveDriver} instance. This will override any existing value, including those
     * auto-loaded via the {@link ServiceLoader} API.
     *
     * @param driver The {@link ProactiveDriver} to make the default driver.
     */
    static void setDefaultDriver(ProactiveDriver driver) {
        Find.setDefaultDriver(driver);
    }

    /**
     * Creates a {@link Context} which will <code>complete</code> immediately on subscription.
     *
     * @param <T> The published type.
     * @return The {@link Publisher}.
     */
    <T> Context<T> empty();

    /**
     * Returns a {@link Publisher} which will only pass <code>onNext</code> signals which match the provided
     * {@link Predicate}
     * @param <T>
     * @param context
     * @param predicate
     * @return
     */
    <T> Context<T> filter(Context<T> context, Predicate<? super T> predicate);

    /**
     * Returns the first <code>onNext</code> signal that matches the predicate then sends <code>onComplete</code> immediately.
     *
     * @param <T> The type of value being published/checked.
     * @param context The {@link Publisher} to adapt.
     * @param predicate The {@link Predicate} to check.
     * @return The new  {@link Publisher} instance.
     */
    <T> Context<T> find(Context<T> context, Predicate<? super T> predicate);

    /**
     * Returns a {@link Publisher} that will only pass on at most the first <code>onNext</code> signal, followed
     * immediately by a <code>onComplete</code> signal.
     *
     * @param <T> The type of value being sent.
     * @param context The {@link Publisher} to adapt.
     * @return The adapted {@link Publisher}.
     */
    <T> Context<T> first(Context<T> context);

    /**
     * Performs a <code>flatMap</code> operation on the results of the provided <code>publisher</code> when
     * subscribed. The function is passed each output of the provided {@link Publisher} and the function should
     * return a different {@link Publisher} which will be flattened and each resulting <code>onNext</code> sent
     * onwards.
     *
     * @param <I> The type produced by the publisher.
     * @param <T> The type produced by the flattened publisher.
     * @param context The publisher to adapt.
     * @param mapper The mapper function.
     * @return The {@link Publisher}. If <code>null</code> is returned then the results of {@link #empty()} are
     * returned instead.
     */
    <I, T> Context<T> flatMap(Context<I> context, Function<? super I, ? extends Context<T>> mapper);

    /**
     * Adapts a single value into a {@link Publisher}. the value will be sent as an <code>onNext</code> signal.
     * If an API has a <code>0|1</code> publisher, it should be produced here.
     *
     * @param <T>   The type of value being adapted.
     * @param value The value to adapt to a {@link Publisher}.
     * @return The {@link Publisher}.
     */
    <T> Context<T> just(T value);

    /**
     * Adapts a list of values into a {@link Publisher}. Each will be sent as an <code>onNext</code> signal in
     * the order provided.
     *
     * @param <T>    The type of the value list.
     * @param values The list values to send.
     * @return The {@link Publisher} instance.
     */
    @SuppressWarnings("unchecked")
    <T> Context<T> just(T... values);

    /**
     * Adds a <code>log</code> transformation to the provided {@link Publisher}, which will output
     *
     * @param <T>       The value type for the {@link Publisher}.
     * @param context The publisher to <code>log</code> on.
     * @param category  The category to report it with.
     * @return The <code>logged</code> {@link Publisher}.
     */
    <T> Context<T> log(Context<T> context, String category);

    /**
     * Requests that the provided {@link Subscriber} is subscribed to the provided {@link Publisher}.
     *  @param <T>        The type of value being published/subscribed to.
     * @param context  The publisher to subscribe to.
     * @param subscriber The subscriber wanting to subscribe.
     */
    <T> void subscribe(Context<T> context, Subscriber<? super T> subscriber);

    /**
     * Requests that the provided {@link Publisher} is subscribed to with no specific callback or
     * {@link Subscriber} instance.
     *  @param <T>       The type being published.
     * @param context The {@link Publisher}.
     */
    <T> void subscribe(Context<T> context);

    /**
     * Requests that the provided {@link Publisher} is subscribed with the provided callbacks. All callbacks
     * may be <code>null</code> without creating an error.
     * @param <T>               The type of value being published.
     * @param context         The {@link Publisher} to subscribe to.
     * @param nextConsumer      This {@link Consumer} will be sent any <code>onNext</code> signals.
     * @param errorConsumer     This {@link Consumer} will be sent the <code>onError</code> signal, if it occurs.
     * @param completeConsumer  This {@link Runnable} will be sent the <code>onComplete</code> signal, if it occurs.
     * @param subscribeConsumer This {@link Consumer} will be sent the <code>onSubscribe</code> signal.
     */
    <T> void subscribe(Context<T> context, Consumer<? super T> nextConsumer,
                       Consumer<? super Throwable> errorConsumer, Runnable completeConsumer,
                       Runnable subscribeConsumer);

    <T> Context<T> toContext(Publisher<T> publisher);

    <T> Context<T> toContext(Flow.Publisher<T> flowPublisher);

    <T> Flow.Publisher<T> toFlowPublisher(Context<T> context);

    <T> Publisher<T> toPublisher(Context<T> context);

    /**
     * Finds the current "default" {@link ProactiveDriver}. The default can be provided manually via the
     * {@link #setDefaultDriver(ProactiveDriver)} method, or if none is provided it will search for the first
     * adaptor found by a {@link ServiceLoader}.
     * <p>
     * Typically an implementation of {@link ProactiveDriver} will be provided automatically by implementation libraries
     * such as <code>proactive.reactor3</code>. If you have multiple implementation libraries in your classpath,
     * an arbitrary implementation is selected by default. That can be overridden by calling
     * {@link #setDefaultDriver(ProactiveDriver)}.
     */
    final class Find {

        private Find() {
        }

        private static ServiceLoader<ProactiveDriver> driverLoader = ServiceLoader.load(ProactiveDriver.class);

        private static ProactiveDriver defaultDriver;

        /**
         * Provides an {@link Iterator} listing all available {@link ProactiveDriver} implementations that are available
         * via the {@link ServiceLoader} API.
         *
         * @return the {@link Iterator}.
         */
        public static Iterator<ProactiveDriver> allDrivers() {
            return driverLoader.iterator();
        }

        /**
         * Finds the default {@link ProactiveDriver} and scans the {@link ServiceLoader} if it hasn't been found already.
         *
         * @return The {@link ProactiveDriver}
         */
        private static ProactiveDriver findDefaultDriver() {
            if (defaultDriver == null) {
                driverLoader.findFirst().ifPresent(firstDriver -> defaultDriver = firstDriver);
            }
            return defaultDriver;
        }

        /**
         * Checks if the default {@link ProactiveDriver} is available via the {@link #defaultDriver()} method.
         *
         * @return <code>true</code> if a default {@link ProactiveDriver}.
         */
        public static boolean hasDefaultDriver() {
            return findDefaultDriver() != null;
        }

        /**
         * Returns the current {@link ProactiveDriver}. If no defaultDriver has been {@link #setDefaultDriver(ProactiveDriver) set} it will
         * search for an implementation of {@link ProactiveDriver} via the standard {@link ServiceLoader} API.
         *
         * @return The {@link ProactiveDriver} implementation, or <code>null</code> if one cannot be found.
         * @see #setDefaultDriver(ProactiveDriver)
         */
        public static ProactiveDriver defaultDriver() {
            if (defaultDriver == null) {
                findDefaultDriver();
                if (defaultDriver == null) {
                    throw new IllegalStateException("No default ProactiveDriver found.");
                }
            }
            return defaultDriver;
        }

        /**
         * Sets the current {@link ProactiveDriver} implementation. This will override any default service provided via the
         * {@link ServiceLoader} API. If the defaultDriver is set to <code>null</code>, then the default implementation via the
         * {@link ServiceLoader} API will be found again.
         *
         * @param driver The new {@link ProactiveDriver}.
         */
        public static void setDefaultDriver(ProactiveDriver driver) {
            defaultDriver = driver;
        }

    }
}
