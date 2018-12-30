import io.reactivex.*;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

import java.util.ArrayList;

public class AggregationExamples {
    private static final Consumer<Throwable> ON_ERROR = e -> System.out.println("Error: " + e);
    private static final Action ON_COMPLETE = () -> System.out.println("Completed");

    private static class SuccessConsumer<T> implements Consumer<T> {
        private String name;

        SuccessConsumer(String name) {
            this.name = name;
        }

        @Override
        public void accept(T t) {
            System.out.println(name + ": " + t);
        }
    }

    private static class PrintObserver<T> implements Observer<T> {

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

    public static void main(String[] args) {
        AggregationExamples examples = new AggregationExamples();
        System.out.println("--- reduce() ---");
        examples.reduce();
        System.out.println("--- scan() ---");
        examples.scan();
        System.out.println("--- collect() ---");
        examples.collect();
        System.out.println("--- toList() ---");
        examples.toList();
        System.out.println("--- toSortedList() ---");
        examples.toSortedList();
        System.out.println("--- toMap() ---");
        examples.toMap();
        System.out.println("--- toMultiMap() ---");
        examples.toMultiMap();
        System.out.println("--- groupBy() ---");
        examples.groupBy();
    }

    /**
     * Similar to concept of Reduce in MapReduce, it performs some aggregate calculation on sequence of values.
     * In example below we calculate Sum and Min of {1, 4, 0, 5, 12}
     */
    private void reduce() {
        Observable<Integer> values = Observable.fromArray(1, 4, 0, 5, 12);
        System.out.println("Original:");
        values.subscribe(new PrintObserver<>());
        values.reduce((i1, i2) -> i1 + i2)
                .subscribe(new SuccessConsumer<>("Sum"), ON_ERROR, ON_COMPLETE);

        values.reduce((i1, i2) -> (i1 > i2) ? i2 : i1)
                .subscribe(new SuccessConsumer<>("Min"), ON_ERROR, ON_COMPLETE);
    }

    /**
     * Similar to reduce(), but also emits all intermediate results
     */
    private void scan() {
        Observable<Integer> values = Observable.fromArray(1, 4, 0, 5, 12);
        System.out.println("Original:");
        values.subscribe(new PrintObserver<>());
        values.scan((i1, i2) -> i1 + i2)
                .subscribe(new SuccessConsumer<>("Sum"), ON_ERROR, ON_COMPLETE);

        values.scan((i1, i2) -> (i1 > i2) ? i2 : i1)
                .subscribe(new SuccessConsumer<>("Min"), ON_ERROR, ON_COMPLETE);
    }

    /**
     * collect() is special version of reduce(), which allows using mutable accumulator, as in example below -
     * for generating new collection.
     * Note that this type of collecting sequence into container is not the best and there are different options.
     */
    private void collect() {
        Observable<Integer> values = Observable.range(10, 5);
        values.collect(
                () -> new ArrayList<Integer>(),
                (acc, value) -> acc.add(value))
                .subscribe(new SuccessConsumer<>("collect()"));
    }

    /**
     * Comparing to {@link #collect()}, toList() is specifically designed for collecting sequence of values into
     * list container.
     */
    private void toList() {
        Observable<Integer> values = Observable.range(10,5);
        values.toList()
                .subscribe(new SuccessConsumer<>("toList()"));
    }

    /**
     * Variation of {@link #toList()}, returning sorted collection of sequence elements.
     */
    private void toSortedList() {
        Observable<Integer> values = Observable.fromArray(1, 4, 0, 5, 12);
        values.toSortedList()
                .subscribe(new SuccessConsumer<>("toList()"));
    }

    private static class Person {
        String name;
        int age;

        public Person(String name, int age) {
            this.name = name;
            this.age = age;
        }
    }

    /**
     * Adds elements from the sequence into the map, where key is defined by keySelector and value is
     * transformed by valueSelector.
     */
    private void toMap() {
        Observable<Person> persons = Observable.just(
                new Person("Jack", 27),
                new Person("Jill", 25),
                new Person("Bob", 30),
                new Person("Fred", 25),
                new Person("Alice", 28)
        );
        // Mapping name to the age
        persons.toMap(
                person -> person.name,
                person -> person.age)
                .subscribe(new SuccessConsumer<>("toMap()"));
    }

    /**
     * Key -> ListOfValues mapping
     */
    private void toMultiMap() {
        Observable<Person> persons = Observable.just(
                new Person("Jack", 27),
                new Person("Jill", 25),
                new Person("Bob", 30),
                new Person("Fred", 25),
                new Person("Alice", 28)
        );
        // Mapping age to the names:
        // several people may be in the collection of values corresponding to the same key (age)
        persons.toMultimap(
                person -> person.age,
                person -> person.name)
                .subscribe(new SuccessConsumer<>("toMultiMap()"));
    }

    /**
     * Rx way of doing MultiMap.
     * For each value, it calculates a key and groups the values into separate observables based on that key.
     */
    private void groupBy() {
        Observable<String> values = Observable.just(
                "first",
                "second",
                "third",
                "forth",
                "fifth",
                "sixth"
        );
        values.groupBy(word -> word.charAt(0))
                .flatMap(group ->
                        group.last("").map(v -> group.getKey() + ": " + v).toObservable()
                )
                .subscribe(System.out::println);
    }
}
