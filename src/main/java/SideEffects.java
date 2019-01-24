import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.ReplaySubject;

public class SideEffects {
    public static void main(String[] args) {
        SideEffectExample example = new SideEffectExample();
        System.out.println("--- sideEffectExample() ---");
        example.sideEffectExample();
        System.out.println("--- scan() ---");
        FixedSideEffectUsingScan scanExample = new FixedSideEffectUsingScan();
        scanExample.scan();
        System.out.println("--- doOnEach() ---");
        CommonWantedSideEffects doSmth = new CommonWantedSideEffects();
        doSmth.doOnEachExample();
        System.out.println("--- doOnSubscribe() ---");
        doSmth.doOnSubscriptionEventExample();
        System.out.println("--- Mutable data in Rx flow ---");
        MutableElements mutable = new MutableElements();
        mutable.modifyDataByReferenceExample();
    }

    private static class SideEffectExample {
        private static class Inc {
            private int count = 0;

            private void inc() {
                count++;
            }
            private int getCount() {
                return count;
            }
        }

        // Side effect here is incrementing index variable, so that on second subscription index will start from 5,
        // not from 1 as on the first subscription
        private void sideEffectExample() {
            Observable<String> values = Observable.just("No", "side", "effects", "please");

            Inc index = new Inc();
            Observable<String> indexed =
                    values.map(w -> {
                        index.inc();
                        return w;
                    });
            indexed.subscribe(w -> System.out.println("1st observer: " + index.getCount() + ": " + w));
            indexed.subscribe(w -> System.out.println("2nd observer: " + index.getCount() + ": " + w));
        }
    }


    private static class FixedSideEffectUsingScan {

        private static class Indexed<T> {
            private final int index;
            private final T item;

            private Indexed(int index, T item) {
                this.index = index;
                this.item = item;
            }
        }


        // Here we fix the side-effect from #sideEffectExample() by storing state in emitted data itself.
        // scan() allows to accumulate the value using previously emitted values, starting from some initial one.
        private void scan() {
            Observable<String> values = Observable.just("No", "side", "effects", "please");
            Observable<Indexed<String>> indexed =
                    values.scan(
                            new Indexed<String>(0, null),
                            (prev, v) -> new Indexed<>(prev.index + 1, v))
                            .skip(1);
            indexed.subscribe(w -> System.out.println("1st observer: " + w.index + ": " + w.item));
            indexed.subscribe(w -> System.out.println("2nd observer: " + w.index + ": " + w.item));
        }
    }

    private static class CommonWantedSideEffects {


        // This example shows that 'Log:' messages on each action for emitted observable are independent from
        // actual processing them. Thus, such doOnEach() can be used for logging side-effects as one of possible options.
        // It also allows to log what was produced instead of only what was actually consumed after filtering on subscriber side.
        private void doOnEachExample() {
            Observable<String> values = Observable.just("side", "effects");

            values.doOnEach(new PrintObserver<>("Log"))
                  .map(s -> s.toUpperCase())
                  .subscribe(new PrintObserver<>("Process"));
        }

        // Difference between do...() methods:
        // doOnEach runs when any notification is emitted
        // doOnNext runs when a value is emitted
        // doOnError runs when the observable terminates with an error
        // doOnComplete runs when the observable terminates with no error
        // doOnTerminate runs when the observable terminates

        /*
           doOnSubscribe() / doOnDispose() differ from other doOn... methods a bit because of the fact that
            appropriate events are not emitted by Observer.
          */
        private void doOnSubscriptionEventExample() {
            ReplaySubject<Integer> subject = ReplaySubject.create();
            Observable<Integer> values = subject
                    .doOnSubscribe(disposable -> System.out.println("New subscription"))
                    .doOnDispose(() -> System.out.println("Subscription over"));

            Disposable s1 = values.subscribe(integer -> System.out.println("1st: " + integer));
            subject.onNext(0);
            values.subscribe(integer -> System.out.println("2nd: " + integer));
            subject.onNext(1);
            s1.dispose();
            subject.onNext(2);
            subject.onNext(3);
            subject.onComplete();
        }
    }

    private static class MutableElements {
        private static class Data {
            private int id;
            private String name;

            private Data(int id, String name) {
                this.id = id;
                this.name = name;
            }

            @Override
            public String toString() {
                return id + ": " + name;
            }
        }

        // this example shows that data in observables is passed by reference:
        // after first subscription, which replaces data String value to "Garbage", on second subscription we'll see
        // already modified data, because the reference to data object will be the same as for the first subscription.
        private void modifyDataByReferenceExample() {
            Observable<Data> data = Observable.just(
                    new Data(1, "Data1"),
                    new Data(2, "Data2")
            );
            data.subscribe(new PrintObserver<>("Original"));
            data.subscribe(d -> d.name = "Garbage");
            data.subscribe(d -> System.out.println(d.id + ": " + d.name));
        }
    }
}
