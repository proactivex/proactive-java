package proactive.statements

class WaitSpec extends StatementSpecification {

    def "wait until value true"() {
        when:
        now(Wait.until(The.series(false, true, true, false)))

        then:
        results == [true]
        error == null
        completed
    }

    def "wait until value unmatched"() {
        when:
        now(Wait.until(The.series( false, false, false)))

        then:
        results == []
        error == null
        completed
    }

    def "wait until value is false"() {
        when:
        now(Wait.until(The.series(true, true, false, false, true)).is(false))

        then:
        results == [false]
        error == null
        completed
    }

    def "wait until value is not true"() {
        when:
        now(Wait.until(The.series(true, true, false, false, true)).isNot(true))

        then:
        results == [false]
        error == null
        completed
    }


    def "wait until value matches"() {
        when:
        now(Wait.until(The.series(2, 4, 6, 8, 10)).matches {it % 3 == 0})

        then:
        results == [6]
        error == null
        completed
    }
}
