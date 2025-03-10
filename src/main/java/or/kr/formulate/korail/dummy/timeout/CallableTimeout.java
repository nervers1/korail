package or.kr.formulate.korail.dummy.timeout;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class CallableTimeout implements Callable<Map<String, Object>> {
    private final int timeout = 1;
    @Override
    public Map<String, Object> call() throws Exception {
        String result = "Callable 반환값";
        Map<String, Object> map = new ConcurrentHashMap<>();
        for (int i = 0; i < 5; i++) {
            map.put("result" + i, result + "[" + i + "]");
        }


        try {
            TimeUnit.SECONDS.sleep(timeout);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
//            throw new InterruptedException();
        //
        }

        return map;
    }
}
