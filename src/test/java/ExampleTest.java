import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;
import io.reactivex.schedulers.TestScheduler;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

public class ExampleTest {
    @Test
    public void testEmittedIntervalObservableDuring5Seconds() {
        TestScheduler scheduler = new TestScheduler();
        List<Long> expected = Arrays.asList(0L, 1L, 2L, 3L, 4L);
        List<Long> result = new ArrayList<>();
        Observable
                .interval(1, TimeUnit.SECONDS, scheduler)
                .take(5)
                .subscribe(i -> result.add(i));
        assertTrue(result.isEmpty());
        scheduler.advanceTimeBy(5, TimeUnit.SECONDS);
        assertTrue(result.equals(expected));
    }

    @Test
    public void testEmittedIntervalObservableDuring5SecondsUsingTestObserver() {
        TestScheduler scheduler = new TestScheduler();
        Long[] expected = new Long[] {0L, 1L, 2L, 3L, 4L};
        TestObserver<Long> testObserver = new TestObserver<>();
        Observable
                .interval(1, TimeUnit.SECONDS, scheduler)
                .take(5)
                .subscribe(testObserver);
        testObserver.assertNoValues();
        scheduler.advanceTimeBy(5, TimeUnit.SECONDS);
        testObserver.assertValues(expected);
    }
}
