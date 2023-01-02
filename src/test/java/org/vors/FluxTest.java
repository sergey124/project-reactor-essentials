package org.vors;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;

@Slf4j
public class FluxTest {
    @Test
    public void fluxStrings() {
        Flux<String> flux = Flux.just("Any", "Beny", "Many", "Moe")
                .log();

        StepVerifier.create(flux)
                .expectNext("Any", "Beny", "Many", "Moe")
                .verifyComplete();
    }

    @Test
    public void fluxIntegerRange() {
        // also Flux.fromIterable(List.of(1, 2, 3, 4));
        Flux<Integer> flux = Flux.range(1, 5)
                .log();

        flux.subscribe(i -> log.info("Value {}", i));

        StepVerifier.create(flux)
                .expectNext(1, 2, 3, 4, 5)
                .verifyComplete();
    }

    @Test
    public void fluxFromNumbersError() {
        Flux<Integer> flux = Flux.range(1, 5)
                .log()
                .map(i -> {
                    if (i == 3) {
                        throw new IllegalStateException("illegal state msg");
                    }
                    return i;
                });

        flux.subscribe(
                i -> log.info("Value {}", i),
                Throwable::printStackTrace,
                () -> log.info("DONE!"));

        StepVerifier.create(flux)
                .expectNext(1, 2)
                .expectError(IllegalStateException.class)
                .verify();
    }

    @Test
    public void fluxFromNumbersInlineBackpressure() {
        Flux<Integer> flux = Flux.range(1, 10)
                .log();

        flux.subscribe(new Subscriber<>() {
            private int count = 0;
            private Subscription subscription;
            private int requestCount = 2;

            @Override
            public void onSubscribe(Subscription subscription) {
                this.subscription = subscription;
                subscription.request(requestCount);
            }

            @Override
            public void onNext(Integer integer) {
                count++;
                if (count >= requestCount) {
                    count = 0;
                    subscription.request(requestCount);
                }
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onComplete() {

            }
        });

        StepVerifier.create(flux)
                .expectNext(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                .verifyComplete();
    }


    @Test
    public void fluxFromNumbersNotSoUglyBackpressure() {
        Flux<Integer> flux = Flux.range(1, 10)
                .log();

        flux.subscribe(new BaseSubscriber<>() {
            private int count = 0;
            private int requestCount = 2;

            @Override
            protected void hookOnNext(Integer value) {
                count++;
                if (count >= requestCount) {
                    count = 0;
                    request(requestCount);
                }
            }
        });

        StepVerifier.create(flux)
                .expectNext(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                .verifyComplete();
    }

    @Test
    public void fluxSubscriberBackpressureLimitRate() {
        Flux<Integer> flux = Flux.range(1, 10)
                .log()
                .limitRate(3);

        flux.subscribe(i -> log.info("number {}", i));

        StepVerifier.create(flux)
                .expectNext(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                .verifyComplete();
    }

    @Test
    public void fluxSubscriberIntervalOne() throws InterruptedException {
        Flux<Long> interval = Flux.interval(Duration.ofMillis(100))
                .take(10)
                .log();

        interval.subscribe(i -> log.info("Number {}", i));

        Thread.sleep(3000);
    }

    @Test
    public void fluxSubscriberIntervalTwo() throws InterruptedException {
        StepVerifier.withVirtualTime(this::createInterval)
                .expectSubscription()
                .expectNoEvent(Duration.ofHours(24))
                .thenAwait(Duration.ofDays(1))
                .expectNext(0L)
                .thenAwait(Duration.ofDays(1))
                .expectNext(1L)
                .thenCancel()
                .verify();

        Thread.sleep(3000);
    }

    private Flux<Long> createInterval() {
        return Flux.interval(Duration.ofDays(1))
                .log();
    }

    /**
     * Hot observable. Executes right after connecting, even if there's no subscribers.
     */
    @Test
    public void connectableFlux() {
        ConnectableFlux<Integer> connectableFlux = Flux.range(1, 10)
                .log()
                .delayElements(Duration.ofMillis(200))
                .publish();

//        connectableFlux.connect();
//
//        log.info("Thread sleeping for 300 ms");
//        Thread.sleep(300);
//
//        connectableFlux.subscribe(i -> log.info("Sub1 number {}", i));
//
//        log.info("Thread sleeping for 200 ms");
//        Thread.sleep(200);
//
//        connectableFlux.subscribe(i -> log.info("Sub2 number {}", i));

        StepVerifier.create(connectableFlux)
                .then(connectableFlux::connect)
                .thenConsumeWhile(i -> i <= 5)
                .expectNext(6,7,8,9,10)
                .expectComplete()
                .verify();
    }

    /**
     * Observable waiting for N subscribers and then publishing.
     */
    @Test
    public void connectableFluxAndConnect() {
        Flux<Integer> fluxAutoConnect = Flux.range(1, 5)
                .log()
                .delayElements(Duration.ofMillis(200))
                .publish()
                .autoConnect(2);

        StepVerifier.create(fluxAutoConnect)
                .then(fluxAutoConnect::subscribe)
                .expectNext(1,2,3,4,5)
                .expectComplete()
                .verify();
    }


}
