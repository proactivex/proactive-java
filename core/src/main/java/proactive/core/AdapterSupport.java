
package proactive.core;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.concurrent.Flow;
import java.util.function.Consumer;

/**
 * Provides adaptors for bridging between Reactive Streams {@link Publisher} classes and Java 9 {@link Flow} ones.
 */
public final class AdapterSupport {
    private AdapterSupport() {
    }

    public static <T> Publisher<T> toPublisher(Flow.Publisher<T> publisher) {
        if (publisher == null) {
            throw new NullPointerException("publisher");
        } else if (publisher instanceof FlowPublisherFromPublisher) {
            return ((FlowPublisherFromPublisher<T>) publisher).publisher;
        } else {
            return new PublisherFromFlowPublisher<>(publisher);
        }
    }

    public static <T> Flow.Publisher<T> toFlowPublisher(Publisher<T> publisher) {
        if (publisher == null) {
            throw new NullPointerException("publisher");
        } else if (publisher instanceof PublisherFromFlowPublisher) {
            return ((PublisherFromFlowPublisher<T>) publisher).flowPublisher;
        } else {
            return new FlowPublisherFromPublisher<>(publisher);
        }
    }

    public static <T> Subscriber<T> toSubscriber(Flow.Subscriber<T> subscriber) {
        if (subscriber == null) {
            throw new NullPointerException("flowSubscriber");
        } else if (subscriber instanceof FlowSubscriberFromSubscriber) {
            return ((FlowSubscriberFromSubscriber<T>) subscriber).subscriber;
        } else {
            return new SubscriberFromFlowSubscriber<>(subscriber);
        }
    }

    public static <T> Flow.Subscriber<T> toFlowSubscriber(Subscriber<T> subscriber) {
        if (subscriber == null) {
            throw new NullPointerException("subscriber");
        } else if (subscriber instanceof SubscriberFromFlowSubscriber) {
            return ((SubscriberFromFlowSubscriber<T>)subscriber).flowSubscriber;
        } else {
            return new FlowSubscriberFromSubscriber<>(subscriber);
        }
    }

    /**
     * Adapts the specified Java 9 {@link Flow.Subscription} into a Reactive Streams {@link Subscription}.
     *
     * @param flowSubscription The {@link Flow.Subscription} to adapt.
     * @return The new {@link Subscription}.
     */
    public static Subscription toSubscription(Flow.Subscription flowSubscription) {
        if (flowSubscription == null) {
            throw new NullPointerException("flowSubscription");
        } else if (flowSubscription instanceof FlowSubscriptionFromSubscription) {
            return ((FlowSubscriptionFromSubscription) flowSubscription).subscription;
        } else {
            return new SubscriptionFromFlowSubscription(flowSubscription);
        }
    }

    /**
     * Adapts the specified Reactive Streams {@link Subscription} into a Java 9 {@link Flow.Subscription}.
     *
     * @param subscription The subscription to adapt.
     * @return The new {@link Flow.Subscription}.
     */
    public static Flow.Subscription toFlowSubscription(Subscription subscription) {
        if (subscription == null) {
            throw new NullPointerException("subscription");
        } else if (subscription instanceof SubscriptionFromFlowSubscription) {
            return ((SubscriptionFromFlowSubscription) subscription).flowSubscription;
        } else {
            return new FlowSubscriptionFromSubscription(subscription);
        }
    }

    /**
     * Adapts the specified Java 9 {@link Flow.Subscription} {@link Consumer} to a Reactive Streams {@link Subscription} {@link Consumer}.
     *
     * @param flowSubscriptionConsumer The {@link Flow.Subscription} {@link Consumer} to adapt.
     * @return The new {@link Subscription} {@link Consumer}.
     */
    public static Consumer<? super Subscription> toSubscriptionConsumer(Consumer<? super Flow.Subscription> flowSubscriptionConsumer) {
        if (flowSubscriptionConsumer == null) {
            return null;
        } else if (flowSubscriptionConsumer instanceof FlowSubscriptionConsumer) {
            return ((FlowSubscriptionConsumer) flowSubscriptionConsumer).subscriptionConsumer;
        } else {
            return new SubscriptionConsumer(flowSubscriptionConsumer);
        }
    }

    public static Consumer<? super Flow.Subscription> toFlowSubscriptionConsumer(Consumer<? super Subscription> subscriptionConsumer) {
        if (subscriptionConsumer == null) {
            return null;
        } else if (subscriptionConsumer instanceof SubscriptionConsumer) {
            return ((SubscriptionConsumer) subscriptionConsumer).flowSubscriptionConsumer;
        } else {
            return new FlowSubscriptionConsumer(subscriptionConsumer);
        }
    }

    /********************************************************
     * Utility Classes
     ********************************************************/

    static final class PublisherFromFlowPublisher<T> implements Publisher<T> {

        private final Flow.Publisher<T> flowPublisher;

        PublisherFromFlowPublisher(Flow.Publisher<T> flowPublisher) {
            this.flowPublisher = flowPublisher;
        }

        @Override
        public void subscribe(Subscriber<? super T> s) {
            flowPublisher.subscribe(toFlowSubscriber(s));
        }
    }

    static final class FlowPublisherFromPublisher<T> implements Flow.Publisher<T> {
        private final Publisher<T> publisher;

        FlowPublisherFromPublisher(Publisher<T> publisher) {
            this.publisher = publisher;
        }

        @Override
        public void subscribe(Flow.Subscriber<? super T> s) {
            publisher.subscribe(toSubscriber(s));
        }
    }

    /**
     * Wraps a {@link Flow.Subscriber} as a Reactive {@link Subscriber}.
     *
     * @param <T> The value type.
     */
    static final class SubscriberFromFlowSubscriber<T> implements Subscriber<T> {

        private final Flow.Subscriber<T> flowSubscriber;

        SubscriberFromFlowSubscriber(Flow.Subscriber<T> subscriber) {
            this.flowSubscriber = subscriber;
        }

        @Override
        public void onSubscribe(Subscription subscription) {
            flowSubscriber.onSubscribe(toFlowSubscription(subscription));
        }

        @Override
        public void onNext(T item) {
            flowSubscriber.onNext(item);
        }

        @Override
        public void onError(Throwable throwable) {
            flowSubscriber.onError(throwable);
        }

        @Override
        public void onComplete() {
            flowSubscriber.onComplete();
        }
    }

    static final class FlowSubscriberFromSubscriber<T> implements Flow.Subscriber<T> {

        private final Subscriber<T> subscriber;

        FlowSubscriberFromSubscriber(Subscriber<T> subscriber) {
            this.subscriber = subscriber;
        }

        @Override
        public void onSubscribe(Flow.Subscription subscription) {
            subscriber.onSubscribe(toSubscription(subscription));
        }

        @Override
        public void onNext(T item) {
            subscriber.onNext(item);
        }

        @Override
        public void onError(Throwable throwable) {
            subscriber.onError(throwable);
        }

        @Override
        public void onComplete() {
            subscriber.onComplete();
        }
    }

    /**
     * Adapts a {@link Flow.Subscription} to a Reactive {@link Subscription}.
     */
    static final class SubscriptionFromFlowSubscription implements Subscription {
        private final Flow.Subscription flowSubscription;

        SubscriptionFromFlowSubscription(Flow.Subscription flowSubscription) {
            this.flowSubscription = flowSubscription;
        }

        @Override
        public void request(long n) {
            flowSubscription.request(n);
        }

        @Override
        public void cancel() {
            flowSubscription.cancel();
        }
    }

    /**
     * Adapts a Reactive {@link Subscription} to a {@link Flow.Subscription}.
     */
    static final class FlowSubscriptionFromSubscription implements Flow.Subscription {

        private final Subscription subscription;

        FlowSubscriptionFromSubscription(Subscription subscription) {
            this.subscription = subscription;
        }

        @Override
        public void request(long n) {
            subscription.request(n);
        }

        @Override
        public void cancel() {
            subscription.cancel();
        }
    }

    static final class SubscriptionConsumer implements Consumer<Subscription> {

        private final Consumer<? super Flow.Subscription> flowSubscriptionConsumer;

        SubscriptionConsumer(Consumer<? super Flow.Subscription> flowSubscriptionConsumer) {
            this.flowSubscriptionConsumer = flowSubscriptionConsumer;
        }

        @Override
        public void accept(Subscription subscription) {
            flowSubscriptionConsumer.accept(toFlowSubscription(subscription));
        }
    }

    static final class FlowSubscriptionConsumer implements Consumer<Flow.Subscription> {

        private final Consumer<? super Subscription> subscriptionConsumer;

        FlowSubscriptionConsumer(Consumer<? super Subscription> subscriptionConsumer) {
            this.subscriptionConsumer = subscriptionConsumer;
        }

        @Override
        public void accept(Flow.Subscription subscription) {
            subscriptionConsumer.accept(toSubscription(subscription));
        }
    }
}
