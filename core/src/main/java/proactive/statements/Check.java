package proactive.statements;

import proactive.core.Context;
import proactive.core.BaseStatement;
import proactive.core.Util;
import proactive.core.Statement;

import java.util.concurrent.Flow;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static proactive.core.ProactiveDriver.defaultDriver;

/**
 * Defines an {@link Check} > {@link Then then}/{@link Then.Otherwise otherwise} {@link Statement}.
 *
 * @param <I> The input type.
 */
public class Check<I> {

    public static <I> Check<I> that(Statement<I> statement) {
        return new Check<>(statement);
    }

    private final Statement<I> value;

    /**
     * Constructs a new {@link Check} instance, checking the specified <code>value</code>. Only the first
     * value of the supplied {@link Flow.Publisher} will be considered when checking the predicate.
     *
     * @param the This {@link Supplier} which will provide a {@link Flow.Publisher} to check.
     */
    private Check(Statement<I> the) {
        this.value = the;
    }

    /**
     * Checks if the value matches the {@link Predicate}.
     *
     * @param predicate The predicate.
     * @return The {@link Matches} instance.
     */
    public Matches<I> matches(Predicate<? super I> predicate) {
        return new Matches<>(value, predicate);
    }

    /**
     * Checks if the value matches the specified value.
     *
     * @param value The value to check for.
     * @return The {@link Matches} instance.
     */
    public Matches<I> is(I value) {
        return new Matches<>(this.value, (isValue) -> Util.areEqual(isValue, value));
    }

    /**
     * Checks that the value does not match the specified value.
     *
     * @param value The value to check for.
     * @return The {@link Matches} instance.
     */
    public Matches<I> isNot(I value) {
        return new Matches<>(this.value, (isValue) -> !Util.areEqual(isValue, value));
    }

    /**
     * Defines what happens if the value matches the predicate.
     *
     * @param execute This function to consult.
     * @return This new  {@link Then} instance.
     */
    public <O> Then<I, O> then(Function<? super I, Statement<O>> execute) {
        return new Then<>(value, Util::isTruthy, execute);
    }

    /**
     * Defines what {@link Statement} to execute if the value matches the predicate.
     *
     * @param statement The statement to run.
     * @return The
     */
    public <O> Then<I, O> then(Statement<O> statement) {
        return then(value -> statement);
    }

    /**
     * Specifies the predicate to check the value agains.
     *
     * @param <I> The type of value being checked.
     */
    public static class Matches<I> {

        private final Statement<I> value;
        private final Predicate<? super I> predicate;

        /**
         * Constructs a new {@link Matches} instance.
         *
         * @param value The value being checked.
         * @param predicate The predicate to check.
         */
        private Matches(Statement<I> value, Predicate<? super I> predicate) {
            this.value = value;
            this.predicate = predicate;
        }

        /**
         * Defines what happens if the value matches the predicate.
         *
         * @param doThis This function to consult.
         * @return This new {@link Check.Then} instance.
         */
        public <O> Then<I, O> then(Function<? super I, Statement<O>> doThis) {
            return new Then<>(value, predicate, doThis);
        }

        /**
         * Defines what happens if the value matches the predicate.
         *
         * @param send The supplied {@link Flow.Publisher} values will be sent onwards.
         * @return The new {@link Check.Then} instance.
         */
        public <O> Then<I, O> then(Statement<O> send) {
            return then(value -> send);
        }
    }

    /**
     * Defines what happens when the {@link Check} statement passes the predicate.
     *
     * @param <X> This input value type.
     * @param <Y> This output value type.
     */
    public static class Then<X, Y> extends BaseStatement<Y> {

        private final Statement<X> value;
        private final Predicate<? super X> predicate;
        private final Function<? super X, Statement<Y>> then;

        /**
         * Constructor for the {@link Check}.Then.
         *
         * @param value This {@link Supplier} from the {@link Check} statement.
         * @param predicate This predicate function to check the value with.
         * @param then This {@link Function} to execute when the {@link Check} predicate succeeds.
         */
        private Then(Statement<X> value, Predicate<? super X> predicate, Function<? super X, Statement<Y>> then) {
            this.value = value;
            this.predicate = predicate;
            this.then = then;
        }

        /**
         * Defines what happens if the {@link Check} predicate fails. This provided {@link Function} will be passed
         * the result of the {@link Check} statement and should return a {@link Flow.Publisher} that will re
         *
         * @param execute This function to execute if the {@link Check} value does not pass the predicate.
         * @return This new {@link Otherwise} {@link Statement}.
         */
        public Otherwise<X, Y> otherwise(Function<? super X, Statement<Y>> execute) {
            return new Otherwise<>(value, predicate, then, execute);
        }

        /**
         * Defines what to send if the {@link Check} predicate fails.
         *
         * @param send The {@link Flow.Publisher} {@link Supplier}.
         * @return The {@link Otherwise} instance.
         */
        public Otherwise<X, Y> otherwise(Statement<Y> send) {
            return otherwise(value -> send);
        }

        /**
         * @return The {@link Flow.Publisher} result.
         */
        @Override
        public Context<Y> get() {
            return defaultDriver().flatMap(value.get(), next -> {
                if (predicate.test(next)) {
                    return then.apply(next).get();
                } else {
                    return defaultDriver().empty();
                }
            });
        }

        /**
         * Provides the {@link Function} or {@link Flow.Publisher} {@link Supplier} to perform if the check does not pass.
         * The output type of the {@link Otherwise} must match that specified in the previous {@link Then}.
         *
         * This is created by calling one of the {@link Then#otherwise(Function)} methods.
         *
         * @param <X> The input type.
         * @param <Y> The output type.
         */
        public static final class Otherwise<X, Y> extends BaseStatement<Y> {

            private final Statement<X> value;
            private final Predicate<? super X> predicate;
            private final Function<? super X, Statement<Y>> then;
            private final Function<? super X, Statement<Y>> otherwise;

            /**
             * Constructs a new {@link Otherwise} instance.
             *
             * @param value The value to check.
             * @param predicate The predicate to check with.
             * @param then The function to execute if the value passes the predicate.
             * @param otherwise The function to execute if the value does not pass the predicate.
             */
            private Otherwise(Statement<X> value, Predicate<? super X> predicate, Function<? super X, Statement<Y>> then, Function<? super X, Statement<Y>> otherwise) {
                this.value = value;
                this.predicate = predicate;
                this.then = then;
                this.otherwise = otherwise;
            }

            /**
             * Returns the {@link Flow.Publisher} instance for the {@link Check} statement.
             *
             * @return The {@link Flow.Publisher}.
             */
            @Override
            public Context<Y> get() {
                return defaultDriver().flatMap(value.get(), next -> {
                    if (predicate.test(next)) {
                        return then.apply(next).get();
                    } else {
                        return otherwise.apply(next).get();
                    }
                });
            }
        }

    }
}
