package proactive.statements;

import org.reactivestreams.Publisher;
import proactive.core.Context;
import proactive.core.BaseStatement;
import proactive.core.Statement;

import java.util.concurrent.Flow;
import java.util.function.Supplier;

import static proactive.core.ProactiveDriver.defaultDriver;

/**
 * A {@link Statement} which adapts other values to a {@link Statement}.
 *
 * @param <O> The type of value being adapted.
 */
public class The<O> extends BaseStatement<O> {

    /**
     * Returns a {@link Statement} that will provide the single value when resolved.
     *
     * @param of The value to resolve.
     * @param <O> The value type.
     * @return The {@link The} instance.
     */
    public static <O> The<O> value(O of) {
        return new The<>(defaultDriver().just(of));
    }

    /**
     * Returns a {@link Statement} that will send each item as an individual <code>onNext</code> signal.
     *
     * @param ofValues The list of values.
     * @param <O> The type of the values.
     * @return The {@link The} instance.
     */
    @SafeVarargs
    public static <O> The<O> series(O... ofValues) {
        return new The<>(defaultDriver().just(ofValues));
    }

    /**
     * Returns a {@link Statement} that will trigger the specified {@link Flow.Publisher} when executed.
     *
     * @param publisher The publisher to adapt.
     * @param <O> The type of value the {@link Flow.Publisher} produces.
     * @return The {@link The} instance.
     */
    public static <O> The<O> publisher(Flow.Publisher<O> publisher) {
        return new The<>(defaultDriver().toContext(publisher));
    }

    /**
     * Returns a {@link Statement} that will trigger the specified Reactive Streams {@link Publisher} when executed.
     *
     * @param publisher The publisher to adapt.
     * @param <O> The type of the value {@link Publisher} produces.
     * @return The {@link The} instance.
     */
    public static <O> The<O> publisher(Publisher<O> publisher) {
        return new The<>(defaultDriver().toContext(publisher));
    }

    /**
     * The {@link Flow.Publisher} provider.
     */
    private Context<O> context;

    /**
     * Constructs a new {@link The} instance with the specified {@link Flow.Publisher} {@link Supplier}.
     *
     * @param context The {@link Flow.Publisher}.
     */
    private The(Context<O> context) {
        this.context = context;
    }

    /**
     * Returns the {@link Flow.Publisher} to be executed. This may or may not be a new instance each time it is called,
     * depending on the source type.
     *
     * @return The {@link Flow.Publisher}.
     */
    @Override
    public Context<O> get() {
        return context;
    }
}
