import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.schedulers.TestScheduler;

import java.util.concurrent.TimeUnit;

public class SchedulerExamples {
    public static void main(String[] args) throws Exception {
        SchedulerExamples examples = new SchedulerExamples();
//        System.out.println("--- trampoline() ---");
//        examples.trampoline();
//        System.out.println("--- newThread() ---");
//        examples.newThread();
        /*
            Other common schedulers:
            - computation() is intended for CPU work
            - io() is intended for IO work
            - test() was useful for testing and debugging, but conceptually different from other Schedulers, so it was
            removed in RxJava 2.x. Use new TestScheduler() instead.
         */

//        System.out.println("--- TestScheduler advanceTimeTo() ---");
//        examples.advanceTimeToExample();
//        System.out.println("--- TestScheduler advanceTimeBy() ---");
//        examples.advanceTimeByExample();
        System.out.println("--- TestScheduler triggerActions() ---");
        examples.triggerActionsExample();
        System.out.println("--- TestScheduler : scheduling collisions ---");
        examples.schedulingCollisions();
    }

    // utility method
    private void printThread(String s) {
        System.out.println(s + " on " + Thread.currentThread().getId());
    }

    // Schedulers.trampoline() queues work on the current thread to be executed after the current work completes.
    private void trampoline() {
        Scheduler scheduler = Schedulers.trampoline();
        Scheduler.Worker worker = scheduler.createWorker();
        worker.schedule(() -> {
            printThread("Start");
            // In this example "Inner" will be executed after "End"
            worker.schedule(() -> printThread("Inner"));
            printThread("End");
        });
        // main thread will be blocked until tasks scheduled on trampoline scheduler are executed
        System.out.println("Main end");
        // As a result we'll see all messages printed on the main thread
    }

    // Workers created from Schedulers.newThread() will perform their actions on a dedicated thread, different from the
    // one where schedule happens
    private void newThread() throws Exception {
        Scheduler scheduler = Schedulers.newThread();
        Scheduler.Worker worker = scheduler.createWorker();
        worker.schedule(() -> {
            printThread("Start");
            // In this example "Inner" will be executed on different from where "Start" and "End" are executed.
            worker.schedule(() -> printThread("Inner"));
            printThread("End");
        });
        System.out.println("Main after schedule");
        // We block current thread to see the outcomes of parallel execution on different thread
        Thread.sleep(10);
        worker.schedule(() -> printThread("Again"));
        System.out.println("Main end");
    }

    /*
        This example shows how virtual time in TestScheduler works. Virtual time allows to not wait for long time when
        testing some event expected to occur after that long time.
        There are 2 version of manipulating with virtual time: advanceTimeTo and advanceTimeBy.
        Here we see example of the first one.
     */
    private void advanceTimeToExample() {
        TestScheduler s = new TestScheduler();

        s.createWorker().schedule(
                () -> System.out.println("Immediate"));
        s.createWorker().schedule(
                () -> System.out.println("20s"),
                20, TimeUnit.SECONDS);
        s.createWorker().schedule(
                () -> System.out.println("40s"),
                40, TimeUnit.SECONDS);

        System.out.println("Advancing to 1ms");
        s.advanceTimeTo(1, TimeUnit.MILLISECONDS);
        System.out.println("Virtual time: " + s.now(TimeUnit.MILLISECONDS));

        System.out.println("Advancing to 10s");
        s.advanceTimeTo(10, TimeUnit.SECONDS);
        System.out.println("Virtual time: " + s.now(TimeUnit.MILLISECONDS));

        System.out.println("Advancing to 40s");
        s.advanceTimeTo(40, TimeUnit.SECONDS);
        System.out.println("Virtual time: " + s.now(TimeUnit.MILLISECONDS));
    }

    /*
        Comparing to advanceTimeTo(), advanceTimeBy() considers relative virtual time shift.
     */
    private void advanceTimeByExample() {
        TestScheduler s = new TestScheduler();

        s.createWorker().schedule(
                () -> System.out.println("Immediate"));
        s.createWorker().schedule(
                () -> System.out.println("20s"),
                20, TimeUnit.SECONDS);
        s.createWorker().schedule(
                () -> System.out.println("40s"),
                40, TimeUnit.SECONDS);

        System.out.println("Advancing by 1ms");
        s.advanceTimeBy(1, TimeUnit.MILLISECONDS);
        System.out.println("Virtual time: " + s.now(TimeUnit.MILLISECONDS));

        System.out.println("Advancing by 10s");
        s.advanceTimeBy(10, TimeUnit.SECONDS);
        System.out.println("Virtual time: " + s.now(TimeUnit.MILLISECONDS));

        System.out.println("Advancing by 40s");
        s.advanceTimeBy(40, TimeUnit.SECONDS);
        System.out.println("Virtual time: " + s.now(TimeUnit.MILLISECONDS));
    }

    /*
       triggerActions() does not advance time. It only executes actions that were scheduled to be executed up
       to the present.
     */
    private void triggerActionsExample() {
        TestScheduler s = new TestScheduler();

        s.createWorker().schedule(
                () -> System.out.println("Immediate"));
        s.createWorker().schedule(
                () -> System.out.println("20s"),
                20, TimeUnit.SECONDS);

        s.triggerActions();
        System.out.println("Virtual time: " + s.now(TimeUnit.MILLISECONDS));
    }

    /*
        When actions are scheduled for the same moment in time, we have a scheduling collision.
        The order that two simultaneous tasks are executed is the same as the order in which they where scheduled.
     */
    private void schedulingCollisions() {
        TestScheduler s = new TestScheduler();

        s.createWorker().schedule(
                () -> System.out.println("First"),
                20, TimeUnit.SECONDS);
        s.createWorker().schedule(
                () -> System.out.println("Second"),
                20, TimeUnit.SECONDS);
        s.createWorker().schedule(
                () -> System.out.println("Third"),
                20, TimeUnit.SECONDS);

        s.advanceTimeTo(20, TimeUnit.SECONDS);
    }
}
