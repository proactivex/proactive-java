package proactive.statements

class TheSpec extends StatementSpecification {
    def "value"() {
        when:
        now(The.value(1))

        then:
        results == [1]
        error == null
        completed
    }

    def "series"() {
        when:
        now(The.series(1, 2, 3))

        then:
        results == [1, 2, 3]
        error == null
        completed
    }

    def "reactive publisher"() {
        when:
        now(The.publisher( Flux.just(1, 2, 3)))

        then:
        results == [1, 2, 3]
        error == null
        completed
    }

    def "flow publisher"() {
        when:
        now(The.publisher( AdapterSupport.toFlowPublisher(Flux.just(1, 2, 3))))

        then:
        results == [1, 2, 3]
        error == null
        completed
    }
}
