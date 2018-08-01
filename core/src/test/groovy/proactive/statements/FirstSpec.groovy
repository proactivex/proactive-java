package proactive.statements

import org.reactivestreams.Publisher
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription

class FirstSpec extends StatementSpecification {
    Subscriber<Integer> subscriber = null
    Subscription subscription = Mock()
    Publisher<Integer> publisher = Mock() {
        subscribe(_ as Subscriber) >> { Subscriber<Integer> s ->
            subscriber = s
            s.onSubscribe(subscription)
        }
    }

    def "first of one"() {
        when:
        now(First.of(The.publisher(publisher)))

        then:
        1 * subscription.request(_)
        results == []
        error == null
        !completed

        when:
        subscriber.onNext(1)

        then:
        results == [1]
        error == null
        completed

        when:
        subscriber.onComplete()

        then:
        results == [1]
        error == null
        completed
    }

    def "first of several"() {
        when:
        now(First.of(The.publisher(publisher)))

        then:
        1 * subscription.request(_)
        results == []
        error == null
        !completed

        when:
        subscriber.onNext(1)

        then:
        results == [1]
        error == null
        completed

        when:
        subscriber.onNext(2)

        then:
        results == [1]
        error == null
        completed

        when:
        subscriber.onComplete()

        then:
        results == [1]
        error == null
        completed
    }

    def "first of none"() {
        when:
        now(First.of(The.publisher(publisher)))

        then:
        subscriber != null
        results == []
        error == null
        !completed

        when:
        subscriber.onComplete()

        then:
        results == []
        error == null
        completed
    }
}
