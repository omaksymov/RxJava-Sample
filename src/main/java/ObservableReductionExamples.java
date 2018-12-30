import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class ObservableReductionExamples {

    public static void main(String[] args) {
        ObservableReductionExamples examples = new ObservableReductionExamples();
        System.out.println("--- filter() ---");
        examples.filter();
        System.out.println("--- distinct() ---");
        examples.distinct();
        System.out.println("--- distinct() with key selector ---");
        examples.distinctWithKeySelector();
        System.out.println("--- distinctUntilChanged() ---");
        examples.distinctUntilChanged();
        System.out.println("--- ignoreElements() ---");
        examples.ignore();
        System.out.println("--- skip() ---");
        examples.skip();
        System.out.println("--- skipWhile() ---");
        examples.skipWhile();
        System.out.println("--- take() ---");
        examples.take();
        System.out.println("--- takeWhile() ---");
        examples.takeWhile();
    }


    private static class DebugObserver<T> implements Observer<T> {
        @Override
        public void onSubscribe(Disposable d) {
        }

        @Override
        public void onNext(T t) {
            System.out.print(t + ",");
        }

        @Override
        public void onError(Throwable e) {
            System.out.println();
            System.err.println("Error: " + e);
        }

        @Override
        public void onComplete() {
            System.out.println();
            System.out.println("Completed");
        }
    }

    /**
     * Filters only values satisfying given predicate (in example below we get only even numbers from the sequence)
     */
    private void filter() {
        Observable<Integer> values = Observable.range(1, 8);
        System.out.println("Original:");
        values.subscribe(new DebugObserver<>());
        System.out.println("Filtered:");
        values.filter(v -> v % 2 == 0)
                .subscribe(new DebugObserver<>());
    }

    /**
     * Filters out the values already appeared in the sequence
     */
    private void distinct() {
        Observable<Integer> values = Observable.fromArray(1, 1, 2, 3, 3);
        System.out.println("Original:");
        values.subscribe(new DebugObserver<>());
        System.out.println("Distinct:");
        values.distinct()
                .subscribe(new DebugObserver<>());
    }

    /**
     * Same as {@link #distinct()}, but considers as key some transformation of initial value. In example below - we
     * consider distinct those characters, whose lowercase representation is different.
     */
    private void distinctWithKeySelector() {
        Observable<Character> values = Observable.fromArray('A', 'B', 'b', 'c', 'a');
        System.out.println("Original:");
        values.subscribe(new DebugObserver<>());
        System.out.println("Distinct lower case:");
        values.distinct(c -> Character.toLowerCase(c))
                .subscribe(new DebugObserver<>());
    }

    /**
     * Same as {@link #distinct()} ()}, but ignores only consequent non-distinct elements.
     */
    private void distinctUntilChanged() {
        Observable<Character> values = Observable.fromArray('A', 'A', 'B', 'C', 'B', 'b', 'b');
        System.out.println("Original:");
        values.subscribe(new DebugObserver<>());
        System.out.println("Distinct until changed:");
        values.distinctUntilChanged()
                .subscribe(new DebugObserver<>());
    }

    /**
     * Just ignores any elements in the sequence, but terminates with onComplete().
     * Note that #ignoreElements() returns Completable rather than Observable<T>.
     */
    private void ignore() {
        Observable<Integer> values = Observable.range(0, 5);
        System.out.println("Original:");
        values.subscribe(new DebugObserver<>());
        System.out.println("After ignored:");
        values.ignoreElements()
                .subscribe(() -> System.out.println("Completed"));
    }

    /**
     * Skips first n elements in the sequence
     */
    private void skip() {
        Observable<Integer> values = Observable.range(0, 5);
        System.out.println("Original:");
        values.subscribe(new DebugObserver<>());
        System.out.println("Skip 2:");
        values.skip(2)
                .subscribe(new DebugObserver<>());
    }

    /**
     * Skips first elements while specified predicate is true (in example below v < 3)
     */
    private void skipWhile() {
        Observable<Integer> values = Observable.fromArray(1, 2, 3, 4, 1, 2, 3, 4);
        System.out.println("Original:");
        values.subscribe(new DebugObserver<>());
        System.out.println("Skip while v <= 2 :");
        values.skipWhile(v -> v <= 2)
                .subscribe(new DebugObserver<>());
    }

    /**
     * Takes first n elements in the sequence
     */
    private void take() {
        Observable<Integer> values = Observable.range(0, 5);
        System.out.println("Original:");
        values.subscribe(new DebugObserver<>());
        System.out.println("Take 2:");
        values.take(2)
                .subscribe(new DebugObserver<>());
    }
    /**
     * Takes first elements in the sequence while specified predicate is true (in example below v < 3)
     */
    private void takeWhile() {
        Observable<Integer> values = Observable.fromArray(1, 2, 3, 4, 1, 2, 3, 4);
        System.out.println("Original:");
        values.subscribe(new DebugObserver<>());
        System.out.println("Take while v <= 2 :");
        values.takeWhile(v -> v <= 2)
                .subscribe(new DebugObserver<>());
    }
}
