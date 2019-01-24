import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observables.ConnectableObservable;

import java.util.concurrent.TimeUnit;

public class ColdAndHotObservables {
    public static void main(String[] args) throws Exception {
        ColdAndHotObservables example = new ColdAndHotObservables();
        System.out.println("--- Cold Observable ---");
        example.coldObservableExample();
        System.out.println("--- Hot Observable ---");
        example.hotObservableWithPublishExample();
        System.out.println("--- Disconnect from Hot Observable ---");
        example.disconnectFromHotObservableExample();
        System.out.println("--- refCount ---");
        example.refCountExample();
        System.out.println("--- replay() ---");
        example.replayExample();
        System.out.println("--- cache() ---");
        example.cacheExample();
        System.out.println("--- cache() unsubscribe ---");
        example.cacheUnsubscribeExample();
    }

    /*
        Cold observables are observables that run their sequence when and if they are subscribed to.
        They present the sequence from the start to each subscriber.
        An example of a cold observable would be Observable.interval.
        Regardless of when it is created and when it is subscribed to, it will generate the same sequence for
        every subscriber.
      */
    private void coldObservableExample() throws Exception {
        Observable<Long> cold = Observable.interval(200, TimeUnit.MILLISECONDS);
        Disposable d1 = cold.subscribe(i -> System.out.println("First: " + i));
        Thread.sleep(500);
        Disposable d2 = cold.subscribe(i -> System.out.println("Second: " + i));
        Thread.sleep(500);
        d1.dispose();
        d2.dispose();
    }

    /*
        Hot observables emit values independent of individual subscriptions.
        They have their own timeline and events occur whether someone is listening or not.
        In RxJava we can transform cold observables into hot using publish().
     */
    private void hotObservableWithPublishExample() throws Exception {
        Observable<Long> cold = Observable.interval(200, TimeUnit.MILLISECONDS);
        ConnectableObservable<Long> hot = cold.publish();
        hot.connect();
        Disposable d1 = hot.subscribe(i -> System.out.println("First: " + i));
        Thread.sleep(500);
        Disposable d2 = hot.subscribe(i -> System.out.println("Second: " + i));
        Thread.sleep(500);
        d1.dispose();
        d2.dispose();
    }
    
    /*
        Example shows that after disconnecting one of observers from the hot observable - other observers still get
        the emitted values.
     */
    private void disconnectFromHotObservableExample() throws Exception {
        ConnectableObservable<Long> connectable = Observable.interval(200, TimeUnit.MILLISECONDS).publish();
        connectable.connect();

        Disposable d1 = connectable.subscribe(i -> System.out.println("First: " + i));
        Thread.sleep(500);
        Disposable d2 = connectable.subscribe(i -> System.out.println("Second: " + i));

        Thread.sleep(500);
        System.out.println("Disconnect second");
        d2.dispose();
        Thread.sleep(500);
        System.out.println("Disconnect first");
        d1.dispose();
    }

    /*
        This example shows that refCount returns an Observable that stays connected to ConnectedObservable (hot
        Observable) as long as there is at least one subscription to ConnectedObservable. After new subscription the
        underlying cold observable emits the values starting from the beginning of the stream.
        Also note that no connect() used after publish() like in examples before.
        share() is an alias for publish().refCount().
     */
    private void refCountExample() throws Exception {
        Observable<Long> cold = Observable.interval(200, TimeUnit.MILLISECONDS).publish().refCount();

        Disposable s1 = cold.subscribe(i -> System.out.println("First: " + i));
        Thread.sleep(500);
        Disposable s2 = cold.subscribe(i -> System.out.println("Second: " + i));
        Thread.sleep(500);
        System.out.println("Disconnect second");
        s2.dispose();
        Thread.sleep(500);
        System.out.println("Disconnect first");
        s1.dispose();

        System.out.println("First connection again");
        Thread.sleep(500);
        s1 = cold.subscribe(i -> System.out.println("First: " + i));
        // In output at this point we see that Observable.interval() emits values from the beginning of the stream.
        Thread.sleep(600);
        System.out.println("Final disconnect");
        s1.dispose();
    }

    /*
        Combines behavior of ReplaySubject and publish() : when replay() invoked on cold observable - after this point
        we can connect() to it and every new subscription gets all the values, which subscriptions before it already
        received (similar to ReplaySubject).
     */
    private void replayExample() throws Exception {
        ConnectableObservable<Long> cold = Observable.interval(200, TimeUnit.MILLISECONDS).replay();
        Disposable s = cold.connect();

        System.out.println("Subscribe first");
        Disposable d1 = cold.subscribe(i -> System.out.println("First: " + i));
        Thread.sleep(700);
        System.out.println("Subscribe second");
        Disposable d2 = cold.subscribe(i -> System.out.println("Second: " + i));
        // at this point second subscription receives everything already received by the first subscription.
        Thread.sleep(500);
        d1.dispose();
        d2.dispose();
    }

    /*
        The cache operator has a similar function to replay, but hides away the ConnectableObservable and removes the
        managing of subscriptions. The internal ConnectableObservable is subscribed to when the first observer arrives.
        Subsequent subscribers have the previous values replayed to them from the cache and don't result in a new
        subscription to the source observable.
     */
    private void cacheExample() throws Exception {
        Observable<Long> obs = Observable.interval(100, TimeUnit.MILLISECONDS)
                .take(5)
                .cache();

        Thread.sleep(500);
        Disposable d1 = obs.subscribe(i -> System.out.println("First: " + i));
        Thread.sleep(300);
        Disposable d2 = obs.subscribe(i -> System.out.println("Second: " + i));
        Thread.sleep(500);
        d1.dispose();
        d2.dispose();
    }


    /*
        An important thing to note is that the internal ConnectableObservable doesn't unsubscribe if all the
        subscribers go away, like refCount would. Once the first subscriber arrives, the source observable will be
        observed and cached once and for all. This matters because we can't walk away from an infinite observable anymore.
        Values will continue to cached until the source terminates or we run out of memory.
        The overload that specifies capacity isn't a solution either, as the capacity is received as a hint for
        optimisation and won't actually restrict the size of our cache.
     */
    private void cacheUnsubscribeExample() throws Exception {
        Observable<Long> obs = Observable.interval(100, TimeUnit.MILLISECONDS)
                .take(5)
                .doOnNext(System.out::println)
                .cache()
                .doOnSubscribe((v) -> System.out.println("Subscribed"))
                .doOnDispose(() -> System.out.println("Unsubscribed"));

        Disposable subscription = obs.subscribe();
        Thread.sleep(150);
        subscription.dispose();
        Thread.sleep(500);
    }

}