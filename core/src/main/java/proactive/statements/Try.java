package proactive.statements;

import proactive.core.Context;
import proactive.core.BaseStatement;
import proactive.core.Statement;

import java.util.function.Function;

import static proactive.core.ProactiveDriver.defaultDriver;

/**
 * Tries to resolve a {@link Statement}. Each <code>onNext</code> signal is sent to the
 * {@link #then(Function) function} {@link #then(Statement) statement} <code>then</code> (if defined).
 * <p>
 * For example:
 * <code>
 * Try.resolving(The.series(1, 2, 3))
 *     .then(int value -> return The.value(value * 10)
 *     .now({ System.out.println("value = " + value});
 * </code>
 *
 * @param <O> The type of value the statement being resolved produces.
 */
public class Try<O> extends BaseStatement<O> {

    /**
     * The returned {@link Try} statement will subscribe to the supplied {@link Statement} and pass on the
     * results to the next stage.
     *
     * @param statement The statement to resolve.
     * @param <O>       The type of values.
     * @return The {@link Try} statement.
     * @see #then(Function)
     * @see #then(Statement)
     */
    public static <O> Try<O> resolving(Statement<O> statement) {
        return new Try<>(statement);
    }

    /**
     * The {@link Statement} to try resolving.
     */
    private final Statement<O> that;

    private Try(Statement<O> that) {
        this.that = that;
    }

    public <T> Then<O, T> then(Function<? super O, Statement<T>> execute) {
        return new Then<>(this, execute);
    }

    public <T> Then<O, T> then(Statement<T> send) {
        return then(value -> send);
    }

    @Override
    public Context<O> get() {
        return that.get();
    }

    public static class Then<I, O> extends BaseStatement<O> {

        private final Statement<I> doThat;
        private final Function<? super I, Statement<O>> thenThat;

        private Then(Statement<I> doThat, Function<? super I, Statement<O>> thenThat) {
            this.doThat = doThat;
            this.thenThat = thenThat;
        }

        public <T> Then<O, T> then(Function<? super O, Statement<T>> execute) {
            return new Then<>(this, execute);
        }

        public <T> Then<O, T> then(Statement<T> send) {
            return then(value -> send);
        }

        @Override
        public Context<O> get() {
            return defaultDriver().flatMap(doThat.get(), value -> thenThat.apply(value).get());
        }
    }
}
