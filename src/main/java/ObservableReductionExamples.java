import io.reactivex.Observable;

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


    /**
     * Filters only values satisfying given predicate (in example below we get only even numbers from the sequence)
     */
    private void filter() {
        Observable<Integer> values = Observable.range(1, 8);
        values.subscribe(new PrintObserver<>("Original"));
        values.filter(v -> v % 2 == 0)
                .subscribe(new PrintObserver<>("Filtered"));
    }

    /**
     * Filters out the values already appeared in the sequence
     */
    private void distinct() {
        Observable<Integer> values = Observable.fromArray(1, 1, 2, 3, 3);
        values.subscribe(new PrintObserver<>("Original"));
        values.distinct()
                .subscribe(new PrintObserver<>("Distinct"));
    }

    /**
     * Same as {@link #distinct()}, but considers as key some transformation of initial value. In example below - we
     * consider distinct those characters, whose lowercase representation is different.
     */
    private void distinctWithKeySelector() {
        Observable<Character> values = Observable.fromArray('A', 'B', 'b', 'c', 'a');
        values.subscribe(new PrintObserver<>("Original"));
        values.distinct(c -> Character.toLowerCase(c))
                .subscribe(new PrintObserver<>("Distinct lower case"));
    }

    /**
     * Same as {@link #distinct()} ()}, but ignores only consequent non-distinct elements.
     */
    private void distinctUntilChanged() {
        Observable<Character> values = Observable.fromArray('A', 'A', 'B', 'C', 'B', 'b', 'b');
        values.subscribe(new PrintObserver<>("Original"));
        values.distinctUntilChanged()
                .subscribe(new PrintObserver<>("Distinct until changed"));
    }

    /**
     * Just ignores any elements in the sequence, but terminates with onComplete().
     * Note that #ignoreElements() returns Completable rather than Observable<T>.
     */
    private void ignore() {
        Observable<Integer> values = Observable.range(0, 5);
        values.subscribe(new PrintObserver<>("Original"));
        System.out.println("After ignored:");
        values.ignoreElements()
                .subscribe(() -> System.out.println("Completed"));
    }

    /**
     * Skips first n elements in the sequence
     */
    private void skip() {
        Observable<Integer> values = Observable.range(0, 5);
        values.subscribe(new PrintObserver<>("Original"));
        values.skip(2)
                .subscribe(new PrintObserver<>("Skip 2"));
    }

    /**
     * Skips first elements while specified predicate is true (in example below v < 3)
     */
    private void skipWhile() {
        Observable<Integer> values = Observable.fromArray(1, 2, 3, 4, 1, 2, 3, 4);
        values.subscribe(new PrintObserver<>("Original"));
        values.skipWhile(v -> v <= 2)
                .subscribe(new PrintObserver<>("Skip while v <= 2"));
    }

    /**
     * Takes first n elements in the sequence
     */
    private void take() {
        Observable<Integer> values = Observable.range(0, 5);
        values.subscribe(new PrintObserver<>("Original"));
        values.take(2)
                .subscribe(new PrintObserver<>("Take 2"));
    }
    /**
     * Takes first elements in the sequence while specified predicate is true (in example below v < 3)
     */
    private void takeWhile() {
        Observable<Integer> values = Observable.fromArray(1, 2, 3, 4, 1, 2, 3, 4);
        values.subscribe(new PrintObserver<>("Original"));
        values.takeWhile(v -> v <= 2)
                .subscribe(new PrintObserver<>("Take while v <= 2"));
    }
}
