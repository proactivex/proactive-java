package proactive.reactor3;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import proactive.core.Context;
import proactive.core.AdapterSupport;
import proactive.core.ProactiveDriver;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.Flow;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Implementation of the {@link ProactiveDriver} for <a href="https://projectreactor.io/">Reactor 3</a>.
 */
public class Reactor3ProactiveDriver implements ProactiveDriver {

    @Override
    public <T> Context<T> empty() {
        return toContext(Mono.empty());
    }

    @Override
    public <T> Context<T> filter(Context<T> context, Predicate<? super T> predicate) {
        Publisher<T> publisher = toPublisher(context);
        if (publisher instanceof Mono) {
            return toContext(Mono.from(publisher).filter(predicate));
        } else {
            return toContext(Flux.from(publisher).filter(predicate));
        }
    }

    @Override
    public <I, T> Context<T> flatMap(Context<I> context, Function<? super I, ? extends Context<T>> mapper) {
        Publisher<I> publisher = toPublisher(context);
        if (publisher instanceof Mono) {
            return toContext(Mono.from(publisher).flatMapMany(value -> toPublisher(mapper.apply(value))));
        } else {
            return toContext(Flux.from(publisher).flatMap(value -> toPublisher(mapper.apply(value))));
        }
    }

    @Override
    @SafeVarargs
    public final <T> Context<T> just(T... values) {
        return toContext(Flux.just(values));
    }

    @Override
    public <T> Context<T> just(T value) {
        if (value == null) {
            return toContext(Mono.empty());
        } else {
            return toContext(Mono.just(value));
        }
    }

    @Override
    public <T> Context<T> find(Context<T> context, Predicate<? super T> predicate) {
        Publisher<T> publisher = toPublisher(context);
        if (publisher instanceof Mono) {
            return toContext(Mono.from(publisher).filter(predicate));
        } else {
            return toContext(Flux.from(publisher).filter(predicate).next());
        }
    }

    @Override
    public <T> Context<T> first(Context<T> context) {
        return toContext(Mono.from(toPublisher(context)));
    }

    @Override
    public <T> Context<T> log(Context<T> context, String category) {
        Publisher<T> publisher = toPublisher(context);
        if (publisher instanceof Mono) {
            return toContext(Mono.from(publisher).log(category));
        } else {
            return toContext(Flux.from(publisher).log(category));
        }
    }

    @Override
    public <T> void subscribe(Context<T> context, Subscriber<? super T> subscriber) {
        toPublisher(context).subscribe(subscriber);
    }

    @Override
    public <T> void subscribe(Context<T> context) {
        Publisher<T> publisher = toPublisher(context);
        if (publisher instanceof Mono) {
            Mono.from(publisher).subscribe();
        } else {
            Flux.from(publisher).subscribe();
        }
    }

    @Override
    public <T> void subscribe(Context<T> context, Consumer<? super T> nextConsumer, Consumer<? super Throwable> errorConsumer, Runnable completeConsumer, Runnable subscribeConsumer) {
        Publisher<T> publisher = toPublisher(context);
        Consumer<? super Subscription> subConsumer = null;

        if (subscribeConsumer != null) {
            subConsumer = s -> {
                subscribeConsumer.run();
                s.request(Long.MAX_VALUE);
            };
        }

        if (publisher instanceof Mono) {
            Mono.from(publisher).subscribe(nextConsumer, errorConsumer, completeConsumer, subConsumer);
        } else {
            Flux.from(publisher).subscribe(nextConsumer, errorConsumer, completeConsumer, subConsumer);
        }
    }

    @Override
    public <T> Publisher<T> toPublisher(Context<T> context) {
        if (context instanceof ReactorContext) {
            return ((ReactorContext<T>) context).publisher;
        } else {
            throw new IllegalArgumentException("Unsupported Context instance.");
        }
    }

    @Override
    public <T> Context<T> toContext(Publisher<T> publisher) {
        return new ReactorContext<>(publisher);
    }

    @Override
    public <T> Context<T> toContext(Flow.Publisher<T> flowPublisher) {
        return new ReactorContext<>(AdapterSupport.toPublisher(flowPublisher));
    }

    @Override
    public <T> Flow.Publisher<T> toFlowPublisher(Context<T> context) {
        if (context instanceof ReactorContext) {
            return AdapterSupport.toFlowPublisher(((ReactorContext<T>) context).publisher);
        } else {
            throw new IllegalArgumentException("Unsupported Context instance.");
        }
    }

    /**
     * Implementation of {@link Context} for the Reactor API.
     *
     * @param <T> The type of value being handled.
     */
    private static class ReactorContext<T> implements Context<T> {
        private final Publisher<T> publisher;

        private ReactorContext(Publisher<T> publisher) {
            this.publisher = publisher;
        }
    }
}
