import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;

public class SchedulerExamples {
    public static void main(String[] args) throws Exception {
        SchedulerExamples examples = new SchedulerExamples();
        System.out.println("--- trampoline() ---");
        examples.trampoline();
        System.out.println("--- newThread() ---");
        examples.newThread();
        /*
            Other common schedulers:
            - computation() is intended for CPU work
            - io() is intended for IO work
            - test() was useful for testing and debugging, but conceptually different from other Schedulers, so it was
            removed in RxJava 2.x. Use new TestScheduler() instead.
         */
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
}
