import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;

import java.util.Arrays;

public class Threading {
    public static void main(String[] args) throws Exception {
        Threading example = new Threading();
        System.out.println("--- default behavior ---");
        example.defaultBehaviorExample();
        System.out.println("--- subscribeOn ---");
        example.subscribeOnExample();
        System.out.println("--- observeOn ---");
        example.observeOnExample();
        System.out.println("--- unsubscribeOn ---");
        example.unsubscribeOnExample();
    }

    /*
        Example shows that by default observers handle values on the same thread where they had been emitted.
     */
    private void defaultBehaviorExample() throws Exception {
        final BehaviorSubject<Integer> subject = BehaviorSubject.create();
        subject.subscribe(i -> {
            System.out.println("Received " + i + " on " + Thread.currentThread().getId());
        });

        int[] i = {1}; // naughty side-effects for examples only ;)
        Runnable r = () -> {
            synchronized(i) {
                System.out.println("onNext(" + i[0] + ") on " + Thread.currentThread().getId());
                subject.onNext(i[0]++);
            }
        };

        r.run(); // Execute on main thread
        new Thread(r).start();
        new Thread(r).start();
    }


    /*
        subscribeOn() in this case set the thread on which Observable creation (when subscribed) will happen
     */
    private void subscribeOnExample() throws Exception {
        System.out.println("Main: " + Thread.currentThread().getId());

        Observable.create(o -> {
            System.out.println("Created on " + Thread.currentThread().getId());
            o.onNext(1);
            o.onNext(2);
            o.onComplete();
        })
                .subscribeOn(Schedulers.newThread()) // when this line is commented - subscription will be on the main thread
                .subscribe(i -> {
                    System.out.println("Received " + i + " on " + Thread.currentThread().getId());
                });

        System.out.println("Finished main: " + Thread.currentThread().getId());
        Thread.sleep(10);
    }

    /*
        Here we'll see that on subscription (creation of Observable) happens on the main thread, but handling emitted
        values - on the new thread.
     */
    private void observeOnExample() {
        Observable.create(o -> {
            System.out.println("Created on " + Thread.currentThread().getId());
            o.onNext(1);
            o.onNext(2);
            o.onComplete();
        })
                .observeOn(Schedulers.newThread())
                .subscribe(i ->
                        System.out.println("Received " + i + " on " + Thread.currentThread().getId()));
    }

    /*
        Some Observables depend on resources allocated on subscription and released when subscription ends.
        unsubscribeOn() allows to perform potentially heavy releasing resources on a different thread,
        BUT in example below it does not work as expected because of unknown reasons.
     */
    private void unsubscribeOnExample() {
        Observable<Object> source = Observable.using(
                () -> {
                    System.out.println("Subscribed on " + Thread.currentThread().getId());
                    return Arrays.asList(1,2);
                },
                (ints) -> {
                    System.out.println("Producing on " + Thread.currentThread().getId());
                    return Observable.fromArray(ints.toArray());
                },
                (ints) -> System.out.println("Unubscribed on " + Thread.currentThread().getId())
        );
        // TODO: figure out, why does not work according to documentation
        source.unsubscribeOn(Schedulers.newThread())
              .subscribe(System.out::println);
    }

}
