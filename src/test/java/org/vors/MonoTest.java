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
}
