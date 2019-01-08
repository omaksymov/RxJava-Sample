import io.reactivex.Notification;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Timed;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TransformationExamples {
    private static class PrintObserver<T> implements Observer<T> {
        private final String name;

        public PrintObserver(String name) {
            this.name = name;
        }

        @Override
        public void onSubscribe(Disposable d) {
        }

        @Override
        public void onNext(T v) {
            System.out.println(name + ": " + v);
        }

        @Override
        public void onError(Throwable e) {
            System.out.println(name + ": Error: " + e);
        }

        @Override
        public void onComplete() {
            System.out.println(name + ": Completed");
        }
    }

    public static void main(String[] args) throws Exception {
        TransformationExamples examples = new TransformationExamples();
        System.out.println("--- map(): increment ---");
        examples.incrementMap();
        System.out.println("--- map(): parseInt ---");
        examples.parseIntMap();
        System.out.println("--- cast() ---");
        examples.cast();
        System.out.println("--- timestamp() ---");
        examples.timestamp();
        System.out.println("--- materialize() ---");
        examples.materialize();
        System.out.println("--- flatMap() single---");
        examples.flatMapSingle();
        System.out.println("--- flatMap() collection---");
        examples.flatMapCollection();
        System.out.println("--- flatMap() timing---");
        examples.flatMapTiming();
        System.out.println("--- concatMap() ---");
        examples.concatMap();
        System.out.println("--- flatMapIterable() ---");
        examples.flatMapIterable();
        System.out.println("--- flatMapIterable() with result mapper ---");
        examples.flatMapIterableWithResultMapper();

    }

    private void incrementMap() {
        Observable<Integer> values = Observable.range(1, 3);
        values.subscribe(new PrintObserver<>("Original"));
        values.map(new Function<Integer, Integer>() {
            @Override
            public Integer apply(Integer integer) throws Exception {
                return integer + 3;
            }
        });
        values.map(i -> i + 3).subscribe(new PrintObserver<>("Map (+3)"));
    }

    private void parseIntMap() {
        Observable<String> values = Observable.fromArray("1", "2", "3", "4");
        values.subscribe(new PrintObserver<>("Original"));
        values.map(Integer::parseInt).map(i -> i + 2).subscribe(new PrintObserver<>("Map (ParseInt, +2)"));
    }

    // Error when cast fails (i.e. String -> Integer)
    private void cast() {
        Observable<Object> values = Observable.just(1, 2, "3");
        values.subscribe(new PrintObserver<>("Original"));
        values.cast(Integer.class).subscribe(new PrintObserver<>("Cast"));
    }

    // Maps emitted values to Timed<T> values containing timestamp of initially emitted value
    private void timestamp() throws Exception {
        Observable<Long> values = Observable.interval(100, TimeUnit.MILLISECONDS);
        Disposable disposable = values.take(3)
                .timestamp()
                .subscribe(new TimedConsumer<>());
        Thread.sleep(500);
        disposable.dispose();
    }

    private static class TimedConsumer<T> implements Consumer<Timed<T>> {

        @Override
        public void accept(Timed<T> tTimed) {
            System.out.println("Timestamped: " + tTimed.toString());
        }
    }

    // Transforms sequence into its metadata representation (i.e. onNext(value) -> OnNextNotification[i],
    // onComplete() -> onCompleteNotification)
    // dematerialize() performs reverse transformation
    private void materialize() throws Exception {
        Observable<Long> values = Observable.interval(100, TimeUnit.MILLISECONDS);
        values.take(3).subscribe(new PrintObserver<>("Original"));
        Disposable disposable = values.take(3)
                .materialize()
                .subscribe(new MaterializedConsumer<>());
        Thread.sleep(500);
        disposable.dispose();
    }

    private static class MaterializedConsumer<T> implements Consumer<Notification<T>> {

        @Override
        public void accept(Notification<T> tNotification) throws Exception {
            System.out.println("Materialize: " + tNotification);
        }
    }

    private void flatMapSingle() {
        Observable<Integer> values = Observable.just(3);
        values.subscribe(new PrintObserver<>("Original"));
        values.flatMap(i -> Observable.range(0, i))
                .subscribe(new PrintObserver<>("flatMapSingle"));
    }

    // 1) for every emitted value it will transform to corresponding character (can be achieved with map only, but...)
    // 2) it also allows to emit Observable.empty() for those emitted values which we'd like to ignore
    private void flatMapCollection() {
        Observable<Integer> values = Observable.range(0, 30);
        values.subscribe(new PrintObserver<>("Original"));
        values.flatMap(i -> {
            if (i > 0 && i <= 26) {
                return Observable.just(Character.valueOf((char) (i + 64)));
            } else {
                return Observable.empty();
            }
        })
                .subscribe(new PrintObserver<>("flatMapCollection"));
    }

    // Showcases the fact that observables returned by flatMap emit values as soon as they are available (not always
    // sequentially, like emit for the first incoming observable, then for the second and so on)
    private void flatMapTiming() throws Exception {
        Disposable disposable = Observable.just(100, 150)
                .flatMap(i ->
                        Observable.interval(i, TimeUnit.MILLISECONDS)
                                .map(v -> i)
                )
                .take(10)
                .subscribe(new FlatMapTimingConsumer<>());
        Thread.sleep(1000);
        disposable.dispose();

    }

    private static class FlatMapTimingConsumer<T> implements Consumer<T> {

        @Override
        public void accept(T t) throws Exception {
            System.out.println("FlatMapTiming: " + t);
        }
    }

    private void concatMap() throws Exception {
        Disposable disposable = Observable.just(100, 150)
                .concatMap(i ->
                        Observable.interval(i, TimeUnit.MILLISECONDS)
                                .map(v -> i).take(3)
                )
                .subscribe(new FlatMapTimingConsumer<>());
        Thread.sleep(1000);
        disposable.dispose();
    }

    // Converts iterable into single observable sequence
    private void flatMapIterable() {
        Observable<Integer> values = Observable.range(1, 3);
        values.subscribe(new PrintObserver<>("Original"));
        values.flatMapIterable(i -> range(1, i))
                .subscribe(new PrintObserver<>("flatMapIterable"));
    }

    private Iterable<Integer> range(int start, int count) {
        int end = start + count;
        List<Integer> res = new ArrayList<>();
        for (int i = start; i < end; i++) {
            res.add(i);
        }
        return res;
    }

    // Converts iterable into single observable sequence and multiplies every range element to the original value
    // which seeded this range (i.e. 1 -> {1 * 1}, 2-> {2 * 1, 2 * 2}, 3 -> {3 * 1, 3 * 2, 3 * 3})
    private void flatMapIterableWithResultMapper() {
        Observable<Integer> values = Observable.range(1, 3);
        values.subscribe(new PrintObserver<>("Original"));
        values.flatMapIterable(i -> range(1, i), (original, iterableItem) -> original * iterableItem)
                .subscribe(new PrintObserver<>("flatMapIterable (orig * rangeItem)"));
    }
}
