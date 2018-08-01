package proactive.statements

import proactive.core.Statement
import spock.lang.Specification

abstract class StatementSpecification extends Specification {
    def results = []
    Throwable error = null
    boolean completed = false

    def now(Statement<?> statement) {
        statement.now(
                { results << it },
                { error = it },
                { completed = true }
        )
    }

}
