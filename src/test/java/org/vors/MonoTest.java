package org.vors;


import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;


@Slf4j
public class MonoTest {
    @Test
    public void monoSubscriber() {
        String name = "Albert Einstein";
        Mono<String> mono = Mono.just(name)
                .log();

        mono.subscribe();
        log.info("----------------");
        StepVerifier.create(mono)
                .expectNext(name)
                .verifyComplete();
    }

    @Test
    public void monoSubscribeWithConsumer() {
        String name = "Albert Einstein";
        Mono<String> mono = Mono.just(name)
                .log();

        mono.subscribe(s -> log.info("Value {}", s));

        StepVerifier.create(mono)
                .expectNext(name)
                .verifyComplete();
    }

    @Test
    public void monoSubscriberHandleError() {
        String name = "Albert Einstein";
        Mono<String> mono = Mono.just(name)
                .map(i -> {
                    throw new RuntimeException("Something happened");
                });

        mono.subscribe(s -> log.info("Value {}", s), s -> log.error("Something bad happened!"));
        mono.subscribe(s -> log.info("Value {}", s), Throwable::printStackTrace);

        StepVerifier.create(mono)
                .expectError(RuntimeException.class)
                .verify();
    }


    @Test
    public void monoSubscribeConsumerComplete() {
        String name = "Albert Einstein";
        Mono<String> mono = Mono.just(name)
                .log()
                .map(String::toUpperCase);

        mono.subscribe(
                s -> log.info("Value {}", s),
                Throwable::printStackTrace,
                () -> log.info("COMPLETED"));

        StepVerifier.create(mono)
                .expectNext(name.toUpperCase())
                .verifyComplete();
    }

    @Test
    public void monoSubscribeConsumerSubscription() {
        String name = "Albert Einstein";
        Mono<String> mono = Mono.just(name)
                .log()
                .map(String::toUpperCase);

        mono.subscribe(
                s -> log.info("Value {}", s),
                Throwable::printStackTrace,
                () -> log.info("COMPLETED"),
                Subscription::cancel);

        StepVerifier.create(mono)
                .expectNext(name.toUpperCase())
                .verifyComplete();
    }

    @Test
    public void monoSubscribeConsumerSubscriptionRequestN() {
        String name = "Albert Einstein";
        Mono<String> mono = Mono.just(name)
                .log()
                .map(String::toUpperCase);

        mono.subscribe(
                s -> log.info("Value {}", s),
                Throwable::printStackTrace,
                () -> log.info("COMPLETED"),
                subscription -> subscription.request(5));

        StepVerifier.create(mono)
                .expectNext(name.toUpperCase())
                .verifyComplete();
    }

    @Test
    public void monoDoOnMethods() {
        String name = "Albert Einstein";
        Mono<String> mono = Mono.just(name)
                .log()
                .map(String::toUpperCase)
                .doOnSubscribe(s -> log.info("Subscribed {}", s))
                .doOnRequest(longNumber -> log.info("Requested {}", longNumber))
                .doOnNext(s -> log.info("Executing next {}", s))
                .doOnSuccess(s -> log.info("Success {}", s));

        mono.subscribe(
                s -> log.info("Value {}", s),
                Throwable::printStackTrace,
                () -> log.info("COMPLETED"));
    }

    @Test
    public void monoDoOnMethodsEmptyNoNext() {
        String name = "Albert Einstein";
        Mono<Object> mono = Mono.just(name)
                .log()
                .map(String::toUpperCase)
                .flatMap(s -> Mono.empty())
                .doOnSubscribe(s -> log.info("Subscribed {}", s))
                .doOnRequest(longNumber -> log.info("Requested {}", longNumber))
                .doOnNext(s -> log.info("Executing next {}", s))
                .doOnSuccess(s -> log.info("Success {}", s));

        mono.subscribe(
                s -> log.info("Value {}", s),
                Throwable::printStackTrace,
                () -> log.info("COMPLETED"));
    }

    @Test
    public void monoError() {
        Mono<String> mono = Mono.error(new IllegalArgumentException("some illegal arg"));

        StepVerifier.create(mono)
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    public void monoErrorResume() {
        String name = "Richard Feynman";
        Mono<String> mono = Mono.error(new IllegalArgumentException("some illegal arg"))
                .onErrorResume(t -> {
                    log.error("Resuming from error {}", t.getMessage());
                    return Mono.just(name);
                })
                .map(String::valueOf);

        StepVerifier.create(mono)
                .expectNext(name)
                .verifyComplete();
    }

    @Test
    public void monoErrorReturn() {
        final String NAME = "Richard Feynman";
        final String EMPTY = "EMPTY";
        Mono<String> mono = Mono.error(new IllegalArgumentException("some illegal arg"))
                .onErrorReturn(EMPTY)
                .onErrorResume(t -> {
                    log.error("Resuming from error {}", t.getMessage());
                    return Mono.just(NAME);
                })
                .map(String::valueOf);

        StepVerifier.create(mono)
                .expectNext(EMPTY)
                .verifyComplete();
    }
}
