import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

public class ObservableExamples {

    private static final Consumer<Long> ON_NEXT = v -> System.out.println("Received: " + v);
    private static final Consumer<Throwable> ON_ERROR = e -> System.out.println("Error: " + e);
    private static final Action ON_COMPLETE = () -> System.out.println("Completed");

    private static class DebugObserver<T> implements Observer<T> {
        @Override
        public void onSubscribe(Disposable d) {
        }

        @Override
        public void onNext(T t) {
            System.out.println("Received: " + t);
        }

        @Override
        public void onError(Throwable e) {
            System.err.println("Error: " + e);
        }

        @Override
        public void onComplete() {
            System.out.println("Completed");
        }
    }

    public static void main(String[] args) throws Exception {
        ObservableExamples examples = new ObservableExamples();
        System.out.println("* Basic types of Observable *");
        System.out.println("--- Observable.just() ---");
        examples.justObservable();
        System.out.println("--- Observable.empty() ---");
        examples.emptyObservable();
        System.out.println("--- Observable.never() ---");
        examples.neverObservable();
        System.out.println("--- Observable.error() ---");
        examples.errorObservable();
        System.out.println();
        System.out.println("* Timing aspects* ");
        System.out.println("--- Observable without defer() ---");
        examples.immediateObservable();
        System.out.println("--- Observable with defer() ---");
        examples.deferObservable();
        System.out.println();
        System.out.println("* Custom Observable *");
        System.out.println("--- Observable.create() ---");
        examples.createObservable();
        System.out.println();
        System.out.println("* Functional unfolds *");
        System.out.println("--- Observable.range() ---");
        examples.rangeObservable();
        System.out.println("--- Observable.interval() ---");
        examples.intervalObservable();
        System.out.println("--- Observable.timer() ---");
        examples.timerObservable();
        System.out.println();
        System.out.println("* Transitioning into Observable *");
        System.out.println("--- Observable.fromFuture() ---");
        examples.futureObservable();
        System.out.println("--- Observable.fromFuture() with timeout ---");
        examples.futureTimeoutObservable();
        System.out.println("--- Observable.fromArray() ---");
        examples.observableFromArray();
    }

    /**
     * Emits predefined sequence of values, then terminates with onComplete().
     */
    void justObservable() {
        Observable<String> values = Observable.just("one", "two", "three");
        values.subscribe(new DebugObserver<>());
    }

    /**
     * Emits nothing, terminates with onComplete() immediately.
     */
    void emptyObservable() {
        Observable<Integer> empty = Observable.empty();
        empty.subscribe(new DebugObserver<>());
    }

    /**
     * Emits literally nothing, terminates immediately, but does not cause even "onComplete()".
     */
    void neverObservable() {
        Observable<Integer> never = Observable.never();
        never.subscribe(new DebugObserver<>());
    }

    /**
     * Emits single error event and terminate.
     */
    void errorObservable() {
        Observable<Integer> error = Observable.error(new Exception("Something went wrong"));
        error.subscribe(new DebugObserver<>());
    }

    /**
     * Example to compare with {@link #deferObservable()}: without .defer() any new subscriptions will get event
     * emitted on Observable.just() execution. It results into same time printed in example below.
     *
     * @throws Exception from Thread.sleep() potential interruption, which is not intended in this example.
     */
    void immediateObservable() throws Exception {
        Observable<Long> now = Observable.just(System.currentTimeMillis());
        now.subscribe(System.out::println);
        Thread.sleep(1000);
        now.subscribe(System.out::println);
    }

    /**
     * In example below Observable.just() will be executed on every new subscription. It results into different time
     * printed on different subscriptions, which is different from {@link #immediateObservable()}
     *
     * @throws Exception from Thread.sleep() potential interruption, which is not intended in this example.
     */
    void deferObservable() throws Exception {
        Observable<Long> defer = Observable.defer(() ->
                Observable.just(System.currentTimeMillis()));
        defer.subscribe(System.out::println);
        Thread.sleep(1000);
        defer.subscribe(System.out::println);
    }


    /**
     * Observable.create() is powerful in terms of customizing predefined set of Observables (like examples above).
     * Example below is basically the same as Observable.just("Hello").
     * On the other hand, .create() is not what intended to be used to "enter reactive world" according to
     * <a href="https://github.com/ReactiveX/RxJava/wiki/What's-different-in-2.0#entering-the-reactive-world">analysis</a>
     * <p>
     * during migration to RxJava2, but it was fixed somehow so that it's usage is supposed to be "safe" in RxJava2
     * (whateve "safe" means)
     */
    void createObservable() {
        Observable<String> values = Observable.create(o -> {
            o.onNext("Hello");
            o.onComplete();
        });
        values.subscribe(new DebugObserver<>());
    }

    /**
     * Emits values from 10 to 13 inclusively
     */
    void rangeObservable() {
        Observable<Integer> range = Observable.range(10, 4);
        range.subscribe(new DebugObserver<>());
    }

    /**
     * Emits long values starting from 0 indefinitely with specified interval (1 second in example below).
     * Emitting values happens on different thread rather than the one we subscribe on.
     * It allows to use blocking Thread.sleep() or System.in.read() to see the output in console (in example below).
     * We limit such listening to 5 seconds by unsubscribing (.dispose()) to avoid indefinite running of example.
     *
     * @throws Exception
     */
    void intervalObservable() throws Exception {
        System.out.println("Waiting...");
        Observable<Long> values = Observable.interval(1, TimeUnit.SECONDS);
        Disposable disposable = values.subscribe(
                ON_NEXT,
                ON_ERROR,
                ON_COMPLETE
        );

        Thread.sleep(4500);
        disposable.dispose();
    }

    /**
     * Waits for specified delay, then emits single value. In example below delay is 1 second.
     * Note that we call Thread.sleep() to actually wait for emitted value as it is generated in different thread.
     */
    void timerObservable() throws Exception {
        System.out.println("Waiting...");
        Observable<Long> timer = Observable.timer(2, TimeUnit.SECONDS);
        timer.subscribe(new DebugObserver<>());
        Thread.sleep(2500);
    }

    /**
     * Java's Future can be converted into ObservableSource that emits the return value of the Future.get method of
     * that object, by passing the object into the from method.
     */
    void futureObservable() {
        FutureTask<Integer> f = new FutureTask<>(() -> {
            Thread.sleep(2000);
            return 21;
        });

        new Thread(f).start();
        System.out.println("Waiting...");
        Observable<Integer> futureObservable = Observable.fromFuture(f);
        futureObservable.subscribe(new DebugObserver<>());
    }

    /**
     * When future is not executed until given timeout - Exception is thrown and observer end up with onError()
     */
    void futureTimeoutObservable() {
        FutureTask<Integer> f = new FutureTask<>(() -> {
            Thread.sleep(1500);
            return 42;
        });
        new Thread(f).start();
        System.out.println("Now waiting no longer than 1 sec...");
        Observable<Integer> timeoutObservable = Observable.fromFuture(f, 1, TimeUnit.SECONDS);
        timeoutObservable.subscribe(new DebugObserver<>());
    }

    void observableFromArray() {
        Integer[] a = new Integer[]{1, 2, 3};
        Observable<Integer> values = Observable.fromArray(a);
        values.subscribe(new DebugObserver<>());
    }

}
