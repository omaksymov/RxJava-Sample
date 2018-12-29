import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.*;

import java.util.concurrent.TimeUnit;

public class SubjectExamples {
    public static void main(String[] args) throws Exception {
        SubjectExamples examples = new SubjectExamples();
        System.out.println("--- PublishSubject ---");
        examples.publishSubject();
        System.out.println("--- ReplaySubject ---");
        System.out.println("- .createBySize() -");
        examples.replaySubjectLimitedBySize();
        System.out.println("- .createByTime() -");
        examples.replaySubjectLimitedByTime();
        System.out.println("--- BehaviorSubject ---");
        examples.behaviorSubject();
        System.out.println("--- AsyncSubject ---");
        examples.asyncSubject();
        System.out.println("--- Disposable.dispose() on ReplaySubject ---");
        examples.disposeEffect();
        System.out.println("--- Disposable.dispose() on independent subscriptions ---");
        examples.disposableIndependence();
        System.out.println("--- UnicastSubject ---");
        examples.unicastSubject();
    }

    /**
     * PublishSubject does not cache emitted values: new subscriptions receive only values emitted after subscription
     */
    void publishSubject() {
        PublishSubject<Integer> subject = PublishSubject.create();
        subject.onNext(1);
        subject.subscribe(System.out::println);
        subject.onNext(2);
        subject.onNext(3);
        subject.onNext(4);
        // Prints 2, 3, 4
    }

    /**
     * ReplaySubject caches previous values, which may be limited by size (.createWithSize)
     */
    void replaySubjectLimitedBySize() {
        ReplaySubject<Integer> replaySubject = ReplaySubject.createWithSize(2);
        replaySubject.subscribe(i -> System.out.println("Early: " + i));
        replaySubject.onNext(1);
        replaySubject.onNext(2);
        replaySubject.onNext(3);
        replaySubject.subscribe(i -> System.out.println("Late: " + i));
        replaySubject.onNext(4);
        replaySubject.onNext(5);
        // Early prints all values, Late - except 1 (out of cached limit)
    }

    /**
     * ReplaySubject caches previous values, which may be limited by time (.createWithTime) or
     * both size and time (.createWithSizeAndTime)
     */
    void replaySubjectLimitedByTime() throws Exception {
        ReplaySubject<Integer> replaySubject = ReplaySubject.createWithTime(150, TimeUnit.MILLISECONDS,
                Schedulers.trampoline());
        replaySubject.subscribe(i -> System.out.println("Early: " + i));
        replaySubject.onNext(1);
        Thread.sleep(100);
        replaySubject.onNext(2);
        Thread.sleep(100);
        replaySubject.subscribe(i -> System.out.println("Late: " + i));
        replaySubject.onNext(3);
        Thread.sleep(100);
        replaySubject.onNext(4);
        // Early prints all values, Late - except 1 (outside timeout cache)
    }

    /**
     * BehaviorSubject is the same as ReplaySubject.createWithSize(1) - caches only last emitted value and delivers it
     * to new subscription
     */
    void behaviorSubject() {
        BehaviorSubject<Integer> subject = BehaviorSubject.create();
        subject.subscribe(i -> System.out.println("Early: " + i));
        subject.onNext(1);
        subject.onNext(2);
        subject.subscribe(i -> System.out.println("Late: " + i));
        subject.onNext(3);
        subject.onNext(4);
        // Early prints all emitted, late - 2, 3, 4
    }

    /**
     * AsyncSubject emits only last value supplied into onNext() before onComplete().
     * If onComplete() is not invoked - nothing is printed in example below.
     */
    void asyncSubject() {
        AsyncSubject<Integer> subject = AsyncSubject.create();
        subject.subscribe(System.out::println);
        subject.onNext(1);
        subject.onNext(2);
        subject.onNext(3);
        subject.onComplete();
        // Only 3 will be printed
    }

    /**
     * UnicastSubject replays values emitted before subscription. Also it allows only one Observer.
     * Additional Observers will receive onError() with IllegalStateException.
     */
    void unicastSubject() {
        UnicastSubject<Integer> subject = UnicastSubject.create();
        subject.onNext(1);
        subject.onNext(2);
        subject.subscribe(i -> System.out.println("First: "+ i),
                s -> System.err.println("First: " + s));
        subject.onNext(3);
        subject.subscribe(i -> System.out.println("Second: "+ i),
                s -> System.err.println("Second: " + s));
        subject.onNext(4);
        subject.subscribe(i -> System.out.println("Third: "+ i),
                s -> System.err.println("Third: " + s));
        subject.onNext(5);
    }


    /**
     * Disposable#dispose() causes subscriber to not receive consequent emitted values.
     * Note: in RxJava 1.x it was unsubscribe(), but since RxJava 2.x it's dispose()
     */
    void disposeEffect() {
        Subject<Integer> values = ReplaySubject.create();
        Disposable disposable = values.subscribe(
                System.out::println,
                System.err::println,
                () -> System.out.println("Done")
        );
        values.onNext(0);
        values.onNext(1);
        disposable.dispose();
        values.onNext(2);
        // Subscriber will not receive emitted value after .dispose()
    }

    /**
     * Disposables are independent between subscriptions: disposing one does not cause disposing
     * another on the same Observable.
     */
    void disposableIndependence() {
        Subject<Integer> values = ReplaySubject.create();
        Disposable disposable1 = values.subscribe(
                v -> System.out.println("First: " + v)
        );
        Disposable disposable2 = values.subscribe(
                v -> System.out.println("Second: " + v)
        );
        values.onNext(0);
        values.onNext(1);
        disposable1.dispose();
        System.out.println("Unsubscribed first");
        values.onNext(2);
        // Second Subscriber still gets emitted value as second Disposable had not been disposed yet.
    }
}
