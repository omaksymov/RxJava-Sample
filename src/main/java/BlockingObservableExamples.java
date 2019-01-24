import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class BlockingObservableExamples {
    public static void main(String[] args) throws Exception {
        BlockingObservableExamples examples = new BlockingObservableExamples();
        System.out.println("--- Non-blocking forEach ---");
        examples.nonBlockingForEach();
        System.out.println("--- Blocking forEach ---");
        examples.blockingForEach();
        System.out.println("--- blockingFirst() ---");
        examples.blockingFirst();
        System.out.println("--- blockingSingle() ---");
        examples.blockingSingle();
        System.out.println("--- Iterable ---");
        examples.basicIterable();
        System.out.println("--- Iterable next ---");
        examples.nextIterable();
        System.out.println("--- Iterable next ---");
        examples.nextIterable();
        System.out.println("--- Iterable latest ---");
        examples.latestIterable();
        System.out.println("--- Iterable mostRecent ---");
        examples.mostRecentIterable();
        System.out.println("--- blocking Future ---");
        examples.blockingAsFuture();

    }

    // Note: forEach is alias for subscribe(), but does not return Disposable
    private void nonBlockingForEach() throws Exception {
        Observable<Long> values = Observable.interval(100, TimeUnit.MILLISECONDS);
        values.take(5)
                .forEach(v -> System.out.println(v));
        System.out.println("Subscribed"); // this is printed first, then - emitted observables
        Thread.sleep(1000);
    }

    /*
        The difference to nonBlockingForEach is that values are printed first (as call to blockingForEach blocked until
        the observable completed)
    */
    private void blockingForEach() {
        Observable<Long> values = Observable.interval(100, TimeUnit.MILLISECONDS);
        values.take(5)
                .blockingForEach(v -> System.out.println(v));
        System.out.println("Subscribed"); // this is printed last - after blocking emitted observables
    }

    /*
        Blocks until the first value is available (but doesn't return Observable that will emit the value
        like non-blocking version does).
        Similar behavior is for blockingLast().
     */
    private void blockingFirst() {
        Observable<Long> values = Observable.interval(500, TimeUnit.MILLISECONDS);

        long value = values
                .take(5).blockingFirst();

        System.out.println(value);
    }

    /*
        Blocks until the single value is available. If there are more values than 1 - throws an exception
        (like in example below)
    */
    private void blockingSingle() {
        Observable<Long> values = Observable.interval(500, TimeUnit.MILLISECONDS);

        try {
            long value = values
                    .take(5).blockingSingle();

            System.out.println(value);
        } catch (IllegalArgumentException e) {
            System.out.println("Caught:" + e);
        }
    }

    // Iterables are pull-based, unlike Rx, which is push-based.
    private void basicIterable() {
        Observable<Long> values = Observable.interval(500, TimeUnit.MILLISECONDS);
        Iterable<Long> iterable = values
                .take(5).blockingIterable();
        for (long l : iterable) {
            System.out.println(l);
        }
    }

    /*
        In this case consumer (printing iterables) is slower than emitter (printed as "Emitted:"),
        so next() misses some emitted values when pulling the data.
        Note: If the consumer is faster than the producer, the iterator will block and wait for the next value.
     */
    private void nextIterable() throws Exception {
        Observable<Long> values = Observable.interval(500, TimeUnit.MILLISECONDS);
        Disposable d = values.subscribe((v) -> System.out.println("Emitted: " + v));
        Iterable<Long> iterable = values
                .take(5).blockingNext();
        for (long l : iterable) {
            System.out.println(l);
            Thread.sleep(750);
        }
        d.dispose();
    }

    /*
        Similar to nextIterable() example, consumer (printing iterables) is slower than emitter (printed as "Emitted:"),
        so blockingLatest() misses some emitted values when pulling the data.
        The blockingLatest() method is similar to blockingNext(), with the difference that it will cache one value.
        The iterator only blocks if no events have been emitted by the observable since the last value was consumed.
        As long as there has been a new event, the iterator will return immediately with a value,
        or with the termination of the iteration.
     */
    private void latestIterable() throws Exception {
        Observable<Long> values = Observable.interval(500, TimeUnit.MILLISECONDS);
        Disposable d = values.subscribe((v) -> System.out.println("Emitted: " + v));
        Iterable<Long> iterable = values
                .take(5).blockingLatest();
        for (long l : iterable) {
            System.out.println(l);
            Thread.sleep(750);
        }
        d.dispose();
        // note in the output that latest emitted value 4 is not printed - it's because when iterable fetches the data -
        // it sees onComplete() already, which means termination of sequence, so doesn't fetch anything.
    }

    /*
        The 'mostRecent' iterator never blocks. It caches a single value, therefore values may be skipped if the consumer
        is slow. Unlike 'latest', the last cached value is always returned, resulting in repetitions if the consumer is
        faster than the producer. To allow the 'mostRecent' iterator to be completely non-blocking, an initial value is
        needed. That value is returned if the observable has not emitted any values yet.
     */
    private void mostRecentIterable() throws Exception {
        Observable<Long> values = Observable.interval(500, TimeUnit.MILLISECONDS);
        Disposable d = values.subscribe((v) -> System.out.println("Emitted: " + v));
        Iterable<Long> iterable = values
                .take(5).blockingMostRecent(0L);
        for (long l : iterable) {
            System.out.println(l);
            Thread.sleep(400);
        }
        d.dispose();
    }

    private void blockingAsFuture() throws Exception {
        Observable<Long> values = Observable.timer(500, TimeUnit.MILLISECONDS);
        Disposable d = values.subscribe((v) -> System.out.println("Emitted: " + v));
        Future<Long> future = values.take(5).toFuture();
        System.out.println(future.get());
        d.dispose();
    }
}
