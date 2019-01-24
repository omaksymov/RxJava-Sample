import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class PrintObserver<T> implements Observer<T> {
    private final String name;

    public PrintObserver(String name) {
        this.name = name;
    }

    @Override
    public void onSubscribe(Disposable d) {
    }

    @Override
    public void onNext(T v) {
        System.out.println(name + ": " + v);
    }

    @Override
    public void onError(Throwable e) {
        System.out.println(name + ": Error: " + e);
    }

    @Override
    public void onComplete() {
        System.out.println(name + ": Completed");
    }
}
