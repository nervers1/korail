package or.kr.formulate.korail.dummy.timeout;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class CallableTimeout implements Callable<Map<String, Object>> {
    @Override
    public Map<String, Object> call() throws Exception {
        String result = "Callable 반환값";
        Map<String, Object> map = new ConcurrentHashMap<>();
        map.put("result", result);




         TimeUnit.SECONDS.sleep(10);




        return map;
    }
}
