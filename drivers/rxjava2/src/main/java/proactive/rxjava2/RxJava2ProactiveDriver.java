package proactive.rxjava2;

import io.reactivex.Flowable;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import proactive.core.AdapterSupport;
import proactive.core.Context;
import proactive.core.ProactiveDriver;

import java.util.concurrent.Flow;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class RxJava2ProactiveDriver implements ProactiveDriver {
    @Override
    public <T> Context<T> empty() {
        return toContext(Flowable.empty());
    }

    @Override
    public <T> Context<T> filter(Context<T> context, Predicate<? super T> predicate) {
        Flowable<T> pub = toFlowable(context);
        return toContext(pub.filter(predicate::test));
    }

    @Override
    public <T> Context<T> find(Context<T> context, Predicate<? super T> predicate) {
        Flowable<T> pub = toFlowable(context);
        return toContext(pub.filter(predicate::test).firstElement().toFlowable());
    }

    private <T> Flowable<T> toFlowable(Context<T> context) {
        return Flowable.fromPublisher(toPublisher(context));
    }

    @Override
    public <T> Context<T> first(Context<T> context) {
        return toContext(toFlowable(context).firstElement().toFlowable());
    }

    @Override
    public <I, T> Context<T> flatMap(Context<I> context, Function<? super I, ? extends Context<T>> mapper) {
        return toContext(toFlowable(context).flatMap(value -> toPublisher(mapper.apply(value))));
    }

    @Override
    public <T> Context<T> just(T value) {
        return toContext(Flowable.just(value));
    }

    @SafeVarargs
    @Override
    public final <T> Context<T> just(T... values) {
        return toContext(Flowable.fromArray(values));
    }

    @Override
    public <T> Context<T> log(Context<T> context, String category) {
        // TODO: figure out how to log.
        return context;
    }

    @Override
    public <T> void subscribe(Context<T> context, Subscriber<? super T> subscriber) {
        toFlowable(context).subscribe(subscriber);
    }

    @Override
    public <T> void subscribe(Context<T> context) {
        toFlowable(context).subscribe();
    }

    @Override
    public <T> void subscribe(Context<T> context, Consumer<? super T> nextConsumer, Consumer<? super Throwable> errorConsumer, Runnable completeConsumer, Runnable subscribeConsumer) {
        io.reactivex.functions.Consumer<? super Subscription> rxSubscribeConsumer = subscription -> {
            subscribeConsumer.run();
            subscription.request(Long.MAX_VALUE);
        };

        //noinspection ResultOfMethodCallIgnored
        toFlowable(context).subscribe(
                nextConsumer::accept,
                errorConsumer::accept,
                completeConsumer::run,
                rxSubscribeConsumer
        );
    }

    @Override
    public <T> Context<T> toContext(Publisher<T> publisher) {
        return new RxJavaContext<>(publisher);
    }

    @Override
    public <T> Context<T> toContext(Flow.Publisher<T> flowPublisher) {
        return new RxJavaContext<>(AdapterSupport.toPublisher(flowPublisher));
    }

    @Override
    public <T> Flow.Publisher<T> toFlowPublisher(Context<T> context) {
        return AdapterSupport.toFlowPublisher(toPublisher(context));
    }

    @Override
    public <T> Publisher<T> toPublisher(Context<T> context) {
        if (context instanceof RxJavaContext) {
            return ((RxJavaContext<T>) context).publisher;
        } else {
            throw new IllegalArgumentException("context");
        }
    }

    /**
     * Implementation of {@link Context} for the Reactor API.
     *
     * @param <T> The type of value being handled.
     */
    private static class RxJavaContext<T> implements Context<T> {
        private final Publisher<T> publisher;

        private RxJavaContext(Publisher<T> publisher) {
            this.publisher = publisher;
        }
    }

}
