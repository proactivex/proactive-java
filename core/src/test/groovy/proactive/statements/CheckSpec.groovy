package proactive.statements

import spock.lang.Unroll

class CheckSpec extends StatementSpecification {
    @Unroll
    def "check that #a then #output"() {
        when:
        now(Check.that(The.value(a)).then(The.value("true")))

        then:
        results == output
        error == null
        completed

        where:
        a    || output
        true  | ["true"]
        false | []
        null  | []
    }

    @Unroll
    def "check that #a is #b then/otherwise is '#result'"() {
        when:
        now(
                Check.that(The.value(a)).is(b)
                        .then(The.value("true"))
                        .otherwise(The.value("false"))
        )

        then:
        results == [result]
        error == null
        completed

        where:
        a     | b     || result
        true  | true  || "true"
        true  | false || "false"
        false | true  || "false"
        false | false || "true"
    }

    @Unroll
    def "check that #a is not #b then/otherwise is '#result'"() {
        when:
        now(
                Check.that(The.value(a)).isNot(b)
                        .then(The.value("true"))
                        .otherwise(The.value("false"))
        )

        then:
        results == [result]
        error == null
        completed

        where:
        a     | b     || result
        true  | true  || "false"
        true  | false || "true"
        false | true  || "true"
        false | false || "false"
    }

    def "check that then function"() {
        when:
        now(Check.that(The.value("World")).then({ The.value("Hello ${it}") }))

        then:
        results == ["Hello World"]
        error == null
        completed
    }

    def "check that matches then"() {
        when:
        now(Check.that(The.value(10)).matches({ it % 2 == 0 }).then(The.value("success")))

        then:
        results == ["success"]
        error == null
        completed
    }
}