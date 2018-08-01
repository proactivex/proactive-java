package proactive.statements

import spock.lang.Unroll

class CombinationSpec extends StatementSpecification {
    @Unroll
    def "try resolving check that #a is #b then #c otherwise #d function outputs #output"() {
        when:
        now(Try.resolving(
                Check.that(The.value(a)).is(b)
                        .then(The.value(c))
                        .otherwise(The.value(d))
        ).then({ The.value(it * 10) }))

        then:
        results == [output]
        error == null
        completed

        where:
        a     | b    | c | d || output
        true  | true | 1 | 2 || 10
        false | true | 1 | 2 || 20
    }
}
