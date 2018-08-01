package proactive.statements;

import proactive.core.Context;
import proactive.core.BaseStatement;
import proactive.core.Util;
import proactive.core.Statement;

import java.util.function.Predicate;
import java.util.function.Supplier;

import static proactive.core.ProactiveDriver.defaultDriver;

/**
 * The <code>Wait</code> {@link Statement} will check <code>next</code> signals against a {@link Predicate}.
 * If it passes, the value is sent onwards.
 *
 * By default, the {@link Predicate} is {@link Util#isTruthy(Object)}. It can be changed via the {@link #is},
 * {@link #isNot(Object)} and {@link #matches(Predicate)} methods.
 *
 * @param <O> The type being waited for.
 */
public class Wait<O> extends BaseStatement<O> {

    /**
     * Creates a {@link Statement} that will wait until the provided {@link Statement} provides a
     * value that passes the {@link Predicate}.
     *
     * @param requirement The {@link Statement} {@link Supplier}.
     * @param <O> The type being waited for.
     * @return The {@link Wait} instance.
     */
    public static <O> Wait<O> until(Statement<O> requirement) {
        return new Wait<>(requirement);
    }

    private final Statement<O> requirement;

    /**
     * Constructs a new instance
     * @param requirement
     */
    private Wait(Statement<O> requirement) {
        this.requirement = requirement;
    }

    /**
     * Specifies a {@link Predicate} to check values against.
     *
     * @param predicate The predicate.
     * @return The {@link WaitUntilMatches} instance.
     */
    public WaitUntilMatches<O> matches(Predicate<? super O> predicate) {
        return new WaitUntilMatches<>(requirement, predicate);
    }

    /**
     * Specifies the value to wait for.
     *
     * @param value The value to require.
     * @return The {@link WaitUntilMatches} instance.
     */
    public WaitUntilMatches<O> is(O value) {
        return matches((O check) -> Util.areEqual(check, value));
    }

    /**
     * Specifies the value the to ignore.
     *
     * @param value The value to ignore.
     * @return the {@link WaitUntilMatches} instance.
     */
    public WaitUntilMatches<O> isNot(O value) {
        return matches((O check) -> !Util.areEqual(check, value));
    }

    /**
     * @return the {@link Context}.
     */
    @Override
    public Context<O> get() {
        return defaultDriver().find(requirement.get(), Util::isTruthy);
    }

    public static class WaitUntilMatches<O> extends BaseStatement<O> {

        private final Statement<O> requirement;
        private final Predicate<? super O> predicate;

        private WaitUntilMatches(Statement<O> requirement, Predicate<? super O> predicate) {
            assert(requirement != null);
            assert(predicate != null);

            this.requirement = requirement;
            this.predicate = predicate;
        }

        /**
         * @return the {@link Context} instance for the statement.
         */
        @Override
        public Context<O> get() {
            return defaultDriver().find(requirement.get(), predicate);
        }
    }
}
