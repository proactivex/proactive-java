package proactive.statements

class TrySpec extends StatementSpecification {
    def "try value"() {
        when:
        now(Try.resolving(The.value(1)))

        then:
        results == [1]
        error == null
        completed
    }

    def "try value then value"() {
        when:
        now(Try.resolving(The.value(1)).then(The.value("one")))

        then:
        results == ["one"]
        error == null
        completed
    }

    def "try value then function"() {
        when:
        now(Try.resolving(The.value(1)).then({The.value(it * 10)}))

        then:
        results == [10]
        error == null
        completed
    }

    def "try series then function series"() {
        when:
        now(Try.resolving(The.series(1, 2)).then({The.series(it, it * 10, it * 100)}))

        then:
        results == [1, 10, 100, 2, 20, 200]
        error == null
        completed
    }

    def "try value then function then function"() {
        when:
        now(Try.resolving(The.series(1, 2)).then({The.value(it * 10)}).then({The.value(it * 5)}))

        then:
        results == [50, 100]
        error == null
        completed
    }

    def "try value then value then value"() {
        when:
        now(Try.resolving(The.value(1)).then(The.value(2)).then(The.value(3)))

        then:
        results == [3]
        error == null
        completed
    }
}
