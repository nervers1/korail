package or.kr.formulate.korail.dummy.timeout;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class CallableList implements Callable<List<String>> {
    private final int timeout = 10;
    @Override
    public List<String> call() throws Exception {
        String result = "CallableList 반환값";
        List<String> list = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            list.add(result + "[" + i + "]");
        }


        try {
            TimeUnit.SECONDS.sleep(timeout);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        //
        }

        return list;
    }
}
