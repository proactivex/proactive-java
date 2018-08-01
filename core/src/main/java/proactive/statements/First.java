package proactive.statements;

import org.reactivestreams.Publisher;
import proactive.core.Context;
import proactive.core.BaseStatement;
import proactive.core.Statement;

import java.util.function.Supplier;

import static proactive.core.ProactiveDriver.defaultDriver;

/**
 * A {@link Statement} requiring that only the first value is sent onwards.
 * It may complete before a value is sent without error.
 *
 * @param <O> the value type.
 */
public class First<O> extends BaseStatement<O> {

    /**
     * Declares that the first value from the supplied {@link Publisher} will be sent, then complete.
     *
     * This may be a lambda, for example:
     *
     * <code>
     *     First.of(() -> myFlowPublisher)
     * </code>
     *
     * Note that {@link Statement}s also implement {@link Supplier}, so you could also do this:
     *
     * <code>
     *     First.of(This.List(1, 2, 3))
     * </code>
     *
     * @param the The {@link Publisher} {@link Supplier}.
     * @param <O> The type
     * @return THe {@link First} instance.
     */
    public static <O> First<O> of(Statement<O> the) {
        return new First<>(the);
    }

    /**
     * The {@link Publisher} source.
     */
    private final Statement<O> source;

    /**
     * Constructs a new {@link First} instance
     * @param source Provides the {@link Publisher}.
     */
    private First(Statement<O> source) {
        this.source = source;
    }

    @Override
    public Context<O> get() {
        return defaultDriver().first(source.get());
    }
}
