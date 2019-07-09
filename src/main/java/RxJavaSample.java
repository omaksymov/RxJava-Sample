import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Function;

import java.util.*;

public class RxJavaSample {
    private static Map<Integer, String> MAP = new HashMap<>();

    public static void main(String[] args) throws Exception {
        urlsExample();
    }

    private static void urlsExample() {
        Map<String, String> urlDict = new HashMap<>();
        urlDict.put("google.com", "Google");
        urlDict.put("bing.com", "Bing");
        urlDict.put("fb.com", "Facebook");
        urlDict.put("twitter.com", "Twitter");
        urlDict.put("instagram.com", "Instagram");
        query("social")
                .flatMap(Observable::fromIterable)
                .flatMap((Function<String, ObservableSource<String>>) s -> Observable.just(urlDict.get(s)))
                .filter(Objects::nonNull)
                .take(2)
                .map(String::toLowerCase)
                .doOnNext(s -> save(s))
                .subscribe(System.out::println);
        for (Map.Entry<Integer, String> entry : MAP.entrySet()) {
            System.out.println("" + entry.getKey() + " -> " + entry.getValue());
        }
    }

    private static void save(String s) {
        MAP.put(s.length(), s);
    }

    private static Observable<List<String>> query(String query) {
        switch (query) {
            case "search":
                List<String> searchSites = new ArrayList<>();
                searchSites.add("google.com");
                searchSites.add("bing.com");
                return Observable.just(searchSites);
            case "social":
                List<String> socialSites = new ArrayList<>();
                socialSites.add("fb.com");
                socialSites.add("twitter.com");
                socialSites.add("instagram.com");
                return Observable.just(socialSites);
            default:
                return Observable.empty();

        }
    }
}
